package ru.gerilovich.services.auth;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.gerilovich.dao.OwnerDao;
import ru.gerilovich.models.Owner;
import ru.gerilovich.models.auth.CustomUserDetails;

import java.util.Optional;

@AllArgsConstructor
@Service
public class OwnerDetailsService implements UserDetailsService {
    private final OwnerDao ownerDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Owner> owner = ownerDao.findByName(username);
        if (owner.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return CustomUserDetails.fromUser(owner.get());
    }
}
