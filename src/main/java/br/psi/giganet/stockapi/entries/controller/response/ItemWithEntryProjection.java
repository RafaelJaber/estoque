package br.psi.giganet.stockapi.entries.controller.response;

import lombok.Data;

@Data
public class ItemWithEntryProjection {

    private Long id;
    private Long entry;
    private String fiscalDocumentNumber;

}
