package br.psi.giganet.stockapi.dashboard.main_items.controller;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.dashboard.main_items.adapter.MainDashboardItemGroupAdapter;
import br.psi.giganet.stockapi.dashboard.main_items.controller.request.MainDashboardItemGroupRequest;
import br.psi.giganet.stockapi.dashboard.main_items.controller.response.MainDashboardItemGroupProjection;
import br.psi.giganet.stockapi.dashboard.main_items.controller.response.MainDashboardItemGroupResponse;
import br.psi.giganet.stockapi.dashboard.main_items.service.MainDashboardItemGroupService;
import br.psi.giganet.stockapi.employees.controller.security.RoleRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard/main-item-groups")
public class MainDashboardGroupController {

    @Autowired
    private MainDashboardItemGroupService groupService;

    @Autowired
    private MainDashboardItemGroupAdapter groupAdapter;

    @GetMapping
    public List<MainDashboardItemGroupResponse> findAll() {
        return groupService.findAll()
                .stream()
                .map(groupAdapter::transformToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MainDashboardItemGroupResponse findById(@PathVariable Long id) {
        return groupService.findById(id)
                .map(groupAdapter::transformToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Grupo de itens não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleRoot
    public MainDashboardItemGroupProjection insert(@RequestBody @Valid MainDashboardItemGroupRequest request) {
        return groupService.insert(groupAdapter.transform(request))
                .map(groupAdapter::transform)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar o grupo solicitado"));
    }

    @PutMapping("/{id}")
    @RoleRoot
    public MainDashboardItemGroupProjection update(@PathVariable Long id, @RequestBody @Valid MainDashboardItemGroupRequest request) {
        return groupService.update(id, groupAdapter.transform(request))
                .map(groupAdapter::transform)
                .orElseThrow(() -> new IllegalArgumentException("Grupo de itens não encontrado"));
    }

    @DeleteMapping("/{id}")
    @RoleRoot
    public ResponseEntity<Object> deleteById(@PathVariable Long id) {
        groupService.deleteById(id).orElseThrow(() -> new IllegalArgumentException("Grupo de itens não encontrado"));
        return ResponseEntity.noContent().build();
    }

}
