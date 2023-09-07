package br.psi.giganet.stockapi.dashboard.main_items.adapter;

import br.psi.giganet.stockapi.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.stockapi.branch_offices.factory.BranchOfficeFactory;
import br.psi.giganet.stockapi.dashboard.main_items.controller.request.MainDashboardItemGroupRequest;
import br.psi.giganet.stockapi.dashboard.main_items.controller.response.MainDashboardItemGroupProjection;
import br.psi.giganet.stockapi.dashboard.main_items.controller.response.MainDashboardItemGroupResponse;
import br.psi.giganet.stockapi.dashboard.main_items.controller.response.MainDashboardItemResponse;
import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItem;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItemGroup;
import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class MainDashboardItemGroupAdapter {

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private BranchOfficeFactory branchOfficeFactory;
    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    public MainDashboardItemGroupResponse transformToResponse(MainDashboardItemGroup group) {
        MainDashboardItemGroupResponse response = new MainDashboardItemGroupResponse();
        response.setId(group.getId());
        response.setLabel(group.getLabel());
        response.setCategory(group.getCategory());
        response.setBranchOffice(branchOfficeAdapter.transform(group.getBranchOffice()));
        response.setEmployees(group.getEmployees()
                .stream()
                .map(e -> employeeAdapter.transform(e))
                .collect(Collectors.toSet()));
        response.setItems(group.getItems()
                .stream()
                .sorted(Comparator.comparing(MainDashboardItem::getIndex))
                .map(item ->
                        new MainDashboardItemResponse(
                                item.getId(),
                                productAdapter.transformWithoutUnit(item.getProduct())))
                .collect(Collectors.toList()));

        return response;
    }

    public MainDashboardItemGroupProjection transform(MainDashboardItemGroup group) {
        MainDashboardItemGroupProjection response = new MainDashboardItemGroupProjection();
        response.setId(group.getId());
        response.setLabel(group.getLabel());
        response.setCategory(group.getCategory());

        return response;
    }

    public MainDashboardItemGroup transform(MainDashboardItemGroupRequest request) {
        MainDashboardItemGroup group = new MainDashboardItemGroup();
        group.setId(request.getId());
        group.setLabel(request.getLabel());
        group.setCategory(request.getCategory());
        if (request.getCategory().equals(GroupCategory.DEFAULT)) {
            group.setEmployees(Collections.emptySet());
        } else {
            group.setEmployees(request.getEmployees().stream()
                    .map(e -> employeeAdapter.create(e))
                    .collect(Collectors.toSet()));
        }
        group.setBranchOffice(branchOfficeFactory.create(request.getBranchOffice()));
        group.setItems(request.getItems().stream()
                .map(itemRequest -> {
                    MainDashboardItem item = new MainDashboardItem();
                    item.setId(itemRequest.getId());
                    item.setIndex(itemRequest.getIndex());
                    item.setProduct(productAdapter.create(itemRequest.getProduct()));
                    item.setGroup(group);

                    return item;
                })
                .collect(Collectors.toList()));

        return group;
    }

}
