package br.psi.giganet.stockapi.patrimonies.service;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyMoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatrimonyMoveService {

    @Autowired
    private PatrimonyMoveRepository patrimonyMoveRepository;

    public Optional<PatrimonyMove> insert(PatrimonyMove move){
        return Optional.of(patrimonyMoveRepository.save(move));
    }

}
