package br.psi.giganet.stockapi.config.security.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity(name = "Permission")
public class Permission {

    @Id
    @EqualsAndHashCode.Include
    private String name;

}
