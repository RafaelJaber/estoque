package br.psi.giganet.stockapi.config.security.adapter;

import br.psi.giganet.stockapi.config.security.model.SystemUser;
import br.psi.giganet.stockapi.config.security.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

public class SystemUserAdapter {

    public static UserDetails create(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));

        return new SystemUser(user, authorities);
    }

}
