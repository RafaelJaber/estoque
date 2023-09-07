package br.psi.giganet.stockapi.technician.adapter;

import br.psi.giganet.stockapi.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.stockapi.branch_offices.factory.BranchOfficeFactory;
import br.psi.giganet.stockapi.technician.controller.request.UpdateTechnicianRequest;
import br.psi.giganet.stockapi.technician.controller.response.TechnicianProjection;
import br.psi.giganet.stockapi.technician.controller.response.TechnicianResponse;
import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TechnicianAdapter {

    @Autowired
    private BranchOfficeFactory branchOfficeFactory;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    public Technician transform(UpdateTechnicianRequest request) {
        Technician technician = new Technician();
        technician.setId(request.getId());
        technician.setName(request.getName());
        technician.setSector(request.getSector());
        technician.setIsActive(request.getIsActive());
        technician.setBranchOffice(branchOfficeFactory.create(request.getBranchOffice()));

        return technician;
    }

    public TechnicianProjection transformToProjection(Technician technician) {
        TechnicianProjection projection = new TechnicianProjection();
        projection.setId(technician.getId());
        projection.setEmail(technician.getEmail());
        projection.setName(technician.getName());
        projection.setUserId(technician.getUserId());
        projection.setSector(technician.getSector());
        projection.setBranchOffice(technician.getBranchOffice() != null ?
                branchOfficeAdapter.transform(technician.getBranchOffice()) : null);

        return projection;
    }
    public TechnicianProjection transformToResponse(Technician technician) {
        TechnicianResponse response = new TechnicianResponse();
        response.setId(technician.getId());
        response.setEmail(technician.getEmail());
        response.setName(technician.getName());
        response.setUserId(technician.getUserId());
        response.setSector(technician.getSector());
        response.setIsActive(technician.getIsActive());
        response.setBranchOffice(technician.getBranchOffice() != null ?
                branchOfficeAdapter.transform(technician.getBranchOffice()) : null);

        return response;
    }

}
