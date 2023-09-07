package br.psi.giganet.stockapi.technician.controller.request;

import br.psi.giganet.stockapi.technician.model.TechnicianSector;
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
public class UpdateTechnicianRequest {

    @NotEmpty(message = "Id do técnico é obrigatório")
    private String id;

    @NotEmpty(message = "Nome não pode ser nulo")
    private String name;

    @NotNull(message = "Filial do técnico não pode ser nula")
    private Long branchOffice;

    @NotNull(message = "Setor não pode ser nulo")
    private TechnicianSector sector;

    @NotNull(message = "Situação do técnico não pode ser nula")
    private Boolean isActive;

}
