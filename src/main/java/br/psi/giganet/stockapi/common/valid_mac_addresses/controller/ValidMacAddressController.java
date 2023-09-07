package br.psi.giganet.stockapi.common.valid_mac_addresses.controller;

import br.psi.giganet.stockapi.common.valid_mac_addresses.model.ValidMacAddress;
import br.psi.giganet.stockapi.common.valid_mac_addresses.service.ValidMacAddressesService;
import br.psi.giganet.stockapi.patrimonies.controller.security.RolePatrimoniesRead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patrimonies/mac-addresses")
public class ValidMacAddressController {

    @Autowired
    private ValidMacAddressesService addressesService;

    @GetMapping("/available")
    @RolePatrimoniesRead
    public Page<String> findAllAvailable(
            @RequestParam(defaultValue = "") String address,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        if(address != null){
            address = address.replaceAll("[:.-]", "");
        }

        return addressesService.findAllNotUsedByAddress(address, page, pageSize)
                .map(ValidMacAddress::getAddress);
    }

}
