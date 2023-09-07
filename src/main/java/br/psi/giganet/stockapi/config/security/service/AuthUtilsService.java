package br.psi.giganet.stockapi.config.security.service;

import br.psi.giganet.stockapi.config.security.model.Permission;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthUtilsService {

    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && authentication != null) {
            return Optional.of(authentication.getName());
        }
        return Optional.empty();
    }

    public Set<Permission> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(r -> new Permission(r.getAuthority()))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

}
