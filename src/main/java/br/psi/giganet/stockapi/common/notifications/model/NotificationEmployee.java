package br.psi.giganet.stockapi.common.notifications.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications_employees")
public class NotificationEmployee extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_notifications_employees_employee"),
            name = "employee",
            nullable = false,
            referencedColumnName = "id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_notifications_employees_notification"),
            name = "notification",
            nullable = false,
            referencedColumnName = "id")
    private Notification notification;

    @NotNull
    private Boolean viewed;

}
