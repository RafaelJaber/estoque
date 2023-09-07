package br.psi.giganet.stockapi.sellers.factory;

import br.psi.giganet.stockapi.common.factory.AbstractFactory;
import br.psi.giganet.stockapi.sellers.model.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerFactory extends AbstractFactory<Seller> {

    public Seller create(String id, String userId, String name, String email, String sellerId, Boolean active) {
        Seller seller = new Seller();
        seller.setId(id);
        seller.setEmail(email);
        seller.setName(name);
        seller.setUserId(userId);
        seller.setIsActive(active);
        seller.setSellerId(sellerId);

        return seller;
    }
}
