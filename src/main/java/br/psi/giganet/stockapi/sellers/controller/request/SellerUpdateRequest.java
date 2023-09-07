package br.psi.giganet.stockapi.sellers.controller.request;

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
public class SellerUpdateRequest {

    @NotEmpty(message = "Id do vendedor é obrigatório")
    private String id;

    @NotEmpty(message = "Nome não pode ser nulo")
    private String name;

    @NotNull(message = "Filial do vendedor não pode ser nula")
    private Long branchOffice;

    @NotNull(message = "Situação do vendedor não pode ser nula")
    private Boolean isActive;
}
