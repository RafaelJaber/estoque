package br.psi.giganet.stockapi.stock.repository;

import br.psi.giganet.stockapi.products.model.Product;

import java.math.BigDecimal;

public interface GeneralStockItem {

    Product getProduct();
    Double getQuantity();
    BigDecimal getPrice();

}
