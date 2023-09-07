package br.psi.giganet.stockapi.nfe.service;

import br.psi.giganet.stockapi.nfe.adapter.NFeAdapter;
import br.psi.giganet.stockapi.nfe.dto.NFeFile;
import br.psi.giganet.stockapi.nfe.model.NFeContent;
import br.psi.giganet.stockapi.nfe.model.NFeEntryItem;
import br.psi.giganet.stockapi.nfe.repository.NFeContentRepository;
import br.psi.giganet.stockapi.nfe.repository.NFeEntryItemRepository;
import br.psi.giganet.stockapi.nfe.util.NFeReader;
import br.psi.giganet.stockapi.nfe.util.NFeReaderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Optional;

@Service
public class NFeService {

    @Autowired
    private NFeContentRepository nFeContents;

    @Autowired
    private NFeEntryItemRepository nFeEntryItems;

    @Autowired
    private NFeAdapter adapter;

    public Optional<NFeContent> findByAccessCode(String accessCode) {
        return nFeContents.findByAccessCode(accessCode);
    }

    public Optional<NFeEntryItem> findByProductAndSupplierAndDocumentProductCode(Long product, String supplierCNPJ, String documentProductCode) {
        return nFeEntryItems.findByProductAndSupplierAndDocumentProductCode(product, supplierCNPJ, documentProductCode);
    }

    public NFeFile readXMLFile(MultipartFile file) throws IOException, JAXBException {
        NFeReaderResult result = NFeReader.readFile(file.getInputStream());
        this.save(adapter.transform(result.getFile(), result.getXmlContent()));

        return result.getFile();
    }

    public NFeContent save(NFeContent content) {
        if (!nFeContents.existsByAccessCode(content.getAccessCode())) {
            return nFeContents.save(content);
        } else {
            return nFeContents.findByAccessCode(content.getAccessCode()).get();
        }
    }

}
