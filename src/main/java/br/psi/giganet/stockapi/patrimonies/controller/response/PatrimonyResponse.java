package br.psi.giganet.stockapi.patrimonies.controller.response;

import br.psi.giganet.stockapi.entries.controller.response.ItemWithEntryProjection;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import lombok.Data;

import java.util.List;

@Data
public class PatrimonyResponse {

    private Long id;
    private String code;
    private PatrimonyCodeType codeType;
    private ProductProjection product;
    private PatrimonyLocationProjection currentLocation;
    private String note;
    private ItemWithEntryProjection entryItem;
    private List<PatrimonyMoveProjection> moves;

}
