package br.psi.giganet.stockapi.common.address.service;

import br.psi.giganet.stockapi.common.address.model.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public Optional<Address> findAddressByPostalCode(String postalCode) {
        final String url = "https://viacep.com.br/ws/" + postalCode + "/json/";

        try {
            var response = this.restTemplate.getForEntity(url, HashMap.class);

            if (response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    !response.getBody().containsKey("erro")) {
                HashMap<String, String> body = (HashMap<String, String>) response.getBody();
                Address address = new Address();
                address.setPostalCode(body.get("cep").replaceAll("-", ""));
                address.setStreet(body.get("logradouro"));
                address.setComplement(body.get("complemento"));
                address.setDistrict(body.get("bairro"));
                address.setCity(body.get("localidade"));
                address.setState(body.get("uf"));

                return Optional.of(address);
            }

        } catch (HttpClientErrorException e) {
            System.out.println(e);
        }

        return Optional.empty();
    }

    public Address getPurchaseOrderDeliveryAddress(){
        Address address = new Address();
        address.setPostalCode("35160296");
        address.setComplement("");
        address.setCity("Ipatinga");
        address.setDistrict("Horto");
        address.setState("MG");
        address.setStreet("Rua Cedro");
        address.setNumber("393");

        return address;
    }

}
