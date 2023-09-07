package br.psi.giganet.stockapi.technician.controller;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.controller.security.RoleRoot;
import br.psi.giganet.stockapi.technician.adapter.TechnicianAdapter;
import br.psi.giganet.stockapi.technician.controller.request.CurrentBranchTechnicianRequest;
import br.psi.giganet.stockapi.technician.controller.response.TechnicianProjection;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/basic/technicians")
public class BasicTechnicianController {
    public static final String TAG = BasicTechnicianController.class.getCanonicalName();
    @Autowired
    private TechnicianService technicianService;
    @Autowired
    private TechnicianAdapter technicianAdapter;

    @PutMapping("/{id}/change/branch")
    public TechnicianProjection branch(@PathVariable String id, @Valid @RequestBody CurrentBranchTechnicianRequest request) {
        return this.technicianService.setCurrentBranch(id, request.getBranchOffice())
                .map(technicianAdapter::transformToProjection)
                .orElseThrow(() -> new IllegalArgumentException("Técnico não foi encontrado"));
    }
}
