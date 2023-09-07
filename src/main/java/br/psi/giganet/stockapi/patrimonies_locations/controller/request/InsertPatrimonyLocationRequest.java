package br.psi.giganet.stockapi.patrimonies_locations.controller.request;

import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InsertPatrimonyLocationRequest {

    @NotEmpty(message = "Nome não pode ser nulo")
    private String name;

    @NotEmpty(message = "Código não pode ser nulo")
    private String code;

    @NotNull(message = "Tipo da localização não pode ser nulo")
    private PatrimonyLocationType type;

    private String note;

}
