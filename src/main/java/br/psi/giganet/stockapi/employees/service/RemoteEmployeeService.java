package br.psi.giganet.stockapi.employees.service;

import br.psi.giganet.stockapi.common.utils.service.RemoteApiService;
import br.psi.giganet.stockapi.employees.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RemoteEmployeeService extends RemoteApiService {


    @Autowired
    private EmployeeService employeeService;

    public List<Optional<Optional<Employee>>> getRemoteEmployeers() {
        Optional<HttpHeaders> headers = getAuthentication();
        if (headers.isEmpty()) {
            return null;
        }
        String url = this.properties.getSmartnet().getUrl() + "/usuario?offset=0&limit=1000";

        ResponseEntity<LinkedHashMap> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers.get()), LinkedHashMap.class);
        List<LinkedHashMap<String, Object>> body = (List<LinkedHashMap<String, Object>>) response.getBody().get("body");

        return body.stream()
                .filter(user -> user.get("email") != null)
                .map(user -> employeeService.findByEmail((String) user.get("email"))
                        .map(employee -> {
                            String id = (String) user.get("id");
                            employee.setUserId(id);
                            return employeeService.insert(employee);
                        }))
                .collect(Collectors.toList());
    }
}