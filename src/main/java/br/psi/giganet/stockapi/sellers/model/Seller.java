package br.psi.giganet.stockapi.sellers.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import br.psi.giganet.stockapi.stock.model.SellerStock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sellers")
public class Seller extends AbstractExternalModel {

    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;

    private String userId;
    private String sellerId;

    @NotNull
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_seller_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @OneToOne(mappedBy = "seller", fetch = FetchType.LAZY)
    private SellerStock stock;
}