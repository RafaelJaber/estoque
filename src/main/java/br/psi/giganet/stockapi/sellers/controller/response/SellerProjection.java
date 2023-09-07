package br.psi.giganet.stockapi.sellers.controller.response;

import br.psi.giganet.stockapi.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import lombok.Data;

@Data
public class SellerProjection {

    private String id;
    private String name;
    private String email;
    private String userId;
    private BranchOfficeProjection branchOffice;
    private StockProjection stock;
    private Boolean isActive;
}
