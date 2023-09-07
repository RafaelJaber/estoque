package br.psi.giganet.stockapi.stock_moves.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BasicInsertMoveFromServiceOrderRequest {

    @NotEmpty(message = "ID da ordem de serviço na API externa não pode ser nulo")
    private String orderId;

    private String activationId;

    @NotEmpty(message = "Tipo da ordem de serviço na API externa não pode ser nulo")
    private String orderType;

    @NotEmpty(message = "ID do cliente na API externa não pode ser nulo")
    private String customerId;

    @NotEmpty(message = "Nome do cliente na API externa não pode ser nulo")
    private String customerName;

    @Valid
    private List<BasicInsertItemMoveFromServiceOrderRequest> entryItems;

    @Valid
    private List<BasicInsertItemMoveFromServiceOrderRequest> outgoingItems;

}
