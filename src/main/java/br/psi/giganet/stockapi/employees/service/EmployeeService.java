package br.psi.giganet.stockapi.employees.service;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.security.Permissions;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.service.AuthUtilsService;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employees;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUtilsService authUtilsService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Autowired
    private RemoteEmployeeService remoteEmployeersService;

    public Optional<Employee> insert(Employee employee) {
        return Optional.of(this.employees.save(employee));
    }

    public List<Employee> findAll() {
        return this.employees.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Employee> findById(Long id) {
        return this.employees.findById(id);
    }

    public Optional<Employee> findByExternalEmployeeByUserId(String userId) {
        return this.employees.findByExternalEmployeeByUserId(userId);
    }

    public Optional<Employee> findByEmail(String email) {
        return this.employees.findByEmail(email);
    }

    public List<Employee> findByNameContaining(String name) {
        return this.employees.findByNameContainingIgnoreCase(name, Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<Employee> findByNameContainingAndPermissions(String name, Permission permission) {
        return this.employees.findByNameContainingAndPermissions(name, permission);
    }

    public Optional<Employee> getCurrentLoggedEmployee() {
        return this.authUtilsService.getCurrentUsername()
                .flatMap(username -> this.employees.findByEmail(username));
    }

    public List<Employee> findByPermission(Permission permission) {
        return this.employees.findByPermission(permission);
    }

    public Boolean isUserRootCurrentLogged() {
        Employee employee = new Employee();
        employee.setPermissions(this.authUtilsService.getCurrentAuthorities());

        return employee.isRoot();
    }

    public Optional<Employee> updatePermissions(Long id, Employee employee) {
        return employees.findById(id)
                .map(e -> {
                    e.setName(employee.getName());
                    e.setPermissions(employee.getPermissions());

                    Set<BranchOffice> newOffices = employee.getBranchOffices()
                            .stream()
                            .map(office -> branchOfficeService.findById(office.getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")))
                            .collect(Collectors.toSet());

                    if (e.getBranchOffices() == null) {
                        e.setBranchOffices(newOffices);
                    } else {
                        e.getBranchOffices().removeIf(office -> !newOffices.contains(office));
                        e.getBranchOffices().addAll(newOffices);
                    }

                    return employees.save(e);
                });
    }

    public void update() {
        remoteEmployeersService.getRemoteEmployeers();
    }
}
