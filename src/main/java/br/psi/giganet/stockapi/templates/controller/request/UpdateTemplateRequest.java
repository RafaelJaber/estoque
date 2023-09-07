package br.psi.giganet.stockapi.templates.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpdateTemplateRequest {

    private Long id;

    @NotEmpty(message = "Nome não pode ser nulo")
    private String name;

    @NotEmpty(message = "É necessário informar pelo menos 1 produto")
    private List<UpdateTemplateItemRequest> items;

}
