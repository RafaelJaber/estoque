package br.psi.giganet.stockapi.branch_offices.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.employees.model.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchOfficeRepository extends JpaRepository<BranchOffice, Long> {

    @Query("SELECT bo FROM BranchOffice bo WHERE :employee MEMBER OF bo.employees")
    List<BranchOffice> findAllByEmployee(Employee employee, Sort sort);

}
