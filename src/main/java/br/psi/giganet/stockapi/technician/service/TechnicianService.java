package br.psi.giganet.stockapi.technician.service;

import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.patrimonies_locations.factory.PatrimonyLocationFactory;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.service.PatrimonyLocationService;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.repository.TechnicianRepository;
import br.psi.giganet.stockapi.technician.service.dto.TechnicianScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TechnicianService {
    public static final String TAG = TechnicianService.class.getCanonicalName();
    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private RemoteTechnicianService remoteTechnicianService;

    @Autowired
    private PatrimonyLocationService patrimonyLocationService;

    @Autowired
    private PatrimonyLocationFactory locationFactory;

    @Autowired
    private BranchOfficeService branchOfficeService;

    public void updateTechniciansList() {
        List<Technician> founds = remoteTechnicianService.getRemoteTechnicians();
        if (founds != null) {
            founds.stream()
                    .forEach(technician -> {
                        Technician saved;

                        Optional<Technician> found = technicianRepository.findById(technician.getId());
                        if (found.isEmpty()) {
                            saved = technicianRepository.save(technician);
                        } else {
                            saved = found.get();
                        }

                        if (saved != null) {
                            Optional<Stock> stock = stockService.findByUser(saved.getUserId());
                            if (stock.isEmpty()) {
                                stockService.save(stockFactory.create(saved));
                            }

                            Optional<PatrimonyLocation> patrimonyLocation = patrimonyLocationService.findByCode(saved.getUserId());
                            if (patrimonyLocation.isEmpty()) {
                                patrimonyLocationService.insert(locationFactory.create(saved));
                            }
                        }

                        Optional<Employee> savedEmployee = employeeService.findByExternalEmployeeByUserId(technician.getUserId());
                        if (savedEmployee.isEmpty()) {
                            employeeService.insert(employeeAdapter.createDefaultUser(technician));
                        }
                    });
        }
    }

    public Optional<Technician> update(String id, Technician technician) {
        return this.technicianRepository.findById(id)
                .map(saved -> {
                    saved.setName(technician.getName());
                    saved.setSector(technician.getSector());
                    saved.setIsActive(technician.getIsActive());
                    saved.setBranchOffice(branchOfficeService.findById(technician.getBranchOffice().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                    stockService.updateStockBranchOffice(saved.getStock().getId(), saved.getBranchOffice());
                    stockService.updateStockVisibility(saved.getStock().getId(), saved.getIsActive());

                    return technicianRepository.save(saved);
                });
    }

    public Optional<Technician> setCurrentBranch(String id, Long branchOffice) {
        return this.technicianRepository.findByUserId(id)
                .map(saved -> {
                    if (saved.getBranchOffice() != null &&
                            saved.getBranchOffice().getId() != null &&
                            !saved.getBranchOffice().getId().equals(branchOffice)
                    ) {
                        saved.setBranchOffice(branchOfficeService.findById(branchOffice)
                                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));
                        stockService.updateStockBranchOffice(saved.getStock().getId(), saved.getBranchOffice());
                    }
                    return technicianRepository.save(saved);
                });
    }

    public Optional<Technician> findByEmail(String email) {
        return this.technicianRepository.findByEmail(email);
    }

    public Optional<Technician> findById(String id) {
        return this.technicianRepository.findById(id);
    }

    public Optional<Technician> findByUserId(String userId) {
        return this.technicianRepository.findByUserId(userId);
    }

    public List<Technician> findAll() {
        return this.technicianRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<TechnicianScheduleDTO> getTechnicianSchedule(TechnicianStock technician, LocalDate initialDate, LocalDate finalDate) {
        return remoteTechnicianService.getTechnicianSchedule(
                stockService.findById(technician.getId())
                        .filter(stock -> stock instanceof TechnicianStock)
                        .map(stock -> ((TechnicianStock) stock).getTechnician())
                        .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado")),
                initialDate,
                finalDate);
    }

    public List<TechnicianScheduleDTO> getTechnicianSchedule(Technician technician, LocalDate initialDate, LocalDate finalDate) {
        return remoteTechnicianService.getTechnicianSchedule(
                technicianRepository.findById(technician.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado")),
                initialDate,
                finalDate);
    }

}
