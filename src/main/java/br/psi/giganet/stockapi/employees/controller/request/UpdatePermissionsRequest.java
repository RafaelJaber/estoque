package br.psi.giganet.stockapi.employees.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class UpdatePermissionsRequest {

    @NotNull(message = "Id não pode ser nulo")
    private Long id;

    @NotNull(message = "Nome não pode ser nulo")
    private String name;

    @NotNull(message = "Lista de permissões não podem ser nula")
    private Set<String> permissions;

    @NotNull(message = "Lista de filiais não pode ser nula")
    private Set<Long> branchOffices;

}
