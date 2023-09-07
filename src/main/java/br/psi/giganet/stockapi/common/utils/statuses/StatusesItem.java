package br.psi.giganet.stockapi.common.utils.statuses;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;

public interface StatusesItem {
    ProcessStatus getStatus();
}
