package br.psi.giganet.stockapi.entries.controller.response;

import lombok.Data;

@Data
public class EntryItemWithMetaDataProjection {

    private EntryItemProjection entryItem;
    private Double registeredPatrimonies;

}
