package br.psi.giganet.stockapi.stock.repository;

import br.psi.giganet.stockapi.stock.model.StockItemQuantityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockItemQuantityLevelRepository extends JpaRepository<StockItemQuantityLevel, Long> {
}
