package br.psi.giganet.stockapi.common.utils.service;

import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.common.utils.DecoderUtil;
import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class RemoteApiService {

    @Autowired
    private LogMessageService logService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    protected ApplicationProperties properties;
    @Autowired
    protected RestTemplate restTemplate;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @SuppressWarnings("unchecked")
    public Optional<HttpHeaders> getAuthentication() {
        final String authUrl = this.properties.getSmartnet().getUrl() + "/auth/sign-in";
        Map<String, String> request = new HashMap<>();
        request.put("user", this.properties.getSmartnet().getUser());
        request.put("password", this.properties.getSmartnet().getPassword());

        try {
            LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) this.restTemplate.postForObject(authUrl, request, LinkedHashMap.class);
            LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) response.get("body");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", (String) body.get("access_token"));

            return Optional.of(headers);

        } catch (RestClientException e) {
            System.out.println(e);
            return Optional.empty();
        }
    }

    public ResponseEntity<Object> requestForPurchaseAPI(String path, HttpMethod method, Object body) {
        final String URL = DecoderUtil.toUTF8(this.properties.getPurchaseApi().getUrl() + "/basic" + path);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(properties.getPurchaseApi().getUser(), properties.getPurchaseApi().getPassword());

        if (body != null) {
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            return this.restTemplate.exchange(URI.create(URL), method, entity, Object.class);
        }
        return this.restTemplate.exchange(URI.create(URL), method, new HttpEntity<>(headers), Object.class);
    }

    public ResponseEntity<Object> requestForPurchaseAPI(String path, HttpMethod method, Object body, Map<String, String> queryParams) {
        final String URL = DecoderUtil.toUTF8(this.properties.getPurchaseApi().getUrl() + "/basic" + path);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(properties.getPurchaseApi().getUser(), properties.getPurchaseApi().getPassword());

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(URL);
        queryParams.forEach(urlBuilder::queryParam);

        if (body != null) {
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            return this.restTemplate.exchange(urlBuilder.build().toUri(), method, entity, Object.class);
        }
        return this.restTemplate.exchange(urlBuilder.build().toUri(), method, new HttpEntity<>(headers), Object.class);
    }
}
