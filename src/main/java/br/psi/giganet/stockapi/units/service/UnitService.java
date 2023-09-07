package br.psi.giganet.stockapi.units.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.units.model.Unit;
import br.psi.giganet.stockapi.units.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    public List<Unit> findAll() {
        return unitRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Unit> findById(String id) {
        return unitRepository.findById(id);
    }

    public Optional<Unit> insert(Unit unit, boolean ignoreConversions) {
        if (ignoreConversions) {
            unit.setConversions(null);
        } else if (unit.getConversions() != null) {
            unit.getConversions().forEach(conversion -> conversion.setTo(
                    unitRepository.findById(conversion.getTo().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada"))));
        }

        return Optional.of(unitRepository.saveAndFlush(unit));
    }

    public Optional<Unit> insert(Unit unit) {
        return insert(unit, false);
    }

    @Transactional
    public Optional<Unit> update(String id, Unit unit) {
        return unitRepository.findById(id)
                .map(saved -> {
                    saved.setName(unit.getName());
                    saved.setDescription(unit.getDescription());
                    saved.setAbbreviation(unit.getAbbreviation());

                    if (unit.getConversions() != null) {

                        if (saved.getConversions() != null) {
                            saved.getConversions()
                                    .removeIf(conversion -> !unit.getConversions().contains(conversion));

                            unit.getConversions()
                                    .stream()
                                    .filter(conversion -> saved.getConversions().contains(conversion))
                                    .collect(Collectors.toList())
                                    .forEach(conversion -> {
                                        int index = saved.getConversions().indexOf(conversion);
                                        saved.getConversions().get(index).setConversion(conversion.getConversion());
                                    });
                        } else {
                            saved.setConversions(new ArrayList<>());
                        }

                        unit.getConversions()
                                .stream()
                                .filter(conversion -> !saved.getConversions().contains(conversion))
                                .peek(conversion -> {
                                    conversion.setFrom(saved);
                                    conversion.setTo(unitRepository
                                            .findById(conversion.getTo().getId())
                                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                                })
                                .collect(Collectors.toList())
                                .forEach(conversion -> saved.getConversions().add(conversion));

                    }

                    return unitRepository.saveAndFlush(saved);
                });
    }

    @Transactional
    public void save(Unit unit) {
        this.findById(unit.getId()).ifPresentOrElse(
                saved -> this.update(saved.getId(), unit),
                () -> {
                    if (unit.getConversions() != null) {
                        unit.getConversions().forEach(u -> this.save(u.getTo()));
                    }
                    this.insert(unit);
                });
    }

    @Transactional
    public void save(List<Unit> units) {
        final List<Unit> markedToUpdate = new ArrayList<>();
        units.forEach(unit ->
                this.findById(unit.getId()).ifPresentOrElse(
                        saved -> this.update(saved.getId(), unit),
                        () -> this.insert(unit, true).ifPresent(saved -> {
                            unit.setId(saved.getId());
                            markedToUpdate.add(unit);
                        })));

        markedToUpdate.forEach(unit -> this.update(unit.getId(), unit));
    }

    public Optional<Unit> deleteById(String id) {
        return unitRepository.findById(id)
                .map(unit -> {
                    unitRepository.delete(unit);
                    return unit;
                });
    }

}
