package br.psi.giganet.stockapi.dashboard.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Data
public class DashboardData {

    private LocalDate initialDate;
    private LocalDate finalDate;

    private List<Map<String, Object>> techniciansStockItemsWithMovesCount;
    private List<Map<String, Object>> mainItems;
    private List<Map<String, Object>> mainItemsInShedAndTechnicianAndMaintenance;
    private List<Map<String, Object>> entryItems;
    private List<Map<String, Object>> obsoleteItems;
    private List<Map<String, Object>> generalItems;
    private List<Map<String, Object>> totalsByTechnicianStocks;
    private Map<String, List<Map<String, Object>>> usedItemsInServiceOrders;
}
