package br.psi.giganet.stockapi.branch_offices.factory;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import org.springframework.stereotype.Component;

@Component
public class BranchOfficeFactory {

    public BranchOffice create(Long id) {
        BranchOffice branchOffice = new BranchOffice();
        branchOffice.setId(id);

        return branchOffice;
    }


}
