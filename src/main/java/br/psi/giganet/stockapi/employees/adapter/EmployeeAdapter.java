package br.psi.giganet.stockapi.employees.adapter;

import br.psi.giganet.stockapi.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.stockapi.branch_offices.factory.BranchOfficeFactory;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.security.Permissions;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.employees.controller.request.InsertEmployeeRequest;
import br.psi.giganet.stockapi.employees.controller.request.UpdatePermissionsRequest;
import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjectionWithEmail;
import br.psi.giganet.stockapi.employees.controller.response.EmployeeResponse;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmployeeAdapter {

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Autowired
    private BranchOfficeFactory branchOfficeFactory;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    public Employee create(Long id) {
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    public Employee transform(InsertEmployeeRequest request) {
        Employee e = new Employee();
        e.setName(request.getName());
        e.setPassword(request.getPassword());
        e.setEmail(request.getEmail());
        return e;
    }

    public Employee transform(UpdatePermissionsRequest request) {
        Employee e = new Employee();
        e.setId(request.getId());
        e.setName(request.getName());
        e.setPermissions(
                request.getPermissions()
                        .stream().map(Permission::new)
                        .collect(Collectors.toSet()));
        e.setBranchOffices(
                request.getBranchOffices()
                        .stream().map(branchOfficeFactory::create)
                        .collect(Collectors.toSet()));

        return e;
    }

    public EmployeeResponse transformToResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setCreatedDate(employee.getCreatedDate());
        response.setLastModifiedDate(employee.getLastModifiedDate());
        response.setEmail(employee.getEmail());
        response.setName(employee.getName());
        response.setPermissions(
                employee.getPermissions() != null ?
                        employee.getPermissions()
                                .stream()
                                .map(Permission::getName)
                                .collect(Collectors.toSet()) :
                        Collections.emptySet());
        response.setBranchOffices(
                employee.getBranchOffices() != null ?
                        employee.getBranchOffices()
                                .stream()
                                .map(branchOfficeAdapter::transform)
                                .collect(Collectors.toSet()) :
                        Collections.emptySet());

        return response;
    }

    public EmployeeProjection transform(Employee employee) {
        EmployeeProjection projection = new EmployeeProjection();
        projection.setName(employee.getName());
        projection.setId(employee.getId());
        return projection;
    }

    public EmployeeProjection transformWithEmail(Employee employee) {
        EmployeeProjectionWithEmail projection = new EmployeeProjectionWithEmail();
        projection.setName(employee.getName());
        projection.setId(employee.getId());
        projection.setEmail(employee.getEmail());
        return projection;
    }

    public Employee createDefaultUser(String userId, String name, String email, String password) {
        Employee e = new Employee();
        e.setName(name);
        e.setEmail(email);
        e.setUserId(userId);
        e.setPassword(new BCryptPasswordEncoder().encode(password));
        e.setPermissions(new HashSet<>(Collections.singletonList(new Permission("ROLE_ADMIN"))));

        return e;
    }

    public Employee createDefaultUser(Seller seller) {
        Employee employee = new Employee();
        employee.setName(seller.getName());
        employee.setEmail(seller.getEmail());
        employee.setUserId(seller.getUserId());
        employee.setPassword(new BCryptPasswordEncoder().encode("DEFAULT_PASSWORD_-Wg6v7G^e*Z^kv?V"));
        employee.setPermissions(new HashSet<>(Arrays.asList(
                new Permission(Permissions.ROLE_NOTIFICATIONS.name()),
                new Permission(Permissions.ROLE_NOTIFICATIONS_STOCK_ITEM_LOW_LEVEL.name()),
                new Permission(Permissions.ROLE_NOTIFICATIONS_STOCK_ITEM_VERY_LOW_LEVEL.name()),
                new Permission(Permissions.ROLE_SALES_MODULE.name()),
                new Permission(Permissions.ROLE_PRODUCTS_READ.name()))));

        employee.setBranchOffices(new HashSet<>(
                Collections.singletonList(Optional.of(seller.getBranchOffice())
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")))));

        return employee;
    }

    public Employee createDefaultUser(Technician technician) {
        Employee e = new Employee();
        e.setName(technician.getName());
        e.setEmail(technician.getEmail());
        e.setUserId(technician.getUserId());
        e.setPassword(new BCryptPasswordEncoder().encode("DEFAULT_PASSWORD_-Wg6v7G^e*Z^kv?V"));
        e.setPermissions(new HashSet<>(Arrays.asList(
                new Permission("ROLE_ADMIN"),
                new Permission("ROLE_UNITS_READ"),
                new Permission("ROLE_PRODUCTS_READ"),
                new Permission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                new Permission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                new Permission("ROLE_MOVES_READ"),
                new Permission("ROLE_PATRIMONIES_LOCATIONS_READ"),
                new Permission("ROLE_STOCKS_READ"),
                new Permission("ROLE_PATRIMONIES_WRITE"),
                new Permission("ROLE_PATRIMONIES_READ"))));

        return e;
    }


}
