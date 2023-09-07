package br.psi.giganet.stockapi.dashboard.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.dashboard.repository.factory.DashboardSqlFactory;
import br.psi.giganet.stockapi.dashboard.repository.factory.TechnicianDashboardSqlFactory;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock_moves.model.ExternalOrderType;
import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
@SuppressWarnings("unchecked")
public class DashboardRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Map<String, Object>> findAllUsedItemsByOrderType(
            ExternalOrderType orderType,
            BranchOffice branchOffice,
            LocalDate initialDate,
            LocalDate finalDate) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.findAllUsedItemsByOrderType(orderType, branchOffice, initialDate, finalDate))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllMainItems(Employee employee, BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.findAllMainItems(employee, branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);
                    map.put("currentLevel", resp[2]);
                    map.put("unit", resp[3]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllMainItemsWithQuantityInShedAndTechnicianAndMaintenance(Employee employee, BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.findAllMainItemsWithQuantityInShedAndTechnicianAndMaintenance(employee, branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("shed", resp[1]);
                    map.put("maintenance", resp[2]);
                    map.put("technician", resp[3]);
                    map.put("total", resp[4]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllEntryItemsWithQuantityByDate(LocalDate initialDate, LocalDate finalDate, BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.findAllEntryItemsWithQuantityByDate(initialDate, finalDate, branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllObsoleteItems(BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.findAllObsoleteItems(branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllStockItemsInShedTechnicianMaintenanceObsoleteDefective(BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.findAllStockItemsInShedTechnicianMaintenanceObsoleteDefective(branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> totalsByTechnicianStocks(BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                DashboardSqlFactory.totalsByTechnicianStocks(branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("technician", resp[0]);
                    map.put("total", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllTechniciansStockItemsWithMoveCounts(LocalDate initialDate, LocalDate finalDate, BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                TechnicianDashboardSqlFactory.getAllStockItemsWithMoveCounts(initialDate, finalDate, branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);
                    map.put("replacement", resp[2]);
                    map.put("retreat", resp[3]);
                    map.put("devolution", resp[4]);
                    map.put("installation", resp[5]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllTechniciansStockItemsWithMoveCounts(
            Technician technician,
            LocalDate initialDate,
            LocalDate finalDate,
            BranchOffice branchOffice) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                TechnicianDashboardSqlFactory.getAllStockItemsWithMoveCounts(technician.getId(), initialDate, finalDate, branchOffice))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("item", resp[0]);
                    map.put("quantity", resp[1]);
                    map.put("replacement", resp[2]);
                    map.put("retreat", resp[3]);
                    map.put("devolution", resp[4]);
                    map.put("installation", resp[5]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllPatrimoniesByTechnician(Technician technician) {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                TechnicianDashboardSqlFactory.findAllPatrimoniesByTechnician(technician.getUserId()))
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("code", resp[0]);
                    map.put("item", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findAllPatrimoniesWithTechnicians() {
        return (List<Map<String, Object>>) entityManager.createNativeQuery(
                TechnicianDashboardSqlFactory.findAllPatrimoniesWithTechnicians())
                .getResultStream()
                .map(e -> {
                    Object[] resp = (Object[]) e;

                    Map<String, Object> map = new HashMap<>();
                    map.put("code", resp[0]);
                    map.put("item", resp[1]);

                    return map;
                })
                .collect(Collectors.toList());
    }

}
