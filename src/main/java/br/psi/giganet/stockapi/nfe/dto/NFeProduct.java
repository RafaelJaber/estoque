package br.psi.giganet.stockapi.nfe.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NFeProduct {

    @XmlElement(name = "cProd")
    private String code;
    @XmlElement(name = "cEAN")
    private String gtim;
    @XmlElement(name = "xProd")
    private String name;
    @XmlElement(name = "qCom")
    private Double quantity;
    @XmlElement(name = "uCom")
    private String unit;
    @XmlElement(name = "vUnCom")
    private BigDecimal unitPrice;
    @XmlElement(name = "vProd")
    private BigDecimal total;

}
