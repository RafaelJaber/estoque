package br.psi.giganet.stockapi.common.notifications.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends AbstractModel {

    @NotEmpty
    private String title;

    @Column(length = 1024)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    private NotificationType type;

    @Column(length = 1024)
    private String data;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            mappedBy = "notification")
    private List<NotificationRole> roles;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            mappedBy = "notification")
    private List<NotificationEmployee> employees;


}
