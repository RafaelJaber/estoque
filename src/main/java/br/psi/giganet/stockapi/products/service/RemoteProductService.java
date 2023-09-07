package br.psi.giganet.stockapi.products.service;

import br.psi.giganet.stockapi.common.utils.service.RemoteApiService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Optional;

@Service
public class RemoteProductService extends RemoteApiService {

    public ResponseEntity<Object> getRemoteNextProductCode(String category) {
        Optional<HttpHeaders> headers = getAuthentication();
        if (headers.isEmpty()) {
            return null;
        }
        String url = this.properties.getSmartnet().getUrl() + "/purchases/products/code/generate?category=" + category;

        ResponseEntity<LinkedHashMap> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers.get()), LinkedHashMap.class);

        return ResponseEntity.accepted().body(response.getBody().get("body"));
    }
}
