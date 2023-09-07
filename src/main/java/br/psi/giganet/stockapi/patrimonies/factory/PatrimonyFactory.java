package br.psi.giganet.stockapi.patrimonies.factory;

import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import org.springframework.stereotype.Component;

@Component
public class PatrimonyFactory {

    public Patrimony create(String code) {
        Patrimony p = new Patrimony();
        p.setCode(code);
        return p;
    }

    public Patrimony create(Long id) {
        Patrimony p = new Patrimony();
        p.setId(id);

        return p;
    }

    public PatrimonyMove create(Patrimony patrimony, PatrimonyLocation to, Employee responsible){
        PatrimonyMove move = new PatrimonyMove();
        move.setResponsible(responsible);
        move.setPatrimony(patrimony);
        move.setTo(to);

        return move;
    }

    public PatrimonyMove create(Patrimony patrimony, PatrimonyLocation to){
        PatrimonyMove move = new PatrimonyMove();
        move.setPatrimony(patrimony);
        move.setTo(to);

        return move;
    }
}
