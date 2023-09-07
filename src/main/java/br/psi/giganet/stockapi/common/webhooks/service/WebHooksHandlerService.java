package br.psi.giganet.stockapi.common.webhooks.service;

import br.psi.giganet.stockapi.common.webhooks.factory.WebhookFactory;
import br.psi.giganet.stockapi.common.webhooks.model.WebHookServer;
import br.psi.giganet.stockapi.common.webhooks.model.WebHookType;
import br.psi.giganet.stockapi.common.webhooks.model.Webhook;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.stockapi.products.categories.controller.request.CategoryWebhookRequest;
import br.psi.giganet.stockapi.products.categories.service.CategoryService;
import br.psi.giganet.stockapi.products.controller.request.ProductWebHookRequest;
import br.psi.giganet.stockapi.products.service.ProductService;
import br.psi.giganet.stockapi.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.stockapi.purchase_order.controller.request.PurchaseOrderWebhookRequest;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.purchase_order.service.PurchaseOrderService;
import br.psi.giganet.stockapi.units.adapter.UnitAdapter;
import br.psi.giganet.stockapi.units.controller.request.UnitWebhookRequest;
import br.psi.giganet.stockapi.units.model.Unit;
import br.psi.giganet.stockapi.units.service.UnitService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WebHooksHandlerService extends AbstractWebHookService {

    @Autowired
    private ProductAdapter productAdapter;
    @Autowired
    private ProductService productService;

    @Autowired
    private UnitService unitService;
    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private CategoryAdapter categoryAdapter;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PurchaseOrderAdapter purchaseOrderAdapter;
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private WebhookFactory webhookFactory;

    private ObjectMapper objectMapper;

    @PostConstruct
    private void init() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onReceive(Webhook webhook) {
        switch (webhook.getType()) {
            case PURCHASE_API_SAVE_UNIT:
                onReceiveSaveUnit(webhook);
                break;
            case PURCHASE_API_SAVE_CATEGORY:
                onReceiveCategory(webhook);
                break;
            case PURCHASE_API_SAVE_PURCHASE_ORDER:
                onReceivePurchaseOrder(webhook);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void onReceiveSaveUnit(Webhook webhook) {
        unitService.save((ArrayList<Unit>) ((objectMapper.convertValue(webhook.getData(), ArrayList.class))
                .stream()
                .map(u -> unitAdapter.transform(objectMapper.convertValue(u, UnitWebhookRequest.class)))
                .collect(Collectors.toList())));
    }

    private void onReceiveCategory(Webhook webhook) {
        categoryService.save(categoryAdapter.transform(
                objectMapper.convertValue(webhook.getData(), CategoryWebhookRequest.class)));
    }

    @SuppressWarnings("unchecked")
    private void onReceiveProduct(Webhook webhook) {
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(webhook.getData(), HashMap.class);
        onReceiveCategory(objectMapper.convertValue(map.get("category"), Webhook.class));
        onReceiveSaveUnit(objectMapper.convertValue(map.get("units"), Webhook.class));

        productService.save(productAdapter.transform(
                objectMapper.convertValue(map.get("product"), ProductWebHookRequest.class)));
    }

    @SuppressWarnings("unchecked")
    private void onReceivePurchaseOrder(Webhook webhook) {
        Map<String, Object> map = (Map<String, Object>) objectMapper.convertValue(webhook.getData(), HashMap.class);

        (objectMapper.convertValue(map.get("items"), ArrayList.class))
                .forEach(item -> {
                    Map<String, Object> itemData = (Map<String, Object>) objectMapper.convertValue(item, HashMap.class);
                    onReceiveProduct(objectMapper.convertValue(itemData.get("product"), Webhook.class));
                    onReceiveSaveUnit(objectMapper.convertValue(itemData.get("units"), Webhook.class));
                });

        purchaseOrderService.save(purchaseOrderAdapter.transform(
                objectMapper.convertValue(map.get("order"), PurchaseOrderWebhookRequest.class)));
    }

    public void onSendPurchaseOrder(PurchaseOrder order) {
        try {
            this.send(webhookFactory.create(order), WebHookServer.PURCHASE_API, false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void emitWebhook(WebHookType type, String id) {
        switch (type) {
            case STOCK_API_SAVE_ENTRY:
                if (id != null) {
                    this.onSendPurchaseOrder(this.purchaseOrderService.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Ordem de compra não encontrada")));
                } else {
                    this.purchaseOrderService.findAll().forEach(this::onSendPurchaseOrder);
                }
                break;

        }

    }

}
