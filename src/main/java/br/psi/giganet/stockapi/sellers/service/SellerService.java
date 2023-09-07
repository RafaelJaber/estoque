package br.psi.giganet.stockapi.sellers.service;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.security.Permissions;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.service.PermissionService;
import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.repository.EmployeeRepository;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.patrimonies_locations.factory.PatrimonyLocationFactory;
import br.psi.giganet.stockapi.patrimonies_locations.service.PatrimonyLocationService;
import br.psi.giganet.stockapi.sellers.controller.request.SellerCreateRequest;
import br.psi.giganet.stockapi.sellers.controller.request.SellerUpdateRequest;
import br.psi.giganet.stockapi.sellers.factory.SellerFactory;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.sellers.repository.SellerRepository;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.SellerStock;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private RemoteSellerService remoteSellerService;

    @Autowired
    private SellerFactory sellerFactory;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PatrimonyLocationService patrimonyLocationService;

    @Autowired
    private PatrimonyLocationFactory locationFactory;

    public Optional<Seller> findById(String id) {
        return this.sellerRepository.findById(id);
    }

    public List<Seller> findAll() {
        return this.sellerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Seller> save(SellerCreateRequest request) {
        Seller seller = sellerFactory.create(request);
        seller.setBranchOffice(branchOfficeService.findById(request.getBranchOffice())
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

        stockService.updateStockBranchOffice(seller.getStock().getId(), seller.getBranchOffice());
        stockService.updateStockVisibility(seller.getStock().getId(), seller.getIsActive());

        return Optional.of(this.sellerRepository.save(seller));
    }

    public Optional<Seller> update(String id, SellerUpdateRequest request) {
        return this.sellerRepository.findById(id)
            .map((seller) -> {
                seller = sellerFactory.merge(seller, request);
                seller.setBranchOffice(branchOfficeService.findById(request.getBranchOffice())
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                stockService.updateStockBranchOffice(seller.getStock().getId(), seller.getBranchOffice());
                stockService.updateStockVisibility(seller.getStock().getId(), seller.getIsActive());
                return sellerRepository.save(seller);
            });
    }

    public void update() {
        if (permissionService.findById(Permissions.ROLE_SALES_MODULE.name()).isEmpty()) {
            permissionService.save(Permissions.ROLE_SALES_MODULE);
        }

        BranchOffice branchOffice = branchOfficeService.getCurrentBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada"));

        remoteSellerService.getRemoteEmployeeSellers()
                .forEach(seller -> {
                    Optional<Seller> persistedSeller = sellerRepository.findById(seller.getId());
                    if (persistedSeller.isEmpty()) {
                        seller.setBranchOffice(branchOffice);
                        seller = sellerRepository.save(seller);
                    }

                    Optional<Technician> persistedTechinician = technicianRepository.findByEmail(seller.getEmail());
                    if (persistedTechinician.isEmpty()) {
                        stockService.save(stockFactory.create(seller));
                        patrimonyLocationService.insert(locationFactory.create(seller));
                    }

                    Optional<Employee> persistedEmployee = employeeService.findByEmail(seller.getEmail());
                    if (persistedEmployee.isEmpty()) {
                        employeeService.insert(employeeAdapter.createDefaultUser(seller));
                    } else {
                        Employee employee = persistedEmployee.get();
                        employee.getPermissions()
                                .add(new Permission(Permissions.ROLE_SALES_MODULE.name()));
                        employeeRepository.save(employee);
                    }

                });
    }
}
