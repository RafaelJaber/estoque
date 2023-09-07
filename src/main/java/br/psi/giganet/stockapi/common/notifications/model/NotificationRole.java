package br.psi.giganet.stockapi.common.notifications.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.config.security.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications_roles")
public class NotificationRole extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_notifications_roles_permission"),
            name = "permission",
            nullable = false,
            referencedColumnName = "name"
    )
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_notifications_roles_notification"),
            name = "notification",
            nullable = false,
            referencedColumnName = "id"
    )
    private Notification notification;

}
