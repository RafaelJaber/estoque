package br.psi.giganet.stockapi.branch_offices.service;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.repository.BranchOfficeRepository;
import br.psi.giganet.stockapi.config.contenxt.BranchOfficeContext;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchOfficeService {

    @Autowired
    private BranchOfficeRepository branchOfficeRepository;

    @Autowired
    private EmployeeService employeeService;

    public List<BranchOffice> findAll() {
        return branchOfficeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<BranchOffice> findAllAvailableByCurrentEmployee() {
        return branchOfficeRepository.findAllByEmployee(
                employeeService.getCurrentLoggedEmployee()
                        .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")),
                Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<BranchOffice> insert(BranchOffice branchOffice) {
        return Optional.of(branchOfficeRepository.save(branchOffice));
    }

    public Optional<BranchOffice> update(Long id, BranchOffice branchOffice) {
        return this.findById(id)
                .map(saved -> {
                    saved.setName(branchOffice.getName());
                    return this.branchOfficeRepository.save(saved);
                });
    }

    public Optional<BranchOffice> findById(Long id) {
        return this.branchOfficeRepository.findById(id);
    }

    public Optional<BranchOffice> getCurrentBranchOffice() {
        BranchOffice office = BranchOfficeContext.getCurrentBranchOffice();
        return office != null ? findById(office.getId()) : Optional.empty();
    }
}
