package br.psi.giganet.stockapi.technician.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CurrentBranchTechnicianRequest {

    @NotNull(message = "Filial do técnico não pode ser nula")
    private Long branchOffice;

}
