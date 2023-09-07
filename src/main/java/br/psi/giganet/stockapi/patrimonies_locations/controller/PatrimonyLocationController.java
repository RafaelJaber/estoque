package br.psi.giganet.stockapi.patrimonies_locations.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.patrimonies_locations.adapter.PatrimonyLocationAdapter;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.InsertPatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.UpdatePatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationProjection;
import br.psi.giganet.stockapi.patrimonies_locations.controller.security.RolePatrimoniesLocationsRead;
import br.psi.giganet.stockapi.patrimonies_locations.controller.security.RolePatrimoniesLocationsWrite;
import br.psi.giganet.stockapi.patrimonies_locations.service.PatrimonyLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patrimonies/locations")
public class PatrimonyLocationController {

    @Autowired
    private PatrimonyLocationAdapter patrimonyLocationAdapter;

    @Autowired
    private PatrimonyLocationService patrimonyLocationService;

    @GetMapping
    @RolePatrimoniesLocationsRead
    public Page<PatrimonyLocationProjection> findAll(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize
    ) {
        return patrimonyLocationService.findByName(name, page, pageSize)
                .map(patrimonyLocationAdapter::transform);
    }

    @GetMapping("/{id}")
    @RolePatrimoniesLocationsRead
    public PatrimonyLocationProjection findById(@PathVariable Long id) {
        return this.patrimonyLocationService.findById(id)
                .map(location -> patrimonyLocationAdapter.transform(location, PatrimonyLocationAdapter.ResponseType.RESPONSE))
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolePatrimoniesLocationsWrite
    public PatrimonyLocationProjection insert(@Valid @RequestBody InsertPatrimonyLocationRequest request) {
        return this.patrimonyLocationService.insert(patrimonyLocationAdapter.transform(request))
                .map(patrimonyLocationAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada"));
    }

    @PutMapping("/{id}")
    @RolePatrimoniesLocationsWrite
    public PatrimonyLocationProjection update(@PathVariable Long id, @Valid @RequestBody UpdatePatrimonyLocationRequest request) {
        return this.patrimonyLocationService.update(id, patrimonyLocationAdapter.transform(request))
                .map(patrimonyLocationAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada"));
    }

}
