package br.psi.giganet.stockapi.config.security.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Users")
public class User extends AbstractModel {

    @Column(unique = true, nullable = false)
    private String email;
    @NotNull
    private String password;

    @Column(unique = true)
    private String userId;

    @ManyToMany(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(name = "Users_has_Permissions",
            joinColumns = @JoinColumn(name = "User_id", foreignKey = @ForeignKey(name = "fk_Users_has_Permissions_user")),
            inverseJoinColumns = @JoinColumn(name = "Permission_id", foreignKey = @ForeignKey(name = "fk_Users_has_Permissions_permission")))
    private Set<Permission> permissions;

    @Transactional
    public Boolean hasRole(Permission permission) {
        return permission != null && permissions != null &&
                permissions.contains(permission);
    }

    @Transactional
    public Boolean hasRole(String permission) {
        return hasRole(new Permission(permission));
    }

    public Boolean isCurrentUserLogged() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && authentication != null) {
            return this.getEmail().equalsIgnoreCase(authentication.getName());
        }

        return Boolean.FALSE;
    }

    @Transactional
    public Boolean hasAnyRole(List<Permission> permissions) {
        return permissions != null && this.permissions != null &&
                permissions.stream().anyMatch(p -> this.permissions.contains(p));
    }

    public Boolean isRoot() {
        return this.hasRole(new Permission("ROLE_ROOT"));
    }

}
