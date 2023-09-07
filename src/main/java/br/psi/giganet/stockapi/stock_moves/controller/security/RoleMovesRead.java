package br.psi.giganet.stockapi.stock_moves.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_MOVES_READ', 'ROLE_MOVES_WRITE_ENTRY_ITEMS'," +
        " 'ROLE_MOVES_WRITE_BETWEEN_STOCKS', 'ROLE_MOVES_WRITE_OUT_ITEM', 'ROLE_SALES_MODULE'," +
        " 'ROLE_MOVES_WRITE_ROOT', 'ROLE_MOVES_SERVICE_ORDER_WRITE', 'ROLE_ROOT' })")
public @interface RoleMovesRead {
}
