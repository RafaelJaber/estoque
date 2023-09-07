package br.psi.giganet.stockapi.patrimonies_locations.service;

import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.repository.PatrimonyLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatrimonyLocationService {

    @Autowired
    private PatrimonyLocationRepository patrimonyLocationRepository;

    public Page<PatrimonyLocation> findAll(Integer page, Integer pageSize) {
        return this.patrimonyLocationRepository.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<PatrimonyLocation> findByName(String name, Integer page, Integer pageSize) {
        return this.patrimonyLocationRepository.findByNameContainingIgnoreCase(
                name,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Optional<PatrimonyLocation> findById(Long id) {
        return this.patrimonyLocationRepository.findById(id);
    }

    public Optional<PatrimonyLocation> findByCode(String code) {
        return this.patrimonyLocationRepository.findByCode(code);
    }

    public Optional<PatrimonyLocation> insert(PatrimonyLocation location) {
        return Optional.of(this.patrimonyLocationRepository.save(location));
    }

    public Optional<PatrimonyLocation> update(Long id, PatrimonyLocation location) {
        return this.patrimonyLocationRepository.findById(id)
                .map(saved -> {
                    saved.setName(location.getName());
                    saved.setNote(location.getNote());

                    return patrimonyLocationRepository.save(saved);
                });
    }

}
