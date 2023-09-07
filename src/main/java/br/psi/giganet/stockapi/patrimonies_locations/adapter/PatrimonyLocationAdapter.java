package br.psi.giganet.stockapi.patrimonies_locations.adapter;

import br.psi.giganet.stockapi.patrimonies_locations.controller.request.InsertPatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.controller.request.UpdatePatrimonyLocationRequest;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationProjection;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationResponse;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import org.springframework.stereotype.Component;

@Component
public class PatrimonyLocationAdapter {

    public PatrimonyLocation transform(InsertPatrimonyLocationRequest request) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setName(request.getName());
        location.setCode(request.getCode());
        location.setType(request.getType());
        location.setNote(request.getNote());

        return location;
    }

    public PatrimonyLocation transform(UpdatePatrimonyLocationRequest request) {
        PatrimonyLocation location = new PatrimonyLocation();
        location.setId(request.getId());
        location.setName(request.getName());
        location.setCode(request.getCode());
        location.setType(request.getType());
        location.setNote(request.getNote());

        return location;
    }

    public PatrimonyLocationProjection transform(PatrimonyLocation patrimonyLocation) {
        return transform(patrimonyLocation, ResponseType.PROJECTION);
    }

    public PatrimonyLocationProjection transform(PatrimonyLocation patrimonyLocation, ResponseType type) {
        PatrimonyLocationProjection projection;

        switch (type) {
            case PROJECTION:
                projection = new PatrimonyLocationProjection();
                projection.setId(patrimonyLocation.getId());
                projection.setName(patrimonyLocation.getName());
                projection.setCode(patrimonyLocation.getCode());

                return projection;

            case RESPONSE:
                projection = new PatrimonyLocationResponse();
                projection.setId(patrimonyLocation.getId());
                projection.setName(patrimonyLocation.getName());
                projection.setCode(patrimonyLocation.getCode());
                ((PatrimonyLocationResponse) projection).setType(patrimonyLocation.getType());
                ((PatrimonyLocationResponse) projection).setNote(patrimonyLocation.getNote());

                return projection;

            default:
                return null;
        }
    }


    public enum ResponseType {
        PROJECTION,
        RESPONSE
    }

}
