package br.psi.giganet.stockapi.branch_offices.controller;

import br.psi.giganet.stockapi.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeResponse;
import br.psi.giganet.stockapi.branch_offices.controller.security.RoleBranchOfficesRead;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/branch-offices")
public class BranchOfficeController {

    @Autowired
    private BranchOfficeService branchOfficeService;
    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    @GetMapping
    @RoleBranchOfficesRead
    public List<BranchOfficeResponse> findAll() {
        return this.branchOfficeService.findAll()
                .stream()
                .map(branchOfficeAdapter::transformToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/available")
    public List<BranchOfficeResponse> findAllAvailableByCurrentEmployee() {
        return this.branchOfficeService.findAllAvailableByCurrentEmployee()
                .stream()
                .map(branchOfficeAdapter::transformToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleBranchOfficesRead
    public BranchOfficeResponse findById(@PathVariable Long id) {
        return this.branchOfficeService.findById(id)
                .map(branchOfficeAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Filial não encontrada"));
    }

}
