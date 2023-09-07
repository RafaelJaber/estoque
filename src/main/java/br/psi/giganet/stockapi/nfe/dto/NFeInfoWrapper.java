package br.psi.giganet.stockapi.nfe.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NFeInfoWrapper {

    @XmlAttribute(name = "Id")
    private String id;
    @XmlElement(name = "emit")
    private NFeEmitter emitter;
    @XmlElement(name = "ide")
    private NFeIdentificationInfo identification;
    @XmlElement(name = "det")
    private List<NFeItem> items;
    @XmlElement(name = "total")
    private NFeTotal totals;

}
