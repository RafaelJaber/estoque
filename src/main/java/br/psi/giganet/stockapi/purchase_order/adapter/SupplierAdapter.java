package br.psi.giganet.stockapi.purchase_order.adapter;

import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.purchase_order.controller.request.PurchaseOrderSupplierRequest;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderSupplierProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderSupplierResponse;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderSupplier;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class SupplierAdapter {

    public PurchaseOrderSupplier create(final String id) {
        final PurchaseOrderSupplier s = new PurchaseOrderSupplier();
        s.setId(id);
        return s;
    }


    public PurchaseOrderSupplier transform(PurchaseOrderSupplierRequest request) {
        PurchaseOrderSupplier s = new PurchaseOrderSupplier();
        s.setId(request.getId());
        s.setName(request.getName());
        s.setCellphone(request.getCellphone());
        s.setTelephone(request.getTelephone());
        s.setEmail(request.getEmail());
        s.setDescription(request.getDescription());
        s.setCnpj(request.getCnpj());
        s.setCpf(request.getCpf());
        s.setStateRegistration(request.getStateRegistration());
        s.setMunicipalRegistration(request.getMunicipalRegistration());

        s.setAddress(new Address());
        s.getAddress().setPostalCode(request.getAddress().getPostalCode());
        s.getAddress().setComplement(request.getAddress().getComplement());
        s.getAddress().setCity(request.getAddress().getCity() != null ? request.getAddress().getCity() : "-");
        s.getAddress().setDistrict(request.getAddress().getDistrict() != null ? request.getAddress().getDistrict() : "-");
        s.getAddress().setNumber(request.getAddress().getNumber() != null ? request.getAddress().getNumber() : "0");
        s.getAddress().setState(request.getAddress().getState() != null ? request.getAddress().getState() : "MG");
        s.getAddress().setStreet(request.getAddress().getStreet() != null ? request.getAddress().getStreet() : "-");

        return s;
    }

    public PurchaseOrderSupplierProjection transform(PurchaseOrderSupplier supplier) {
        PurchaseOrderSupplierProjection s = new PurchaseOrderSupplierProjection();
        s.setName(supplier.getName());
        s.setId(supplier.getId());

        return s;
    }

    public PurchaseOrderSupplierProjection transform(String id, String name) {
        PurchaseOrderSupplierProjection s = new PurchaseOrderSupplierProjection();
        s.setName(name);
        s.setId(id);

        return s;
    }

    @Transactional
    public PurchaseOrderSupplierResponse transformToResponse(PurchaseOrderSupplier supplier) {
        PurchaseOrderSupplierResponse response = new PurchaseOrderSupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setCellphone(supplier.getCellphone());
        response.setTelephone(supplier.getTelephone());
        response.setEmail(supplier.getEmail());
        response.setDescription(supplier.getDescription());
        response.setCnpj(supplier.getCnpj());
        response.setStateRegistration(supplier.getStateRegistration());
        response.setMunicipalRegistration(supplier.getMunicipalRegistration());

        if (supplier.getAddress() != null) {
            if (response.getAddress() == null) {
                response.setAddress(new Address());
            }

            response.getAddress().setComplement(supplier.getAddress().getComplement());
            response.getAddress().setPostalCode(supplier.getAddress().getPostalCode());
            response.getAddress().setCity(supplier.getAddress().getCity());
            response.getAddress().setDistrict(supplier.getAddress().getDistrict());
            response.getAddress().setNumber(supplier.getAddress().getNumber());
            response.getAddress().setState(supplier.getAddress().getState());
            response.getAddress().setStreet(supplier.getAddress().getStreet());
        }

        return response;
    }

}
