package br.psi.giganet.stockapi.patrimonies.repository;

import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvancedPatrimonyRepository {

    Page<Patrimony> findAllFetchByIsVisibleAndProductOrCodeOrLocation(List<String> queries, Pageable pageable);

}
