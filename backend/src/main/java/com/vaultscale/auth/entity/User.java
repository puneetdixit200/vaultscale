
package com.vaultscale.auth.entity;

import jakarta.persistence.*;
import jdk.jfr.DataAmount;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

//@entity = "this class amps to datbase table"
//@table ="use exactly this tbale name in posgresql"

@Entity
@Table(name = "users")
@Data                   // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Lombok: generates empty constructor (JPA needs this)
@AllArgsConstructor     // Lombok: generates constructor with all fields
@Builder                // Lombok: lets us do User.builder().email("x").build() — clean pattern
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column (nullable =false,unique=true)
    private String email;
    @Column (nullable =false)
    private String password;
    @Column (name="full_name")
    private String fullName;
    @Column(name="is_active",nullable = false)
    @Builder.Default
    private boolean isActive = true;
    @Column(name="created_at",nullable = false,updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    //user detials interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return isActive;
    }

}
