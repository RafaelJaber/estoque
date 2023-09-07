package br.psi.giganet.stockapi.common.webhooks.factory;

import br.psi.giganet.stockapi.common.webhooks.model.Webhook;
import br.psi.giganet.stockapi.common.webhooks.model.WebHookServer;
import br.psi.giganet.stockapi.common.webhooks.model.WebHookType;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WebhookFactory {

    public Webhook create(PurchaseOrder purchaseOrder) {
        Map<String, Object> orderData = new LinkedHashMap<>();

        orderData.put("id", purchaseOrder.getId());
        orderData.put("status", purchaseOrder.getStatus());
        orderData.put("items", purchaseOrder.getItems().stream().map(item -> {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", item.getId());
            itemData.put("status", item.getStatus());

            return itemData;
        }).collect(Collectors.toList()));


        Webhook webhook = new Webhook();
        webhook.setId(UUID.randomUUID().toString());
        webhook.setOrigin(WebHookServer.STOCK_API);
        webhook.setType(WebHookType.STOCK_API_SAVE_ENTRY);
        webhook.setData(Collections.singletonMap("order", orderData));

        return webhook;
    }

}
