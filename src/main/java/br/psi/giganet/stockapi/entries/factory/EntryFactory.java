package br.psi.giganet.stockapi.entries.factory;

import br.psi.giganet.stockapi.entries.model.EntryItem;
import org.springframework.stereotype.Component;

@Component
public class EntryFactory {

    public EntryItem create(Long id) {
        EntryItem item = new EntryItem();
        item.setId(id);

        return item;
    }

}
