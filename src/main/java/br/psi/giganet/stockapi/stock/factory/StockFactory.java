package br.psi.giganet.stockapi.stock.factory;

import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.stock.model.*;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.technician.factory.TechnicianFactory;
import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
public class StockFactory {

    @Autowired
    private TechnicianFactory technicianFactory;

    public Stock create(Long id, StockType type) {
        Stock stock;
        switch (type) {
            case SHED:
                stock = new ShedStock();
                break;

            case DEFECTIVE:
                stock = new DefectiveStock();
                break;

            case MAINTENANCE:
                stock = new MaintenanceStock();
                break;

            case OBSOLETE:
                stock = new ObsoleteStock();
                break;

            case TECHNICIAN:
                stock = new TechnicianStock();
                break;

            case CUSTOMER:
                stock = new CustomerStock();
                break;

            default:
                return new Stock();
        }
        stock.setId(id);
        stock.setType(type);

        return stock;
    }

    public Stock create(Long id) {
        Stock stock = new Stock();
        stock.setId(id);

        return stock;
    }

    public TechnicianStock create(String userId) {
        TechnicianStock stock = new TechnicianStock();
        stock.setTechnician(technicianFactory.create(userId));
        stock.setType(StockType.TECHNICIAN);
        stock.setIsVisible(Boolean.TRUE);
        stock.setUserId(userId);

        return stock;
    }

    public StockItem createItem(Stock stock, Product product) {
        StockItem stockItem = new StockItem();
        stockItem.setStock(stock);
        stockItem.setProduct(product);

        return stockItem;
    }

    public StockItem createItem(Stock stock, Product product, Double maxQuantity, BigDecimal price) {
        return new StockItem(stock, product, null, null,
                0d, 0d, 1d, maxQuantity, price, QuantityLevel.UNDEFINED, new ArrayList<>());
    }

    public TechnicianStock create(Technician technician) {
        TechnicianStock stock = new TechnicianStock();
        stock.setName("TECNICO - " + technician.getName().toUpperCase() + " - " + technician.getId());
        stock.setType(StockType.TECHNICIAN);
        stock.setIsVisible(Boolean.TRUE);
        stock.setTechnician(technician);
        stock.setUserId(technician.getUserId());

        return stock;
    }

    public SellerStock create(Seller seller) {
        SellerStock stock = new SellerStock();
        stock.setName("VENDEDOR - " + seller.getName().toUpperCase() + " - " + seller.getId());
        stock.setType(StockType.TECHNICIAN);
        stock.setIsVisible(Boolean.TRUE);
        stock.setUserId(seller.getUserId());
        stock.setSeller(seller);
        stock.setBranchOffice(seller.getBranchOffice());
        return stock;
    }

}
