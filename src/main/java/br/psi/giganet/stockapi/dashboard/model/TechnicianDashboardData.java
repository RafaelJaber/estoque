package br.psi.giganet.stockapi.dashboard.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class TechnicianDashboardData {

    private LocalDate initialDate;
    private LocalDate finalDate;

    private List<Map<String, Object>> stockItemsWithMovesCount;
    private List<Map<String, Object>> patrimonies;
}
