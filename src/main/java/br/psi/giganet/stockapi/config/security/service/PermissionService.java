package br.psi.giganet.stockapi.config.security.service;

import br.psi.giganet.stockapi.config.security.Permissions;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.config.security.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public Optional<Permission> findById(String id){
        return this.permissionRepository.findById(id);
    }

    public Optional<Permission> findByName(String name){
        return this.permissionRepository.findByName(name);
    }

    public Permission save(Permissions permission) {
        return permissionRepository.findById(permission.name())
                .orElseGet(() -> permissionRepository.save(new Permission(permission.name())));
    }
}
