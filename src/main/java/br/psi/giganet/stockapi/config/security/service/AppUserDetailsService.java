package br.psi.giganet.stockapi.config.security.service;

import br.psi.giganet.stockapi.config.security.adapter.SystemUserAdapter;
import br.psi.giganet.stockapi.config.security.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private AbstractUserService users;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> optionalUser = users.handleLoginCheck(login);
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("DefaultUser and/or password are incorrect"));
        return SystemUserAdapter.create(user);
    }

}
