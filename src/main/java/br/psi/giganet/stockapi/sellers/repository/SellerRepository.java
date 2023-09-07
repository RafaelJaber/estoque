package br.psi.giganet.stockapi.sellers.repository;

import br.psi.giganet.stockapi.sellers.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {
}
