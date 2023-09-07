package br.psi.giganet.stockapi.technician.technician_product_category.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.products.categories.service.CategoryService;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.technician_product_category.model.TechnicianSectorProductCategory;
import br.psi.giganet.stockapi.technician.technician_product_category.repository.TechnicianSectorProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TechnicianSectorProductCategoryService {

    @Autowired
    private TechnicianSectorProductCategoryRepository sectorCategoryRepository;

    @Autowired
    private CategoryService categoryService;

    public List<TechnicianSectorProductCategory> findAll() {
        return sectorCategoryRepository.findAll();
    }

    public List<TechnicianSectorProductCategory> findBySector(TechnicianSector sector) {
        return sectorCategoryRepository.findBySector(sector);
    }

    @Transactional
    public List<TechnicianSectorProductCategory> update(TechnicianSector sector, List<TechnicianSectorProductCategory> updates) {
        sectorCategoryRepository.deleteByTechnicianSectors(Collections.singleton(sector));

        return sectorCategoryRepository.saveAll(
                updates.stream()
                        .peek(sc -> sc.setCategory(categoryService.findById(sc.getCategory().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"))))
                        .collect(Collectors.toList()));
    }

    @Transactional
    public List<TechnicianSectorProductCategory> update(List<TechnicianSectorProductCategory> updates) {
        Set<TechnicianSector> sectors = updates.stream()
                .map(TechnicianSectorProductCategory::getSector)
                .collect(Collectors.toSet());
        sectorCategoryRepository.deleteByTechnicianSectors(sectors);

        return sectorCategoryRepository.saveAll(
                updates.stream()
                        .peek(sc -> sc.setCategory(categoryService.findById(sc.getCategory().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"))))
                        .collect(Collectors.toList()));
    }

}
