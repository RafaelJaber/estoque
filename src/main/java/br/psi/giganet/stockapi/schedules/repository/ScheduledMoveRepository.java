package br.psi.giganet.stockapi.schedules.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ScheduledMoveRepository extends JpaRepository<ScheduledMove, Long> {

    @Query("SELECT s FROM ScheduledMove s WHERE s.date BETWEEN :initialDate AND :finalDate")
    List<ScheduledMove> findAllBetween(ZonedDateTime initialDate, ZonedDateTime finalDate, Sort sort);

    @Query("SELECT s FROM ScheduledMove s WHERE " +
            "s.date BETWEEN :initialDate AND :finalDate AND " +
            "s.status = 'SCHEDULED'")
    List<ScheduledMove> findAllScheduledBetween(ZonedDateTime initialDate, ZonedDateTime finalDate, Sort sort);

    @Query("SELECT s FROM ScheduledMove s " +
            "JOIN FETCH s.items " +
            "JOIN FETCH s.responsible " +
            "WHERE " +
            "s.date BETWEEN :initialDate AND :finalDate AND " +
            "s.status = 'SCHEDULED' AND " +
            "s.execution = 'AUTOMATIC'")
    List<ScheduledMove> findAllAutomaticAndScheduledBetween(ZonedDateTime initialDate, ZonedDateTime finalDate, Sort sort);

    @Query("SELECT s FROM ScheduledMove s WHERE s.branchOffice = :branchOffice")
    Page<ScheduledMove> findAllByBranchOffice(BranchOffice branchOffice, Pageable pageable);

}
