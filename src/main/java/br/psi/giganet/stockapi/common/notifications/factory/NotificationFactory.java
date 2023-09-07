package br.psi.giganet.stockapi.common.notifications.factory;

import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.common.notifications.model.NotificationRole;
import br.psi.giganet.stockapi.common.notifications.model.NotificationType;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.stock.model.StockItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class NotificationFactory {


    public Notification create(Long id) {
        Notification notification = new Notification();
        notification.setId(id);

        return notification;
    }

    public Notification createOnLowLevelInStockItem(StockItem item) {
        Notification notification = new Notification();
        notification.setTitle("Nível de estoque baixo");
        notification.setDescription("O nível em estoque de " +
                item.getProduct().getName() +
                " está baixo no estoque " +
                item.getStock().getName());
        notification.setData(item.getId().toString());
        notification.setType(NotificationType.STOCK_ITEM_LOW_LEVEL);
        notification.setRoles(Collections.singletonList(
                new NotificationRole(new Permission("ROLE_NOTIFICATIONS_STOCK_ITEM_LOW_LEVEL"), notification)));
        notification.setEmployees(new ArrayList<>());

        return notification;
    }

    public Notification createOnVeryLowLevelInStockItem(StockItem item) {
        Notification notification = new Notification();
        notification.setTitle("Nível de estoque muito baixo");
        notification.setDescription("O nível em estoque de " +
                item.getProduct().getName() +
                " está muito baixo no estoque " +
                item.getStock().getName());
        notification.setData(item.getId().toString());
        notification.setType(NotificationType.STOCK_ITEM_VERY_LOW_LEVEL);
        notification.setRoles(Collections.singletonList(
                new NotificationRole(new Permission("ROLE_NOTIFICATIONS_STOCK_ITEM_VERY_LOW_LEVEL"), notification)));
        notification.setEmployees(new ArrayList<>());

        return notification;
    }

}
