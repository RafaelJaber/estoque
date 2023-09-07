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

@RestController
@RequestMapping("/basic/patrimonies/locations")
public class BasicPatrimonyLocationController {

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
}
