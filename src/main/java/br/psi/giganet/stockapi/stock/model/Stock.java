package br.psi.giganet.stockapi.stock.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stocks")
public class Stock extends AbstractModel {

    @NotEmpty
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StockType type;

    /**
     * Reference to userId on Smartnet
     */
    @Column(name = "userId", unique = true)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stocks_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id")
    private BranchOffice branchOffice;

    @NotNull
    private Boolean isVisible;

    @OneToMany(
            mappedBy = "stock",
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<StockItem> items;

    @JsonIgnore
    @Transactional
    public Optional<StockItem> find(Product product) {
        return this.items == null ? Optional.empty() :
                this.items.stream().filter(p -> p.getProduct().equals(product)).findFirst();
    }

    public boolean isShed(){
        return type.equals(StockType.SHED);
    }

    public boolean isCustomer(){
        return type.equals(StockType.CUSTOMER);
    }

    public boolean isTechnician(){
        return type.equals(StockType.TECHNICIAN);
    }

    public boolean isMaintenance(){
        return type.equals(StockType.MAINTENANCE);
    }
}
