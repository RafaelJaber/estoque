package br.psi.giganet.stockapi.entries.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InsertEntryRequest {

    @NotNull(message = "Código da ordem de compra não pode ser nula")
    private String order;
    private String fiscalDocument;
    private String documentAccessCode;
    private String note;
    @NotEmpty(message = "O lançamento deve conter pelo menos 1 item")
    @Valid
    private List<InsertEntryItemRequest> items;
    @NotNull(message = "É necessário informar se o lançamento é manual")
    private Boolean isManual;
    @NotNull(message = "É necessário informar se o lançamento deve atualizar o estoque")
    private Boolean updateStock;

}
