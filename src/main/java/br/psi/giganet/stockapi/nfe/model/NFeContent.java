package br.psi.giganet.stockapi.nfe.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "util_nfe")
public class NFeContent extends AbstractModel {

    @Column(unique = true, nullable = false)
    private String accessCode;
    @NotEmpty
    @Lob
    private String content;

}
