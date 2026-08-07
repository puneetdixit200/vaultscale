package com.vaultscale.common.security;

import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * Preflight SSRF guard for user-controlled outbound URLs.
 *
 * This blocks obvious local/private/reserved targets and validates every address
 * returned by DNS at validation time. It is intentionally documented as a
 * preflight guard, not perfect DNS-rebinding prevention: HttpClient performs its
 * own resolution later, so production deployments should also enforce network
 * egress policy/firewall rules.
 */
@Component
public class SafeApiRequestValidator {

    public void validate(String urlString) {
        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException exception) {
            throw new SecurityException("Malformed URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new SecurityException("Only http/https URLs are allowed");
        }

        if (uri.getUserInfo() != null) {
            throw new SecurityException("URLs containing embedded credentials are not allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new SecurityException("URL must have a valid host");
        }

        int port = uri.getPort();
        if (port < -1 || port == 0 || port > 65535) {
            throw new SecurityException("URL contains an invalid port");
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException exception) {
            throw new SecurityException("Could not resolve host");
        }

        if (addresses.length == 0) {
            throw new SecurityException("Could not resolve host");
        }

        for (InetAddress address : addresses) {
            if (isPrivateOrReserved(address)) {
                throw new SecurityException("Requests to private/internal IP addresses are blocked");
            }
        }
    }

    private boolean isPrivateOrReserved(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        byte[] bytes = address.getAddress();

        if (address instanceof Inet4Address) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);

            // Carrier-grade NAT: 100.64.0.0/10.
            if (first == 100 && second >= 64 && second <= 127) {
                return true;
            }

            // Documentation/benchmark networks should never be useful request targets.
            if ((first == 192 && second == 0 && Byte.toUnsignedInt(bytes[2]) == 2)
                    || (first == 198 && second == 51 && Byte.toUnsignedInt(bytes[2]) == 100)
                    || (first == 203 && second == 0 && Byte.toUnsignedInt(bytes[2]) == 113)) {
                return true;
            }

            // Reserved/broadcast range 240.0.0.0/4.
            return first >= 240;
        }

        if (address instanceof Inet6Address) {
            int first = Byte.toUnsignedInt(bytes[0]);
            // IPv6 Unique Local Addresses fc00::/7.
            return (first & 0xFE) == 0xFC;
        }

        return false;
    }
}
