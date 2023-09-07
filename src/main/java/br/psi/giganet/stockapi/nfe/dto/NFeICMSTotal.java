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
public class NFeICMSTotal {

    @XmlElement(name = "vNF")
    private BigDecimal total;
    @XmlElement(name = "vTotTrib")
    private BigDecimal tributes;
    @XmlElement(name = "vOutro")
    private BigDecimal othersCosts;
    @XmlElement(name = "vProd")
    private BigDecimal items;
    @XmlElement(name = "vFrete")
    private BigDecimal transport;
    @XmlElement(name = "vDesc")
    private BigDecimal discount;

}
