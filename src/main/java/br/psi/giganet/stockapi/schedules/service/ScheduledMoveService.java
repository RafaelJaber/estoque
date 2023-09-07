package br.psi.giganet.stockapi.schedules.service;

import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.schedules.model.ScheduledMove;
import br.psi.giganet.stockapi.schedules.model.ScheduledMoveItem;
import br.psi.giganet.stockapi.schedules.model.ScheduledStatus;
import br.psi.giganet.stockapi.schedules.repository.ScheduledMoveRepository;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.stock_moves.factory.MoveFactory;
import br.psi.giganet.stockapi.stock_moves.model.ScheduledStockMove;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduledMoveService {

    @Autowired
    private ScheduledMoveRepository scheduledMoveRepository;

    @Autowired
    private StockMovesAdapter stockMovesAdapter;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockMovesService stockMovesService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private MoveFactory moveFactory;

    @Autowired
    private BranchOfficeService branchOfficeService;

    @Transactional
    public Optional<ScheduledMove> insert(ScheduledMove scheduled) {
        scheduled.setResponsible(this.employeeService.getCurrentLoggedEmployee()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

        scheduled.setFrom(scheduled.getFrom() != null ?
                stockService.findById(scheduled.getFrom().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque de destino não encontrado")) : null);

        scheduled.setTo(scheduled.getFrom() != null ?
                stockService.findById(scheduled.getTo().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque de destino não encontrado")) : null);

        if (!scheduled.isSameBranchOffice()) {
            throw new IllegalArgumentException("Agendamento de movimentações são permitidas apenas entre estoques de uma mesma filial");
        }
        scheduled.setBranchOffice(branchOfficeService.getCurrentBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

        scheduled.getItems().forEach(scheduleMove -> {
            scheduleMove.setFrom(scheduled.getFrom() != null ?
                    scheduled.getFrom().find(scheduleMove.getProduct())
                            .orElseGet(() -> stockService.saveStockItem(scheduled.getFrom(), scheduleMove.getProduct())) : null);

            scheduleMove.setTo(scheduled.getTo() != null ?
                    stockService.findByStockAndProductId(scheduleMove.getTo().getStock(), scheduleMove.getProduct().getId())
                            .orElseGet(() -> stockService.saveStockItem(scheduled.getTo(), scheduleMove.getProduct())) : null);

        });

        return Optional.of(scheduledMoveRepository.save(scheduled));
    }

    public Optional<ScheduledMove> update(Long id, ScheduledMove scheduled) {
        return this.scheduledMoveRepository.findById(id)
                .map(saved -> {
                    if (!saved.getStatus().equals(ScheduledStatus.SCHEDULED)) {
                        throw new IllegalArgumentException("Este agendamento não pode ser mais alterado devido à sua situação atual");
                    }
                    saved.setExecution(scheduled.getExecution());
                    saved.setType(scheduled.getType());
                    saved.setDate(scheduled.getDate());
                    saved.setNote(scheduled.getNote());

                    saved.setFrom(scheduled.getFrom() != null ?
                            stockService.findById(scheduled.getFrom().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Estoque de destino não encontrado")) : null);

                    saved.setTo(scheduled.getTo() != null ?
                            stockService.findById(scheduled.getTo().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Estoque de destino não encontrado")) : null);

                    if (!saved.isSameBranchOffice()) {
                        throw new IllegalArgumentException("Agendamento de movimentações são permitidas apenas entre estoques de uma mesma filial");
                    }
                    saved.setBranchOffice(branchOfficeService.getCurrentBranchOffice()
                            .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

                    scheduled.getItems().removeIf(item -> !scheduled.getItems().contains(item));

                    saved.getItems().stream()
                            .filter(item -> scheduled.getItems().contains(item))
                            .forEach(savedItem -> {
                                final int index = scheduled.getItems().indexOf(savedItem);
                                ScheduledMoveItem scheduleMove = scheduled.getItems().get(index);

                                savedItem.setFrom(saved.getFrom() != null ?
                                        saved.getFrom().find(scheduleMove.getProduct())
                                                .orElseGet(() -> stockService.saveStockItem(saved.getFrom(), scheduleMove.getProduct())) : null);

                                savedItem.setTo(saved.getTo() != null ?
                                        saved.getTo().find(scheduleMove.getProduct())
                                                .orElseGet(() -> stockService.saveStockItem(saved.getTo(), scheduleMove.getProduct())) : null);

                                savedItem.setQuantity(scheduleMove.getQuantity());
                            });

                    scheduled.getItems().stream()
                            .filter(item -> !saved.getItems().contains(item))
                            .forEach(scheduleMove -> {
                                scheduleMove.setFrom(saved.getFrom() != null ?
                                        saved.getFrom().find(scheduleMove.getProduct())
                                                .orElseGet(() -> stockService.saveStockItem(saved.getFrom(), scheduleMove.getProduct())) : null);

                                scheduleMove.setTo(saved.getTo() != null ?
                                        saved.getTo().find(scheduleMove.getProduct())
                                                .orElseGet(() -> stockService.saveStockItem(saved.getTo(), scheduleMove.getProduct())) : null);

                                scheduleMove.setScheduled(saved);
                                saved.getItems().add(scheduleMove);
                            });

                    return scheduledMoveRepository.save(saved);
                });
    }

    public Optional<ScheduledMove> updateOnError(ScheduledMove scheduled) {
        scheduled.setStatus(ScheduledStatus.FAILED);
        return Optional.of(scheduledMoveRepository.save(scheduled));
    }

    public List<ScheduledMove> findAllBetween(ZonedDateTime initialDate, ZonedDateTime finalDate) {
        return this.scheduledMoveRepository.findAllBetween(initialDate, finalDate, Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Page<ScheduledMove> findAllByCurrentBranchOffice(Integer page, Integer pageSize) {
        return this.scheduledMoveRepository.findAllByBranchOffice(
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public List<ScheduledMove> findAllScheduledBetween(ZonedDateTime initialDate, ZonedDateTime finalDate) {
        return this.scheduledMoveRepository.findAllScheduledBetween(initialDate, finalDate, Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Optional<ScheduledMove> findById(Long id) {
        return this.scheduledMoveRepository.findById(id);
    }

    public List<ScheduledMove> findAllAutomaticAndScheduledBetween(ZonedDateTime initialDate, ZonedDateTime finalDate) {
        return scheduledMoveRepository.findAllAutomaticAndScheduledBetween(initialDate, finalDate, Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Optional<ScheduledMove> cancel(Long id) {
        return this.scheduledMoveRepository.findById(id)
                .map(saved -> {
                    if (!saved.getStatus().equals(ScheduledStatus.SCHEDULED)) {
                        throw new IllegalArgumentException("Agendamento já executado");
                    }
                    saved.setStatus(ScheduledStatus.CANCELED);
                    return scheduledMoveRepository.save(saved);
                });
    }

    @Transactional
    public Optional<ScheduledMove> execute(Long id) {
        return this.scheduledMoveRepository.findById(id)
                .map(saved -> {
                    if (!saved.getStatus().equals(ScheduledStatus.SCHEDULED)) {
                        throw new IllegalArgumentException("Agendamento já executado");
                    }
                    saved.setResponsible(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));
                    this.execute(saved);
                    return saved;
                });
    }

    @Transactional
    public void execute(ScheduledMove scheduled) {
        scheduled.setStatus(ScheduledStatus.IN_PROGRESS);
        scheduledMoveRepository.save(scheduled);

        List<ScheduledStockMove> executed = stockMovesService.insertScheduledMove(
                scheduled.getItems()
                        .stream()
                        .filter(item -> item.getMove() == null)
                        .map(item -> moveFactory.createScheduleStockMove(item))
                        .collect(Collectors.toList()));

        if (executed != null && executed.size() == scheduled.getItems().size()) {
            executed.forEach(executedMove -> executedMove.getScheduledMoveItem().setMove(executedMove));
            scheduled.setStatus(ScheduledStatus.SUCCESS);
            scheduled.setDescription("Realizado com sucesso!");

        } else {
            scheduled.setStatus(ScheduledStatus.FAILED);
            scheduled.setDescription("Horário: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "    Motivo: SEM DESCRIÇÃO");
        }
        scheduledMoveRepository.save(scheduled);
    }

}
