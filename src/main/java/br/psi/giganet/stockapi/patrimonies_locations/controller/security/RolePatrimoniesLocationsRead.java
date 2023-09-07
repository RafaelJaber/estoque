package br.psi.giganet.stockapi.patrimonies_locations.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_PATRIMONIES_LOCATIONS_READ', 'ROLE_PATRIMONIES_LOCATIONS_WRITE', 'ROLE_ROOT' })")
public @interface RolePatrimoniesLocationsRead {
}
