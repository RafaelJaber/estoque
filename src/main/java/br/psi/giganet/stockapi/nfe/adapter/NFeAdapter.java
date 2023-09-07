package br.psi.giganet.stockapi.nfe.adapter;

import br.psi.giganet.stockapi.nfe.dto.NFeFile;
import br.psi.giganet.stockapi.nfe.model.NFeContent;
import org.springframework.stereotype.Component;

@Component
public class NFeAdapter {


    public NFeContent transform(NFeFile file, String xmlContent){
        NFeContent content = new NFeContent();
        content.setAccessCode(file.getWrapper().getInfo().getId());
        content.setContent(xmlContent);

        return content;
    }

}
