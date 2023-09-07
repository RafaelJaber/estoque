package br.psi.giganet.stockapi.products.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductRequest {

    private String id;
    @NotEmpty(message = "Código nao pode ser vazio/nulo")
    private String code;
    @NotEmpty(message = "Nome nao pode ser vazio/nulo")
    private String name;
    @NotNull(message = "Categoria nao pode ser vazio/nulo")
    private String category;
    @NotEmpty(message = "Fabricante nao pode ser vazio/nulo")
    private String manufacturer;
    @NotNull(message = "Unidade nao pode ser nulo")
    private String unit;

    private String description;

}
