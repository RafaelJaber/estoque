package br.psi.giganet.stockapi.nfe.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NFeItem {

    @XmlAttribute(name = "nItem")
    private Long id;

    @XmlElement(name = "prod")
    private NFeProduct product;

}
