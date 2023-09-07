package br.psi.giganet.stockapi.common.webhooks.controller;

import br.psi.giganet.stockapi.common.webhooks.controller.request.EmitWebhookRequest;
import br.psi.giganet.stockapi.common.webhooks.model.Webhook;
import br.psi.giganet.stockapi.common.webhooks.service.AbstractWebHookService;
import br.psi.giganet.stockapi.common.webhooks.service.WebHooksHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/webhooks")
public class WebHookController {

    @Autowired
    private WebHooksHandlerService webHookService;

    @PostMapping
    public ResponseEntity<Object> webHookReceive(
            @RequestHeader("Signature") String signature,
            @Valid @RequestBody Webhook webHook) throws IOException {
        this.webHookService.onReceiveWebHookHandler(webHook, signature);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/emit")
    public ResponseEntity<Object> webHookEmit(
            @Valid @RequestBody EmitWebhookRequest request) {
        this.webHookService.emitWebhook(request.getType(), request.getId());
        return ResponseEntity.noContent().build();
    }
}
