package br.psi.giganet.stockapi.dashboard.main_items.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.dashboard.main_items.model.GroupCategory;
import br.psi.giganet.stockapi.dashboard.main_items.model.MainDashboardItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MainDashboardItemGroupRepository extends JpaRepository<MainDashboardItemGroup, Long> {

    Optional<MainDashboardItemGroup> findByLabel(String label);

    List<MainDashboardItemGroup> findByCategoryAndBranchOffice(GroupCategory category, BranchOffice branchOffice);

}
