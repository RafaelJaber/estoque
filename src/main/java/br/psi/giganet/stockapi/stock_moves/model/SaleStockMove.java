package br.psi.giganet.stockapi.stock_moves.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "sales_stock_moves")
public class SaleStockMove extends StockMove {

}
