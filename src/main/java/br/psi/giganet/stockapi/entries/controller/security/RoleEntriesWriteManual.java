package br.psi.giganet.stockapi.entries.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_ENTRIES_WRITE_MANUAL', 'ROLE_ROOT' })")
public @interface RoleEntriesWriteManual {
}
