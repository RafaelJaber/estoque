package br.psi.giganet.stockapi.common.valid_mac_addresses.service;

import br.psi.giganet.stockapi.common.valid_mac_addresses.model.ValidMacAddress;
import br.psi.giganet.stockapi.common.valid_mac_addresses.repository.ValidMacAddressesRepository;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.service.PatrimonyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidMacAddressesService {

    @Autowired
    private ValidMacAddressesRepository addressesRepository;

    @Autowired
    private PatrimonyService patrimonyService;

    public Page<ValidMacAddress> findAllNotUsedByAddress(String address, Integer page, Integer pageSize) {
        return addressesRepository.findAllNotUsedByAddress(address, PageRequest.of(page, pageSize));
    }

    public Optional<ValidMacAddress> findByAddress(String address) {
        return addressesRepository.findByAddress(address);
    }

    public Boolean isValid(String address) {
        Optional<Patrimony> patrimony = patrimonyService.findByUniqueCode(address);
        Optional<ValidMacAddress> validMacAddress = findByAddress(address);
        return patrimony.isPresent() || (validMacAddress.isPresent() && !validMacAddress.get().isUsed());
    }

    public Boolean isAvailable(String address) {
        return addressesRepository.existsAddressAndIsNotUsed(address);
    }

    public Optional<ValidMacAddress> markAddressAsUsed(String address) {
        return addressesRepository.findByAddress(address)
                .map(saved -> {
                    if (saved.isUsed()) {
                        throw new IllegalArgumentException("Código MAC já foi marcado como utilizado");
                    }
                    saved.setIsUsed(Boolean.TRUE);
                    return addressesRepository.save(saved);
                });
    }

}
