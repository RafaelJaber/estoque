package br.psi.giganet.stockapi.dashboard.main_items.controller.request;

import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
public class MainDashboardItemGroupRequest {

    private Long id;

    @NotEmpty(message = "Código ID do produto não informado")
    private String label;

    @NotNull(message = "Categoria do grupo não pode ser nulo")
    private GroupCategory category;

    @NotNull(message = "Filial associada não pode ser nulo")
    private Long branchOffice;

    private Set<Long> employees;

    @NotEmpty(message = "Um grupo deve ser associado a pelo menos 1 item")
    private List<MainDashboardItemRequest> items;

}
