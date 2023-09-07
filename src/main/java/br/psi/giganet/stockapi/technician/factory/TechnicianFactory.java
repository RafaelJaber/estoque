package br.psi.giganet.stockapi.technician.factory;

import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.stereotype.Component;

@Component
public class TechnicianFactory {

    public Technician create(String id, String userId, String name, String email, String technicianId, Boolean active) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setEmail(email);
        technician.setName(name);
        technician.setUserId(userId);
        technician.setTechnicianId(technicianId);
        technician.setIsActive(active);

        return technician;
    }

    public Technician create(String userId) {
        Technician technician = new Technician();
        technician.setUserId(userId);

        return technician;
    }

    public Technician createById(String id) {
        Technician technician = new Technician();
        technician.setId(id);

        return technician;
    }

}
