package br.psi.giganet.stockapi.entries.controller;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.entries.adapter.EntryAdapter;
import br.psi.giganet.stockapi.entries.controller.request.InsertEntryRequest;
import br.psi.giganet.stockapi.entries.controller.response.EntryItemWithMetaDataProjection;
import br.psi.giganet.stockapi.entries.controller.response.EntryProjection;
import br.psi.giganet.stockapi.entries.controller.response.EntryResponse;
import br.psi.giganet.stockapi.entries.controller.security.RoleEntriesRead;
import br.psi.giganet.stockapi.entries.controller.security.RoleEntriesWriteManual;
import br.psi.giganet.stockapi.entries.service.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/entries")
public class EntryController {

    @Autowired
    private EntryService entries;

    @Autowired
    private EntryAdapter adapter;

    @GetMapping
    @RoleEntriesRead
    public List<EntryProjection> findAll() {
        return entries.findAllByCurrentBranchOffice()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleEntriesRead
    public EntryResponse findById(@PathVariable Long id) {
        return entries.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));
    }

    @GetMapping(path = "/{id}", params = {"withMetaData"})
    @RoleEntriesRead
    public List<EntryItemWithMetaDataProjection> findByIdWithMetaData(@PathVariable Long id) {
        return entries.findById(id)
                .map(entry -> entry.getItems()
                        .stream()
                        .map(adapter::transformToEntryItemWithMetaDataProjection).
                                collect(Collectors.toList()))
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));
    }

    @GetMapping("/items/{id}")
    @RoleEntriesRead
    public EntryItemWithMetaDataProjection findByItemIdWithMetaData(@PathVariable Long id) {
        return entries.findByEntryItemId(id)
                .map(adapter::transformToEntryItemWithMetaDataProjection)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleEntriesWriteManual
    public EntryResponse insert(@Valid @RequestBody InsertEntryRequest request) {
        return this.entries.insert(adapter.transform(request), request.getUpdateStock())
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar o seu lançamento"));
    }

}
