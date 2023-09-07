package br.psi.giganet.stockapi.nfe.controller;

import br.psi.giganet.stockapi.nfe.dto.NFeFile;
import br.psi.giganet.stockapi.nfe.service.NFeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@RestController
@RequestMapping("fiscal-notes")
public class NFeController {

    @Autowired
    private NFeService nFeService;

    @PostMapping("/uploads")
    public NFeFile insertPurchaseRecord(@RequestBody MultipartFile file) throws IOException, JAXBException {
        return nFeService.readXMLFile(file);
    }

}
