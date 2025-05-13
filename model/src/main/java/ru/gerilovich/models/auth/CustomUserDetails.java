package ru.gerilovich.models.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ru.gerilovich.models.Owner;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails extends User {
    private final Long id;

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public static UserDetails fromUser(Owner owner) {
        return new CustomUserDetails(
                owner.getId(),
                owner.getName(),
                owner.getPassword(),
                List.of(new SimpleGrantedAuthority(owner.getRole().getRole()))
        );
    }
}
