package br.psi.giganet.stockapi.branch_offices.adapter;

import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeResponse;
import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BranchOfficeAdapter {

    @Autowired
    private StockAdapter stockAdapter;

    public BranchOfficeResponse transformToResponse(BranchOffice branchOffice) {
        BranchOfficeResponse response = new BranchOfficeResponse();
        response.setName(branchOffice.getName());
        response.setId(branchOffice.getId());
        response.setCity(branchOffice.getCity());
        response.setShed(branchOffice.shed().map(stockAdapter::transform).orElse(null));
        response.setMaintenance(branchOffice.maintenance().map(stockAdapter::transform).orElse(null));
        response.setObsolete(branchOffice.obsolete().map(stockAdapter::transform).orElse(null));
        response.setDefective(branchOffice.defective().map(stockAdapter::transform).orElse(null));
        response.setCustomer(branchOffice.customer().map(stockAdapter::transform).orElse(null));

        return response;
    }

    public BranchOfficeProjection transform(BranchOffice branchOffice) {
        BranchOfficeProjection response = new BranchOfficeProjection();
        response.setId(branchOffice.getId());
        response.setName(branchOffice.getName());
        response.setCity(branchOffice.getCity());

        return response;
    }

}
