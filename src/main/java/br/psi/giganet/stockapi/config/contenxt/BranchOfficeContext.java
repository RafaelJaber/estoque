package br.psi.giganet.stockapi.config.contenxt;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;

public class BranchOfficeContext {

    private static final ThreadLocal<BranchOffice> currentBranchOffice = new InheritableThreadLocal<>();

    public static BranchOffice getCurrentBranchOffice() {
        return currentBranchOffice.get();
    }

    public static void setCurrentBranchOffice(BranchOffice office) {
        currentBranchOffice.set(office);
    }

    public static void clear() {
        currentBranchOffice.set(null);
    }

}
