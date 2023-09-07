package br.psi.giganet.stockapi.common.notifications.controller;

import br.psi.giganet.stockapi.common.notifications.adapter.NotificationAdapter;
import br.psi.giganet.stockapi.common.notifications.controller.request.MarkAsReadRequest;
import br.psi.giganet.stockapi.common.notifications.controller.response.NotificationResponse;
import br.psi.giganet.stockapi.common.notifications.controller.security.RoleNotificationsRead;
import br.psi.giganet.stockapi.common.notifications.factory.NotificationFactory;
import br.psi.giganet.stockapi.common.notifications.service.NotificationService;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationAdapter notificationAdapter;

    @Autowired
    private NotificationFactory notificationFactory;


    @GetMapping("/{id}")
    @RoleNotificationsRead
    public NotificationResponse findById(@PathVariable Long id) {
        return this.notificationService.findById(id)
                .map(notificationAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
    }

    @GetMapping("/me")
    @RoleNotificationsRead
    public List<NotificationResponse> findAllByCurrentEmployee(@RequestParam(defaultValue = "50") Integer limit) {
        return this.notificationService.findAllByEmployee()
                .stream()
                .limit(limit)
                .map(notificationAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/me/unread")
    @RoleNotificationsRead
    public List<NotificationResponse> findAllUnreadByCurrentEmployee() {
        return this.notificationService.findAllUnreadByEmployee()
                .stream()
                .map(notificationAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/view")
    @RoleNotificationsRead
    public NotificationResponse markAsViewed(@PathVariable Long id) {
        return this.notificationService.markAsViewed(notificationFactory.create(id))
                .map(notificationAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
    }

    @PostMapping("/view")
    @RoleNotificationsRead
    public List<NotificationResponse> markAsViewed(@RequestBody @Valid MarkAsReadRequest request) {
        return this.notificationService.markAsViewed(notificationAdapter.transform(request))
                .stream()
                .map(notificationAdapter::transform)
                .collect(Collectors.toList());
    }

}
