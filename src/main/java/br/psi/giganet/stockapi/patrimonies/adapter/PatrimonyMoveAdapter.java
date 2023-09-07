package br.psi.giganet.stockapi.patrimonies.adapter;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.patrimonies.factory.PatrimonyFactory;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import br.psi.giganet.stockapi.patrimonies_locations.factory.PatrimonyLocationFactory;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.stock_moves.controller.request.BasicInsertMoveFromServiceOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PatrimonyMoveAdapter {

    @Autowired
    private PatrimonyLocationFactory patrimonyLocationFactory;

    @Autowired
    private PatrimonyFactory patrimonyFactory;

    @Autowired
    private ProductAdapter productAdapter;

    public List<PatrimonyMove> transform(BasicInsertMoveFromServiceOrderRequest request, String userId) {
        PatrimonyLocation customerLocation = patrimonyLocationFactory.create(
                request.getCustomerId(),
                "CLIENTE - " + request.getCustomerName() + " - " + request.getCustomerId(),
                PatrimonyLocationType.CUSTOMER);
        PatrimonyLocation technicianLocation = patrimonyLocationFactory.create(userId);

        return Stream.concat(
                request.getOutgoingItems().stream()
                        .filter(item -> item.getPatrimonies() != null && !item.getPatrimonies().isEmpty())
                        .flatMap(item -> {
                            if (item.getPatrimonies().size() > item.getQuantity()) {
                                throw new IllegalArgumentException("O número de patrimônios informado é superior a" +
                                        " quantidade informada para o produto " + item.getProduct());
                            }

                            return item.getPatrimonies().stream()
                                    .map(code -> {
                                        Patrimony patrimony = patrimonyFactory.create(code.replaceAll("[:.-]", ""));
                                        patrimony.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
                                        patrimony.setIsVisible(Boolean.TRUE);
                                        patrimony.setProduct(productAdapter.create(item.getProduct()));
                                        patrimony.setCurrentLocation(technicianLocation);
                                        patrimony.setNote("Ordem de serviço: " + request.getOrderId());
                                        patrimony.setMoves(new ArrayList<>());

                                        return patrimonyFactory.create(patrimony, customerLocation);
                                    });
                        }),
                request.getEntryItems().stream()
                        .filter(item -> item.getPatrimonies() != null && !item.getPatrimonies().isEmpty())
                        .flatMap(item -> {
                            if (item.getPatrimonies().size() > item.getQuantity()) {
                                throw new IllegalArgumentException("O número de patrimônios informado é superior a" +
                                        " quantidade informada para o produto " + item.getProduct());
                            }

                            return item.getPatrimonies().stream()
                                    .map(code -> {
                                        Patrimony patrimony = patrimonyFactory.create(code.replaceAll("[:.-]", ""));
                                        patrimony.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
                                        patrimony.setIsVisible(Boolean.TRUE);
                                        patrimony.setProduct(productAdapter.create(item.getProduct()));
                                        patrimony.setCurrentLocation(customerLocation);
                                        patrimony.setNote("Ordem de serviço: " + request.getOrderId());
                                        patrimony.setMoves(new ArrayList<>());

                                        return patrimonyFactory.create(patrimony, technicianLocation);
                                    });
                        }))
                .collect(Collectors.toList());
    }
}
