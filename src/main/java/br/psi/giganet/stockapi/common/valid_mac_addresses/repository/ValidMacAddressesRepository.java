package br.psi.giganet.stockapi.common.valid_mac_addresses.repository;

import br.psi.giganet.stockapi.common.valid_mac_addresses.model.ValidMacAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidMacAddressesRepository extends JpaRepository<ValidMacAddress, String> {

    Optional<ValidMacAddress> findByAddress(String address);

    @Query("SELECT a FROM ValidMacAddress a WHERE " +
            "a.isUsed = FALSE AND " +
            "UPPER(a.address) LIKE CONCAT(:address, '%')")
    Page<ValidMacAddress> findAllNotUsedByAddress(String address, Pageable pageable);

    @Query("SELECT (COUNT(a) > 0) FROM ValidMacAddress a WHERE a.address = :address AND a.isUsed = FALSE")
    Boolean existsAddressAndIsNotUsed(String address);

}
