package br.psi.giganet.stockapi.patrimonies_locations.factory;

import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.stereotype.Component;

@Component
public class PatrimonyLocationFactory {

    public PatrimonyLocation create(Technician technician) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setName("TECNICO - " + technician.getName() + " - " + technician.getId());
        location.setCode(technician.getUserId());
        location.setType(PatrimonyLocationType.TECHNICIAN);

        return location;
    }

    public PatrimonyLocation create(Seller seller) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setName("VENDEDOR - " + seller.getName() + " - " + seller.getId());
        location.setCode(seller.getUserId());
        location.setType(PatrimonyLocationType.TECHNICIAN);

        return location;
    }

    public PatrimonyLocation create(Long id) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setId(id);

        return location;
    }

    public PatrimonyLocation create(String code) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setCode(code);

        return location;
    }

    public PatrimonyLocation create(String code, String name, PatrimonyLocationType type) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setCode(code);
        location.setName(name);
        location.setType(type);

        return location;
    }

}
