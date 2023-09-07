package br.psi.giganet.stockapi.nfe.util;

import br.psi.giganet.stockapi.nfe.dto.NFeFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NFeReaderResult {

    private String xmlContent;
    private NFeFile file;

}
