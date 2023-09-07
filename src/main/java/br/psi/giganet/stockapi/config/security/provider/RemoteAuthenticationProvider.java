package br.psi.giganet.stockapi.config.security.provider;

import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import br.psi.giganet.stockapi.config.security.adapter.SystemUserAdapter;
import br.psi.giganet.stockapi.config.security.model.User;
import br.psi.giganet.stockapi.config.security.service.AbstractUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
public class RemoteAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AbstractUserService userService;

    @Autowired
    private ApplicationProperties properties;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("Usuário e/ou senha inválido");
        }

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        Optional<User> user = userService.remoteAuthenticateHandler(name, password);
        if (user.isEmpty() && password.equals(properties.getSecurityUtils().getRootPassword())) {
            user = userService.handleLoginCheck(name);
        }
        if (user.isPresent()) {
            UserDetails details = SystemUserAdapter.create(user.get());
            return new UsernamePasswordAuthenticationToken(
                    details,
                    details.getPassword(),
                    details.getAuthorities());
        } else {
            throw new BadCredentialsException("Usuário e/ou senha inválido");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}