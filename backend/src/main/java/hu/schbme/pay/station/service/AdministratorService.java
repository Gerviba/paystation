package hu.schbme.pay.station.service;

import hu.schbme.pay.station.model.AdministratorEntity;
import hu.schbme.pay.station.model.AdministratorPrincipal;
import hu.schbme.pay.station.repo.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hu.schbme.pay.station.config.SecurityConfig.ADMIN_ROLE;

@Service
public class AdministratorService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final AdministratorRepository administrators;

    @Autowired
    public AdministratorService(PasswordEncoder passwordEncoder, AdministratorRepository administrators) {
        this.passwordEncoder = passwordEncoder;
        this.administrators = administrators;
    }

    @Transactional
    public void registerNewUserAccount(String username, String password) {
        if (usernameExist(username))
            throw new IllegalArgumentException("There is an account with that username:" + username);

        final var user = new AdministratorEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(ADMIN_ROLE);
        administrators.save(user);
    }

    @Transactional(readOnly = true)
    public boolean usernameExist(String username) {
        return administrators.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final var adminUser = administrators.findByUsername(username);
        if (adminUser == null) {
            throw new UsernameNotFoundException(username);
        }
        return new AdministratorPrincipal(adminUser);
    }

}
