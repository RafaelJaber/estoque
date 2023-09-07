package br.psi.giganet.stockapi.entries.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class InsertEntryItemRequest {

    @NotNull(message = "Código do item da ordem não pode ser nulo")
    private String orderItem;

    private String documentProductCode;
    @NotNull(message = "Quantidade recebida não pode ser nula")
    @Positive(message = "Quantidade recebida deve ser maior do que zero")
    private Double receivedQuantity;

}
