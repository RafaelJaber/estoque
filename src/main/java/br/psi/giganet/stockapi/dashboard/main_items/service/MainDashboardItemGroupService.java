package br.psi.giganet.stockapi.dashboard.main_items.service;

import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItem;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItemGroup;
import br.psi.giganet.stockapi.dashboard.main_items.repository.MainDashboardItemGroupRepository;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MainDashboardItemGroupService {

    @Autowired
    private MainDashboardItemGroupRepository groupRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    public Optional<MainDashboardItemGroup> insert(MainDashboardItemGroup group) {
        if (groupRepository.findByLabel(group.getLabel()).isPresent()) {
            throw new IllegalArgumentException("O nome " + group.getLabel() + " já é utilizado por outro grupo cadastrado");

        } else if (group.getCategory().equals(GroupCategory.DEFAULT)
                && !groupRepository.findByCategoryAndBranchOffice(group.getCategory(), group.getBranchOffice()).isEmpty()) {
            throw new IllegalArgumentException("É permitido somente 1 grupo como DEFAULT por filial");

        } else if (group.getCategory().equals(GroupCategory.CUSTOM) && group.getEmployees().isEmpty()) {
            throw new IllegalArgumentException("É necessário associar pelo menos 1 funcionário a este grupo");

        }

        group.setEmployees(group.getEmployees()
                .stream()
                .map(e -> employeeService.findById(e.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")))
                .collect(Collectors.toSet()));

        group.setBranchOffice(branchOfficeService.findById(group.getBranchOffice().getId())
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

        group.getItems().forEach(item ->
                item.setProduct(productService.findById(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"))));

        return Optional.of(groupRepository.save(group));
    }

    public Optional<MainDashboardItemGroup> update(Long id, MainDashboardItemGroup group) {
        return groupRepository.findById(id)
                .map(saved -> {

                    groupRepository.findByLabel(group.getLabel())
                            .filter(found -> !found.equals(saved))
                            .ifPresent(found -> {
                                throw new IllegalArgumentException("O nome " + group.getLabel() + " já é utilizado por outro grupo cadastrado");
                            });

                    if (group.getCategory().equals(GroupCategory.DEFAULT)) {
                        List<MainDashboardItemGroup> defaultGroups = groupRepository.findByCategoryAndBranchOffice(
                                group.getCategory(), group.getBranchOffice());

                        if (!defaultGroups.isEmpty() && !defaultGroups.contains(saved)) {
                            throw new IllegalArgumentException("É permitido somente 1 grupo como DEFAULT por filial");
                        }

                    } else if (group.getCategory().equals(GroupCategory.CUSTOM) && group.getEmployees().isEmpty()) {
                        throw new IllegalArgumentException("É necessário associar pelo menos 1 funcionário a este grupo");

                    }

                    saved.setLabel(group.getLabel());
                    saved.setCategory(group.getCategory());
                    saved.setBranchOffice(branchOfficeService.findById(group.getBranchOffice().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                    saved.setEmployees(group.getEmployees()
                            .stream()
                            .map(e -> employeeService.findById(e.getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")))
                            .collect(Collectors.toSet()));

                    saved.getItems().removeIf(savedItem -> !group.getItems().contains(savedItem));
                    group.getItems().forEach(item -> {
                        final int index = saved.getItems().indexOf(item);
                        if (index >= 0) {
                            MainDashboardItem savedItem = saved.getItems().get(index);
                            savedItem.setProduct(productService.findById(item.getProduct().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                            savedItem.setIndex(item.getIndex());
                        } else {
                            item.setGroup(saved);
                            item.setProduct(productService.findById(item.getProduct().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                            saved.getItems().add(item);
                        }
                    });

                    return groupRepository.save(saved);
                });
    }

    public List<MainDashboardItemGroup> findAll() {
        return groupRepository.findAll(Sort.by(Sort.Direction.ASC, "label"));
    }

    public Optional<MainDashboardItemGroup> findById(Long id) {
        return groupRepository.findById(id);
    }

    public Optional<MainDashboardItemGroup> deleteById(Long id) {
        Optional<MainDashboardItemGroup> group = groupRepository.findById(id);
        if (group.isPresent()) {
            groupRepository.deleteById(id);
        }

        return group;
    }
}
