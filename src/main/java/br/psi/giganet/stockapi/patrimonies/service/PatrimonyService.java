package br.psi.giganet.stockapi.patrimonies.service;

import br.psi.giganet.stockapi.common.valid_mac_addresses.service.ValidMacAddressesService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.entries.service.EntryService;
import br.psi.giganet.stockapi.patrimonies.factory.PatrimonyFactory;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import br.psi.giganet.stockapi.patrimonies.repository.PatrimonyRepository;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.service.PatrimonyLocationService;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.service.ProductService;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatrimonyService {

    @Autowired
    private PatrimonyRepository patrimonyRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PatrimonyLocationService patrimonyLocationService;
    @Autowired
    private StockMovesService movesService;
    @Autowired
    private EntryService entryService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private PatrimonyMoveService patrimonyMoveService;
    @Autowired
    private PatrimonyFactory patrimonyFactory;

    @Autowired
    private ValidMacAddressesService macAddressesService;

    public Optional<Patrimony> findById(Long id) {
        return patrimonyRepository.findById(id);
    }

    public Optional<Patrimony> findByUniqueCode(String uniqueCode) {
        return patrimonyRepository.findByCodeIgnoreCase(uniqueCode);
    }

    public Page<Patrimony> findAll(Integer page, Integer pageSize) {
        return this.patrimonyRepository.findAllFetchByIsVisible(
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<Patrimony> findAllByProductNameOrCodeOrLocation(List<String> queries, Integer page, Integer pageSize) {
        return this.patrimonyRepository.findAllFetchByIsVisibleAndProductOrCodeOrLocation(
                queries,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "product.name")));
    }

    public Page<Patrimony> findAllByProduct(Product product, Integer page, Integer pageSize) {
        return this.patrimonyRepository.findByProductAndIsVisible(product,
                PageRequest.of(page, pageSize));
    }

    public Page<Patrimony> findAllByCurrentLocation(
            PatrimonyLocation patrimonyLocation,
            Integer page,
            Integer pageSize) {
        return this.patrimonyRepository.findByCurrentLocationAndIsVisible(
                patrimonyLocationService.findById(patrimonyLocation.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada")),
                PageRequest.of(page, pageSize));
    }

    public Page<Patrimony> findAllByCurrentLocationCode(
            PatrimonyLocation patrimonyLocation,
            Integer page,
            Integer pageSize) {
        return this.patrimonyRepository.findByCurrentLocationAndIsVisible(
                patrimonyLocationService.findByCode(patrimonyLocation.getCode())
                        .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada")),
                PageRequest.of(page, pageSize));
    }

    public Page<Patrimony> findAllByCurrentLocationAndProduct(
            PatrimonyLocation patrimonyLocation,
            Product product,
            Integer page,
            Integer pageSize) {
        return this.patrimonyRepository.findByCurrentLocationAndProductAndIsVisible(
                patrimonyLocationService.findById(patrimonyLocation.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                product,
                PageRequest.of(page, pageSize));
    }

    public Page<Patrimony> findAllByCurrentLocationCodeAndProduct(
            PatrimonyLocation patrimonyLocation,
            Product product,
            Integer page,
            Integer pageSize) {
        return this.patrimonyRepository.findByCurrentLocationAndProductAndIsVisible(
                patrimonyLocationService.findByCode(patrimonyLocation.getCode())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                product,
                PageRequest.of(page, pageSize));
    }

    @Transactional
    public Optional<Patrimony> insertByTechnician(Patrimony patrimony) {
        Employee employee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        patrimony.setCurrentLocation(
                patrimonyLocationService.findByCode(
                        technicianService
                                .findByEmail(employee.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado"))
                                .getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Localização informada é inválida")));

        return insert(patrimony);
    }

    @Transactional
    public List<Patrimony> insert(List<Patrimony> patrimonies) {
        return patrimonies.stream().map(patrimony -> {
            patrimony.setEntryItem(patrimony.getEntryItem() == null ? null :
                    entryService.findByEntryItemId(patrimony.getEntryItem().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Entrada de item não encontrada")));

            Optional<Patrimony> saved = insert(patrimony);
            if (saved.isEmpty()) {
                throw new IllegalArgumentException("Não foi possível salvar o patrimônio " + patrimony.getCode());
            }
            return saved.get();
        }).collect(Collectors.toList());
    }

    @Transactional
    public Optional<Patrimony> insert(Patrimony patrimony) {
        patrimonyRepository.findByCodeIgnoreCase(patrimony.getCode()).ifPresent((p) -> {
            throw new IllegalArgumentException("Patrimônio já cadastrado");
        });

        Boolean isCodeAvailable = macAddressesService.isAvailable(patrimony.getCode());
        Employee employee = employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        if (!employee.hasRole("ROLE_PATRIMONIES_WRITE_ROOT") && !employee.isRoot() && !isCodeAvailable) {
            throw new IllegalArgumentException("Código do patrimônio não está entre os códigos permitidos. " +
                    "Tente novamente ou entre em contato com os seus responsáveis para incluí-lo");
        }

        if (isCodeAvailable) {
            macAddressesService.markAddressAsUsed(patrimony.getCode());
        }

        patrimony.setProduct(productService.findById(patrimony.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
        patrimony.setCurrentLocation(patrimonyLocationService.findById(patrimony.getCurrentLocation().getId())
                .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada")));

        if (patrimony.getEntryItem() != null) {
            if (patrimony.getEntryItem().getPatrimonies() != null) {
                final int registered = patrimony.getEntryItem().getPatrimonies().size();
                if (registered >= patrimony.getEntryItem().getQuantity()) {
                    throw new IllegalArgumentException("Não é possível vincular o patrimônio " + patrimony.getCode() +
                            " ao lançamento " + patrimony.getEntryItem().getId() +
                            ". Já foram cadastrados todos os patrimônios permitidos para esta entrada.");
                }
            } else {
                patrimony.getEntryItem().setPatrimonies(new ArrayList<>());
            }
            patrimony.getEntryItem().getPatrimonies().add(patrimony);
        }

        Patrimony saved = patrimonyRepository.save(patrimony);

        saved.getMoves().add(
                patrimonyMoveService.insert(
                        patrimonyFactory.create(
                                patrimony,
                                patrimony.getCurrentLocation(),
                                employeeService.getCurrentLoggedEmployee()
                                        .orElseThrow(() -> new IllegalArgumentException("Responsável não foi encontrado"))))
                        .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar a movimentação")));

        return Optional.of(patrimonyRepository.save(saved));
    }

    @Transactional
    public Optional<Patrimony> update(Long id, Patrimony patrimony) {
        return this.patrimonyRepository.findById(id)
                .map(saved -> {
                    if (!saved.getProduct().equals(patrimony.getProduct())) {
                        saved.setProduct(productService.findById(patrimony.getProduct().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                    }

                    saved.setNote(patrimony.getNote());

                    if (!saved.isVisible()) {
                        saved.setIsVisible(Boolean.TRUE);
                    }

                    return patrimonyRepository.save(saved);
                });
    }

    @Transactional
    public Optional<Patrimony> movePatrimony(Long id, PatrimonyMove move) {
        return patrimonyRepository.findById(id)
                .map((patrimony) -> {
                    if (patrimony.getMoves() == null) {
                        patrimony.setMoves(new ArrayList<>());
                    }

                    move.setPatrimony(patrimony);
                    move.setResponsible(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Responsável não foi encontrado")));
                    move.setFrom(patrimony.getCurrentLocation());
                    move.setTo(patrimonyLocationService.findById(move.getTo().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Localização não encontrada")));

                    PatrimonyMove saved = patrimonyMoveService.insert(move).orElseThrow(
                            () -> new IllegalArgumentException("Não foi possível salvar a movimentação"));
                    patrimony.getMoves().add(saved);
                    patrimony.setCurrentLocation(saved.getTo());

                    return patrimonyRepository.save(patrimony);
                });
    }

    @Transactional
    public List<Patrimony> movePatrimonyFromServiceOrder(List<PatrimonyMove> moves) {
        return moves.stream()
                .map(move -> {
                    Optional<Patrimony> found = patrimonyRepository.findByCodeIgnoreCase(move.getPatrimony().getCode());
                    Patrimony patrimony;
                    if (found.isEmpty()) {
                        patrimony = this.insertByTechnician(move.getPatrimony())
                                .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar este patrimônio. Código " +
                                        move.getPatrimony().getCode()));
                    } else {
                        patrimony = found.get();

                        if (!patrimony.getProduct().equals(move.getPatrimony().getProduct())) {
                            throw new IllegalArgumentException("O patrimônio " +
                                    move.getPatrimony().getCode() + " não corresponde ao produto informado.");
                        }
                    }

                    if (patrimony.getMoves() == null) {
                        patrimony.setMoves(new ArrayList<>());
                    }

                    move.setPatrimony(patrimony);
                    move.setResponsible(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Responsável não foi encontrado")));
                    move.setFrom(patrimony.getCurrentLocation());

                    move.setTo(patrimonyLocationService.findByCode(move.getTo().getCode())
                            .orElseGet(() -> this.patrimonyLocationService.insert(move.getTo())
                                    .orElseThrow(() -> new IllegalArgumentException("Não foi possível salvar o local de destino do patrimônio"))));

                    PatrimonyMove saved = patrimonyMoveService.insert(move).orElseThrow(
                            () -> new IllegalArgumentException("Não foi possível salvar a movimentação"));
                    patrimony.getMoves().add(saved);
                    patrimony.setCurrentLocation(saved.getTo());

                    return patrimonyRepository.save(patrimony);

                })
                .collect(Collectors.toList());

    }

    public Optional<Patrimony> hidePatrimony(Long id) {
        return patrimonyRepository.findById(id)
                .map(saved -> {
                    saved.setIsVisible(Boolean.FALSE);

                    return patrimonyRepository.save(saved);
                });
    }

}
