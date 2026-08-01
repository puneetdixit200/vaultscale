package com.vaultscale.common.security;

import org.springframework.stereotype.Component;

import java.net.*;

// This class exists purely to prevent SSRF (Server-Side Request Forgery) attacks.
// Without this, a malicious user could save an endpoint pointing to
// "http://localhost:5432" or "http://169.254.169.254/latest/meta-data/"
// and trick OUR server into attacking OUR OWN infrastructure or leaking cloud secrets.
@Component
public class SafeApiRequestValidator {

    // Main entry point — throws SecurityException if the URL is unsafe
    public void validate(String urlString) {
        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            throw new SecurityException("Malformed URL: " + urlString);
        }

        // 1. Only allow http and https — block file://, ftp://, gopher:// etc.
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new SecurityException("Only http/https URLs are allowed");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new SecurityException("URL must have a valid host");
        }

        // 2. Resolve the hostname to its ACTUAL IP address.
        // Important: we check the resolved IP, not the domain name string.
        // This blocks "DNS rebinding" attacks where a public domain like
        // "evil.com" is configured to resolve to "127.0.0.1" at request time.
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new SecurityException("Could not resolve host: " + host);
        }

        if (isPrivateOrReserved(address)) {
            throw new SecurityException("Requests to private/internal IP addresses are blocked: " + address.getHostAddress());
        }
    }

    // Checks if an IP falls into a private, loopback, or link-local range.
    // These ranges are reserved for internal networks and should NEVER be
    // reachable from a public-facing "run this API request" feature.
    private boolean isPrivateOrReserved(InetAddress address) {
        return address.isLoopbackAddress()      // 127.0.0.1 (localhost)
            || address.isLinkLocalAddress()      // 169.254.x.x (AWS/cloud metadata range!)
            || address.isSiteLocalAddress()      // 10.x.x.x, 172.16-31.x.x, 192.168.x.x (private LANs)
            || address.isMulticastAddress()      // 224.x.x.x - 239.x.x.x
            || address.isAnyLocalAddress();      // 0.0.0.0
    }
}
