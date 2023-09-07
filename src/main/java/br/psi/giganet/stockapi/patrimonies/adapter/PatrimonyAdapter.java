package br.psi.giganet.stockapi.patrimonies.adapter;

import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.entries.adapter.EntryAdapter;
import br.psi.giganet.stockapi.entries.factory.EntryFactory;
import br.psi.giganet.stockapi.patrimonies.controller.request.*;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyMoveProjection;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyProjection;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyProjectionWithoutUnit;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyResponse;
import br.psi.giganet.stockapi.patrimonies.factory.PatrimonyFactory;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import br.psi.giganet.stockapi.patrimonies_locations.adapter.PatrimonyLocationAdapter;
import br.psi.giganet.stockapi.patrimonies_locations.factory.PatrimonyLocationFactory;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatrimonyAdapter {

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private PatrimonyLocationAdapter locationAdapter;
    @Autowired
    private PatrimonyLocationFactory locationFactory;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private EntryFactory entryFactory;
    @Autowired
    private EntryAdapter entryAdapter;

    @Autowired
    private PatrimonyFactory patrimonyFactory;

    public Patrimony transform(InsertPatrimonyRequest request) {
        Patrimony patrimony = new Patrimony();
        patrimony.setCode(request.getCode().replaceAll("[:.-]", ""));
        patrimony.setCodeType(request.getCodeType());
        patrimony.setIsVisible(Boolean.TRUE);
        patrimony.setProduct(productAdapter.create(request.getProduct()));
        patrimony.setCurrentLocation(locationFactory.create(request.getCurrentLocation()));
        patrimony.setNote(request.getNote());
        patrimony.setMoves(new ArrayList<>());

        return patrimony;
    }

    public Patrimony transform(BasicInsertPatrimonyRequest request) {
        Patrimony patrimony = new Patrimony();
        patrimony.setCode(request.getCode().replaceAll("[:.-]", ""));
        patrimony.setCodeType(PatrimonyCodeType.MAC_ADDRESS);
        patrimony.setIsVisible(Boolean.TRUE);
        patrimony.setProduct(productAdapter.create(request.getProduct()));
        patrimony.setNote(request.getNote());
        patrimony.setMoves(new ArrayList<>());

        return patrimony;
    }

    public PatrimonyMove transform(MovePatrimonyRequest request) {
        PatrimonyMove move = new PatrimonyMove();
        move.setPatrimony(patrimonyFactory.create(request.getPatrimony()));
        move.setTo(locationFactory.create(request.getNewLocation()));
        move.setNote(request.getNote());

        return move;
    }

    public List<Patrimony> transform(BatchInsertPatrimonyRequest request) {
        return request.getCodes().stream()
                .map(code -> {
                    Patrimony patrimony = new Patrimony();
                    patrimony.setCode(code.replaceAll("[:.-]", ""));
                    patrimony.setCodeType(request.getCodeType());
                    patrimony.setIsVisible(Boolean.TRUE);
                    patrimony.setProduct(productAdapter.create(request.getProduct()));
                    patrimony.setCurrentLocation(locationFactory.create(request.getCurrentLocation()));
                    patrimony.setNote(request.getNote());
                    patrimony.setEntryItem(request.getEntryItem() != null ? entryFactory.create(request.getEntryItem()) : null);
                    patrimony.setMoves(new ArrayList<>());

                    return patrimony;
                }).collect(Collectors.toList());
    }

    public Patrimony transform(UpdatePatrimonyRequest request) {
        Patrimony patrimony = new Patrimony();
        patrimony.setProduct(productAdapter.create(request.getProduct()));
        patrimony.setNote(request.getNote());

        return patrimony;
    }

    public PatrimonyProjection transform(Patrimony patrimony) {
        PatrimonyProjection projection = new PatrimonyProjection();
        projection.setId(patrimony.getId());
        projection.setCode(patrimony.getCode());
        projection.setCodeType(patrimony.getCodeType());
        projection.setProduct(productAdapter.transform(patrimony.getProduct()));
        projection.setCurrentLocation(patrimony.getCurrentLocation() != null ?
                locationAdapter.transform(patrimony.getCurrentLocation()) : null);
        return projection;
    }

    public PatrimonyProjectionWithoutUnit transformWithoutUnit(Patrimony patrimony) {
        PatrimonyProjectionWithoutUnit projection = new PatrimonyProjectionWithoutUnit();
        projection.setId(patrimony.getId());
        projection.setCode(patrimony.getCode());
        projection.setCodeType(patrimony.getCodeType());
        projection.setProduct(productAdapter.transformWithoutUnit(patrimony.getProduct()));
        projection.setCurrentLocation(patrimony.getCurrentLocation() != null ?
                locationAdapter.transform(patrimony.getCurrentLocation()) : null);
        return projection;
    }

    public PatrimonyResponse transformToResponse(Patrimony patrimony, boolean withHistory) {
        PatrimonyResponse response = new PatrimonyResponse();
        response.setId(patrimony.getId());
        response.setCode(patrimony.getCode());
        response.setCodeType(patrimony.getCodeType());
        response.setProduct(productAdapter.transform(patrimony.getProduct()));
        response.setCurrentLocation(patrimony.getCurrentLocation() != null ?
                locationAdapter.transform(patrimony.getCurrentLocation()) : null);
        response.setNote(patrimony.getNote());
        response.setEntryItem(patrimony.getEntryItem() != null ?
                entryAdapter.transformToItemWithEntryProjection(patrimony.getEntryItem()) : null);

        if (withHistory) {
            response.setMoves(
                    patrimony.getMoves() != null ?
                            patrimony.getMoves().stream()
                                    .map(move -> {
                                        PatrimonyMoveProjection moveResponse = new PatrimonyMoveProjection();
                                        moveResponse.setFrom(move.getFrom() != null ?
                                                locationAdapter.transform(move.getFrom()) : null);
                                        moveResponse.setTo(move.getTo() != null ?
                                                locationAdapter.transform(move.getTo()) : null);
                                        moveResponse.setDate(move.getCreatedDate());
                                        moveResponse.setId(move.getId());
                                        moveResponse.setResponsible(employeeAdapter.transform(move.getResponsible()));
                                        moveResponse.setNote(move.getNote());

                                        return moveResponse;
                                    })
                                    .collect(Collectors.toList()) :
                            Collections.emptyList());
        }

        return response;
    }

    public PatrimonyResponse transformToResponse(Patrimony patrimony) {
        return transformToResponse(patrimony, false);
    }

}
