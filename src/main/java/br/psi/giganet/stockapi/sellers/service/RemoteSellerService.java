package br.psi.giganet.stockapi.sellers.service;

import br.psi.giganet.stockapi.common.utils.service.RemoteApiService;
import br.psi.giganet.stockapi.sellers.factory.SellerFactory;
import br.psi.giganet.stockapi.sellers.model.Seller;
import br.psi.giganet.stockapi.technician.factory.TechnicianFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RemoteSellerService extends RemoteApiService {

    @Autowired
    private SellerFactory sellerFactory;

    @Autowired
    private TechnicianFactory technicianFactory;

    public List<Seller> getRemoteEmployeeSellers() {
        Optional<HttpHeaders> headers = getAuthentication();
        if (headers.isEmpty()) {
            return null;
        }
        String url = this.properties.getSmartnet().getUrl() + "/vendedor";

        ResponseEntity<LinkedHashMap> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers.get()), LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> body = (List<LinkedHashMap<String, Object>>) response.getBody().get("body");

        return body.stream()
                .filter(seller -> ((LinkedHashMap<String, Object>) seller.get("usuario")).get("email") != null)
                .map(seller -> {
                    String id = (String) seller.get("id");
                    Boolean active = (Boolean) seller.get("ativo");

                    LinkedHashMap<String, Object> user = (LinkedHashMap<String, Object>) seller.get("usuario");
                    String userId = (String) user.get("id");
                    String name = (String) user.get("nome");
                    String email = (String) user.get("email");

                    return sellerFactory.create(id, userId, name, email, id, active);
                })
                .collect(Collectors.toList());
    }
}
