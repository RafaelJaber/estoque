package br.psi.giganet.stockapi.technician.technician_product_category.repository;

import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.technician_product_category.model.TechnicianSectorProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface TechnicianSectorProductCategoryRepository extends JpaRepository<TechnicianSectorProductCategory, Long> {

    List<TechnicianSectorProductCategory> findBySector(TechnicianSector sector);

    @Modifying
    @Transactional
    @Query("DELETE FROM TechnicianSectorProductCategory sc WHERE sc.sector IN :sectors")
    void deleteByTechnicianSectors(Set<TechnicianSector> sectors);

}
