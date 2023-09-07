package br.psi.giganet.stockapi.common.notifications.adapter;

import br.psi.giganet.stockapi.common.notifications.controller.request.MarkAsReadRequest;
import br.psi.giganet.stockapi.common.notifications.controller.response.NotificationResponse;
import br.psi.giganet.stockapi.common.notifications.factory.NotificationFactory;
import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.common.notifications.model.NotificationEmployee;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NotificationAdapter {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private NotificationFactory notificationFactory;

    public List<Notification> transform(MarkAsReadRequest request) {
        return request.getNotifications()
                .stream()
                .map(n -> notificationFactory.create(n))
                .collect(Collectors.toList());
    }

    public NotificationResponse transform(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setData(notification.getData());
        response.setDate(notification.getCreatedDate());
        response.setDescription(notification.getDescription());
        response.setType(notification.getType());

        Employee currentUser = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        Optional<NotificationEmployee> notificationEmployee = notification.getEmployees().stream()
                .filter(ne -> ne.getEmployee().equals(currentUser))
                .findFirst();

        response.setViewed(notificationEmployee.isPresent() && notificationEmployee.get().getViewed());
        if (response.getViewed()) {
            response.setViewedDate(notificationEmployee.get().getLastModifiedDate());
        }

        return response;
    }

}
