package br.psi.giganet.stockapi.dashboard.controller;

import br.psi.giganet.stockapi.dashboard.model.DashboardData;
import br.psi.giganet.stockapi.dashboard.model.SellerDashboardData;
import br.psi.giganet.stockapi.dashboard.model.TechnicianDashboardData;
import br.psi.giganet.stockapi.dashboard.service.DashboardService;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.technician.factory.TechnicianFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TechnicianFactory technicianFactory;

    @GetMapping
    public ResponseEntity<DashboardData> getData(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            @RequestParam CityOptions city) {
        return ResponseEntity.ok(dashboardService.getData(initialDate, finalDate, city));
    }

    @GetMapping("/technicians")
    public ResponseEntity<TechnicianDashboardData> getTechniciansData(
            @RequestParam(required = false) String technician,
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate) {
        return ResponseEntity.ok(technician != null ?
                dashboardService.getTechnicianData(technicianFactory.createById(technician), initialDate, finalDate) :
                dashboardService.getTechnicianData(initialDate, finalDate));
    }

    @GetMapping("/sellers")
    public ResponseEntity<SellerDashboardData> getSellersData(
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate,
            @RequestParam CityOptions city) {
        return ResponseEntity.ok(dashboardService.getSellersData(initialDate, finalDate, city));
    }

}
