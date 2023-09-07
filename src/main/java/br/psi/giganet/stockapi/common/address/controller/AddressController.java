package br.psi.giganet.stockapi.common.address.controller;

import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Address findAddressByPostalCode(@RequestParam(name = "cep") String postalCode) {
        return addressService.findAddressByPostalCode(postalCode)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));
    }

}
