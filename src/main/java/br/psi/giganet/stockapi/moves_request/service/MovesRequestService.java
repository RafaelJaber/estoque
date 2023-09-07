package br.psi.giganet.stockapi.moves_request.service;

import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.moves_request.model.RequestedMove;
import br.psi.giganet.stockapi.moves_request.repository.MovesRequestRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.service.ProductService;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.stock_moves.factory.MoveFactory;
import br.psi.giganet.stockapi.stock_moves.model.DetachedStockMove;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovesRequestService {

    @Autowired
    private MovesRequestRepository movesRequestRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockMovesService stockMovesService;

    @Autowired
    private MoveFactory moveFactory;

    @Autowired
    private ProductService productService;

    @Autowired
    private BranchOfficeService branchOfficeService;


    @Transactional
    public List<RequestedMove> insertByTechnician(List<RequestedMove> moves) {
        return insert(moves.stream()
                .peek(m -> m.getTo().setStock(
                        stockService.findByUserId(((TechnicianStock) m.getTo().getStock()).getTechnician().getUserId())
                                .orElseThrow(() -> new IllegalArgumentException("Estoque do técnico não foi encontrado"))))
                .collect(Collectors.toList()));
    }

    @Transactional
    public List<RequestedMove> insert(List<RequestedMove> moves) {
        return moves.stream()
                .map(move -> {
                    move.setProduct(productService.findById(move.getProduct().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));

                    move.setFrom(stockService.findByStockAndProductId(move.getFrom().getStock(), move.getProduct().getId())
                            .orElseGet(() -> stockService.saveStockItem(move.getFrom().getStock(), move.getProduct())));

                    move.setTo(stockService.findByStockAndProductId(move.getTo().getStock(), move.getProduct().getId())
                            .orElseGet(() -> stockService.saveStockItem(move.getTo().getStock(), move.getProduct())));

                    if (move.getFrom().equals(move.getTo())) {
                        throw new IllegalArgumentException("Estoque de origem é igual ao de destino");
                    } else if (!move.isSameBranchOffice()) {
                        throw new IllegalArgumentException("Solicitações de movimentações são permitidas apenas entre estoques de uma mesma filial");
                    }

                    move.setBranchOffice(move.getFrom().getStock().getBranchOffice());

                    move.setRequester(employeeService.getCurrentLoggedEmployee()
                            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado")));

                    move.setDescription(getRequestedMoveDescription(move));

                    return movesRequestRepository.save(move);

                })
                .collect(Collectors.toList());
    }

    public Optional<RequestedMove> approve(Long id) {
        return movesRequestRepository.findById(id)
                .filter(request -> request.getStatus().equals(MoveStatus.REQUESTED))
                .map(requested -> {
                    List<DetachedStockMove> executedMove = stockMovesService.insertDetachedMove(
                            Collections.singletonList(moveFactory.createRequestedStockMove(requested)));

                    requested.setMove(executedMove.get(0));
                    requested.setStatus(MoveStatus.REALIZED);

                    return movesRequestRepository.save(requested);
                });
    }

    public Optional<RequestedMove> cancel(Long id) {
        return movesRequestRepository.findById(id)
                .map(move -> {
                    if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
                        throw new IllegalArgumentException("Esta solicitação já foi finalizada, portanto não pode ser mais cancelada");
                    }
                    move.setStatus(MoveStatus.CANCELED);

                    return movesRequestRepository.save(move);
                });
    }

    public Optional<RequestedMove> reject(Long id) {
        return movesRequestRepository.findById(id)
                .map(move -> {
                    if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
                        throw new IllegalArgumentException("Esta solicitação já foi finalizada, portanto não pode ser mais cancelada");
                    }
                    move.setStatus(MoveStatus.REJECTED);

                    return movesRequestRepository.save(move);
                });
    }

    public Optional<RequestedMove> findById(Long id) {
        return movesRequestRepository.findById(id);
    }

    public Page<RequestedMove> findAll(Integer page, Integer pageSize) {
        return movesRequestRepository.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllByCurrentBranchOffice(Integer page, Integer pageSize) {
        return movesRequestRepository.findAllByBranchOffice(
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllPendingByCityStockFrom(CityOptions city, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllPendingByCityStockFrom(city,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllPendingByCityStockTo(CityOptions city, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllPendingByCityStockTo(city,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllPendingByStockTypeFrom(List<StockType> types, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllPendingByStockTypeFromAndBranchOffice(
                types,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllPendingByStockTypeTo(List<StockType> types, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllPendingByStockTypeToAndBranchOffice(
                types,
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllByTechnicianFrom(TechnicianStock technicianStock, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllByStockFrom(
                stockService.findByUserId(technicianStock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllByTechnicianFromAndStatus(TechnicianStock technicianStock, MoveStatus status, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllByStockFromAndStatus(
                stockService.findByUserId(technicianStock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                status,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllByTechnicianTo(TechnicianStock technicianStock, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllByStockTo(
                stockService.findByUserId(technicianStock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    public Page<RequestedMove> findAllByTechnicianToAndStatus(TechnicianStock technicianStock, MoveStatus status, Integer page, Integer pageSize) {
        return movesRequestRepository.findAllByStockToAndStatus(
                stockService.findByUserId(technicianStock.getTechnician().getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")),
                status,
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdDate")));
    }


    private String getRequestedMoveDescription(RequestedMove request) {
        final StockItem from = request.getFrom();
        final StockItem to = request.getTo();
        final double quantity = request.getQuantity();
        final Employee employee = request.getRequester();
        final Product product = request.getProduct();
        String templateMessage = ":move : :employee :type :quantity :unit de :item ( :itemCode ) dia " +
                ZonedDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss"));

        return templateMessage
                .replaceAll(":move", "Entre estoques")
                .replaceAll(":type", "solicitou uma movimentação do estoque " +
                        Objects.requireNonNull(from).getStock().getName() +
                        " para " + to.getStock().getName())
                .replaceAll(":employee", employee.getName())
                .replaceAll(":quantity", Double.toString(quantity))
                .replaceAll(":unit", product.getUnit().getAbbreviation())
                .replaceAll(":itemCode", product.getCode())
                .replaceAll(":item", product.getName());
    }

}
