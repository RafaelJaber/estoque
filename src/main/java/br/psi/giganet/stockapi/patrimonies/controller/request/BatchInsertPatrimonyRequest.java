package br.psi.giganet.stockapi.patrimonies.controller.request;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchInsertPatrimonyRequest {

    @NotNull(message = "Produto não pode ser nulo")
    private String product;

    @NotNull(message = "Tipo do código não pode ser nulo")
    private PatrimonyCodeType codeType;

    @NotNull(message = "Localização atual não pode ser nula")
    private Long currentLocation;

    private Long entryItem;

    private String note;

    @NotEmpty(message = "É necessário informar pelo menos um código")
    @Valid
    private List<String> codes;

}
