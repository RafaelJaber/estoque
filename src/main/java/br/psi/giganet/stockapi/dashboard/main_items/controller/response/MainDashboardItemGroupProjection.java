package br.psi.giganet.stockapi.dashboard.main_items.controller.response;

import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import lombok.Data;

@Data
public class MainDashboardItemGroupProjection {

    private Long id;
    private String label;
    private GroupCategory category;

}
