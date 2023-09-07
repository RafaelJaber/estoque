package br.psi.giganet.stockapi.technician.service;

import br.psi.giganet.stockapi.common.utils.service.RemoteApiService;
import br.psi.giganet.stockapi.technician.factory.TechnicianFactory;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.service.dto.PlainDTO;
import br.psi.giganet.stockapi.technician.service.dto.TechnicianScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RemoteTechnicianService extends RemoteApiService {

    @Autowired
    private TechnicianFactory technicianFactory;

    @SuppressWarnings("unchecked")
    public List<Technician> getRemoteTechnicians() {
        Optional<HttpHeaders> headers = getAuthentication();
        if (headers.isEmpty()) {
            return null;
        }
        String url = this.properties.getSmartnet().getUrl() + "/tecnico";

        ResponseEntity<LinkedHashMap> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers.get()), LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> body = (List<LinkedHashMap<String, Object>>) response.getBody().get("body");

        return body.stream()
                .map(technician -> {
                    String id = (String) technician.get("id");
                    LinkedHashMap<String, Object> user = (LinkedHashMap<String, Object>) technician.get("usuario");

                    String userId = (String) user.get("id");
                    String name = (String) user.get("nome");
                    String email = user.get("email") != null ? (String) user.get("email") : "nao-informado@email.com";

                    LinkedHashMap<String, Object> technicianRegister = (LinkedHashMap<String, Object>) user.get("tecnico");

                    String technicianId = (String) technicianRegister.get("id");
                    Boolean active = (Boolean) technicianRegister.get("ativo");

                    return technicianFactory.create(id, userId, name, email, technicianId, active);
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<TechnicianScheduleDTO> getTechnicianSchedule(Technician technician, LocalDate initialDate, LocalDate finalDate) {
        Optional<HttpHeaders> headers = getAuthentication();
        if (headers.isEmpty()) {
            return null;
        }
        String url = this.properties.getSmartnet().getUrl() + "/tecnico/" + technician.getId() + "/agenda-completa?" +
                "dataInicial=" + initialDate.toString() + "&dataFinal=" + finalDate.toString();

        ResponseEntity<LinkedHashMap> response;
        try {
            response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers.get()), LinkedHashMap.class);
        } catch (RestClientException ex) {
            return Collections.emptyList();
        }

        if (response.getBody().get("body") == null) {
            return Collections.emptyList();
        }

        List<LinkedHashMap<String, Object>> body = (List<LinkedHashMap<String, Object>>) response.getBody().get("body");

        return body.stream()
                .map(schedule -> {
                    String date = (String) schedule.get("dataAgendamento");
                    date = date.substring(0, date.indexOf("T"));

                    LinkedHashMap<String, Object> addressPayload = (LinkedHashMap<String, Object>) schedule.get("enderecoInstalacao");

                    final String address;
                    if (addressPayload.get("rua") != null) {
                        final String street = ((LinkedHashMap<String, String>) addressPayload.get("rua")).get("nome");
                        final String postalCode = (String) addressPayload.get("cep");
                        final String number = addressPayload.get("numero").toString();
                        final String district = ((LinkedHashMap<String, String>) addressPayload.get("bairro")).get("nome");
                        final String city = ((LinkedHashMap<String, String>) addressPayload.get("cidade")).get("nome");
                        final String state = ((LinkedHashMap<String, String>) addressPayload.get("estado")).get("uf");
                        address = street + " " + number + ", " + district + ", " + city + " - " + state + ". CEP: " + postalCode;
                    } else {
                        address = (String) addressPayload.get("complemento");
                    }

                    final String subtype = ((LinkedHashMap<String, String>) schedule.get("subtipoOS")).get("descricao");

                    final String box;
                    if (schedule.get("caixa") != null) {
                        box = ((LinkedHashMap<String, String>) schedule.get("caixa")).get("nome");
                    } else {
                        box = "-";
                    }

                    LinkedHashMap<String, String> plain = (LinkedHashMap<String, String>) schedule.get("plano");
                    final String plainId = plain.get("id");
                    final String plainName = plain.get("nome");

                    return new TechnicianScheduleDTO(
                            LocalDate.parse(date),
                            subtype,
                            address,
                            box,
                            new PlainDTO(plainId, plainName));
                })
                .collect(Collectors.toList());

    }
}
