package br.psi.giganet.stockapi.stock_moves.annotations;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@WithMockUser(username = "teste_write_root@teste.com", authorities = {"ROLE_MOVES_WRITE_ROOT", "ROLE_MOVES_WRITE_ENTRY_ITEMS", "ROLE_MOVES_WRITE_BETWEEN_STOCKS", "ROLE_MOVES_WRITE_OUT_ITEM"})
public @interface RoleTestStockMovesWriteRoot {
}