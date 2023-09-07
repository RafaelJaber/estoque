package br.psi.giganet.stockapi.sellers.adapter;

import br.psi.giganet.stockapi.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.stockapi.common.adapter.AbstractAdapter;
import br.psi.giganet.stockapi.sellers.controller.response.SellerProjection;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.stock.adapter.StockAdapter;
import br.psi.giganet.stockapi.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellerAdapter extends AbstractAdapter<Seller> {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockAdapter stockAdapter;

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    public SellerProjection transformToProjection(Seller seller) {
        SellerProjection projection = transformToProjection(SellerProjection.class, seller);
        projection.setBranchOffice(seller.getBranchOffice() != null ?
                branchOfficeAdapter.transform(seller.getBranchOffice()) : null);

        stockService.findSellerStockByUserId(seller.getUserId())
                .ifPresent(sellerStock -> projection.setStock(stockAdapter.transform(sellerStock)));

        return projection;
    }
}
