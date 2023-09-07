package br.psi.giganet.stockapi.dashboard.service;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.dashboard.model.DashboardData;
import br.psi.giganet.stockapi.dashboard.model.SellerDashboardData;
import br.psi.giganet.stockapi.dashboard.model.TechnicianDashboardData;
import br.psi.giganet.stockapi.dashboard.repository.DashboardRepositoryImpl;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.stock_moves.model.ExternalOrderType;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepositoryImpl dashboardRepository;

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private StockService stockService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    public DashboardData getData(LocalDate initialDate, LocalDate finalDate, CityOptions city) {
        DashboardData data = new DashboardData();
        data.setInitialDate(initialDate);
        data.setFinalDate(finalDate);

        Employee currentEmployee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        BranchOffice currentOffice = branchOfficeService.getCurrentBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada"));

        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(() -> data.setTotalsByTechnicianStocks(
                dashboardRepository.totalsByTechnicianStocks(currentOffice))));

        threads.add(new Thread(() -> data.setMainItems(
                dashboardRepository.findAllMainItems(currentEmployee, currentOffice))));

        threads.add(new Thread(() -> data.setMainItemsInShedAndTechnicianAndMaintenance(
                dashboardRepository.findAllMainItemsWithQuantityInShedAndTechnicianAndMaintenance(currentEmployee, currentOffice))));

        threads.add(new Thread(() -> data.setEntryItems(
                dashboardRepository.findAllEntryItemsWithQuantityByDate(initialDate, finalDate, currentOffice))));

        threads.add(new Thread(() -> data.setObsoleteItems(
                dashboardRepository.findAllObsoleteItems(currentOffice))));

        threads.add(new Thread(() -> data.setGeneralItems(
                dashboardRepository.findAllStockItemsInShedTechnicianMaintenanceObsoleteDefective(currentOffice))));

        threads.add(new Thread(() -> data.setTechniciansStockItemsWithMovesCount(
                dashboardRepository.getAllTechniciansStockItemsWithMoveCounts(initialDate, finalDate, currentOffice))));

        data.setUsedItemsInServiceOrders(new HashMap<>());
        threads.add(new Thread(() -> data.getUsedItemsInServiceOrders().put(
                "installation", dashboardRepository.findAllUsedItemsByOrderType(
                        ExternalOrderType.INSTALLATION, currentOffice, initialDate, finalDate))));
        threads.add(new Thread(() -> data.getUsedItemsInServiceOrders().put(
                "repair", dashboardRepository.findAllUsedItemsByOrderType(
                        ExternalOrderType.REPAIR, currentOffice, initialDate, finalDate))));
        threads.add(new Thread(() -> data.getUsedItemsInServiceOrders().put(
                "cancellation", dashboardRepository.findAllUsedItemsByOrderType(
                        ExternalOrderType.CANCELLATION, currentOffice, initialDate, finalDate))));
        threads.add(new Thread(() -> data.getUsedItemsInServiceOrders().put(
                "addressChange", dashboardRepository.findAllUsedItemsByOrderType(
                        ExternalOrderType.ADDRESS_CHANGE, currentOffice, initialDate, finalDate))));
        threads.add(new Thread(() -> data.getUsedItemsInServiceOrders().put(
                "secondPoint", dashboardRepository.findAllUsedItemsByOrderType(
                        ExternalOrderType.SECOND_POINT, currentOffice, initialDate, finalDate))));

        threads.forEach(t -> {
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });


        return data;
    }

    public TechnicianDashboardData getTechnicianData(Technician technician, LocalDate initialDate, LocalDate finalDate) {
        TechnicianDashboardData data = new TechnicianDashboardData();
        data.setInitialDate(initialDate);
        data.setFinalDate(finalDate);

        Technician found = this.technicianService.findById(technician.getId())
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado"));

        BranchOffice currentOffice = branchOfficeService.getCurrentBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada"));

        List<Thread> threads = new ArrayList<>();

        threads.add(new Thread(() -> data.setStockItemsWithMovesCount(
                dashboardRepository.getAllTechniciansStockItemsWithMoveCounts(found, initialDate, finalDate, currentOffice))));

        threads.add(new Thread(() -> data.setPatrimonies(
                dashboardRepository.findAllPatrimoniesByTechnician(found))));

        threads.forEach(t -> {
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });


        return data;
    }

    public TechnicianDashboardData getTechnicianData(LocalDate initialDate, LocalDate finalDate) {
        TechnicianDashboardData data = new TechnicianDashboardData();
        data.setInitialDate(initialDate);
        data.setFinalDate(finalDate);

        BranchOffice currentOffice = branchOfficeService.getCurrentBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada"));

        List<Thread> threads = new ArrayList<>();

        threads.add(new Thread(() -> data.setStockItemsWithMovesCount(
                dashboardRepository.getAllTechniciansStockItemsWithMoveCounts(initialDate, finalDate, currentOffice))));

        threads.add(new Thread(() -> data.setPatrimonies(
                dashboardRepository.findAllPatrimoniesWithTechnicians())));

        threads.forEach(t -> {
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });


        return data;
    }


    public SellerDashboardData getSellersData(LocalDate initialDate, LocalDate finalDate, CityOptions city) {
        SellerDashboardData data = new SellerDashboardData();
        data.setInitialDate(initialDate);
        data.setFinalDate(finalDate);

        Stock stock = stockService.findByCurrentLoggedEmployeeAndBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada"));

        List<Map<String, Object>> mainItems = new ArrayList<>();
        stock.getItems().forEach((stockItem -> {
            Map<String, Object> map = new HashMap<>();
            map.put("item", stockItem.getProduct().getName());
            map.put("quantity", stockItem.getQuantity());
            map.put("currentLevel", setCurrentLevelFromChips(stockItem.getQuantity()));
            map.put("unit", stockItem.getProduct().getUnit().getAbbreviation());
            mainItems.add(map);
        }));

        data.setMainItems(mainItems);

        return data;
    }

    private String setCurrentLevelFromChips(Double quantity) {
        if (quantity <= 3) {
            return QuantityLevel.VERY_LOW.name();
        } else if (quantity <= 5) {
            return QuantityLevel.LOW.name();
        } else if (quantity <= 10) {
            return QuantityLevel.NORMAL.name();
        } else if (quantity <= 20) {
            return QuantityLevel.HIGH.name();
        }
        return QuantityLevel.VERY_HIGH.name();
    }
}
