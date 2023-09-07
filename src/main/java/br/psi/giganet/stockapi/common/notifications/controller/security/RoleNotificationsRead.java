package br.psi.giganet.stockapi.common.notifications.controller.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({ 'ROLE_NOTIFICATIONS', 'ROLE_NOTIFICATIONS_STOCK_ITEM_LOW_LEVEL'," +
        "'ROLE_NOTIFICATIONS_STOCK_ITEM_VERY_LOW_LEVEL', 'ROLE_ROOT' })")
public @interface RoleNotificationsRead {
}
