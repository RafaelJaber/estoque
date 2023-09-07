package br.psi.giganet.stockapi.nfe.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NFeEmitter {

    @XmlElement(name = "CNPJ")
    private String cnpj;
    @XmlElement(name = "IE")
    private String stateRegistration;
    @XmlElement(name = "IM")
    private String municipalRegistration;
    @XmlElement(name = "CPF")
    private String cpf;
    @XmlElement(name = "xNome")
    private String corporateName;
    @XmlElement(name = "xFant")
    private String name;

}
