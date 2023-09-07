package br.psi.giganet.stockapi.branch_offices.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_BRANCH_OFFICES_READ', 'ROLE_BRANCH_OFFICES_WRITE', 'ROLE_ROOT' })")
public @interface RoleBranchOfficesRead {
}
