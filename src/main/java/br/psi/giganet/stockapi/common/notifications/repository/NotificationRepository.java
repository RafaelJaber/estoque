package br.psi.giganet.stockapi.common.notifications.repository;

import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT DISTINCT n FROM Notification n " +
            "INNER JOIN NotificationEmployee ne ON ne.notification = n " +
            "WHERE ne.viewed = FALSE AND " +
            "ne.employee = :employee " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findAllUnreadByEmployee(Employee employee);

    @Query("SELECT DISTINCT n FROM Notification n " +
            "INNER JOIN NotificationEmployee ne ON ne.notification = n " +
            "WHERE ne.employee = :employee " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findAllByEmployee(Employee employee);

}
