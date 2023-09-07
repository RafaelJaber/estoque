package br.psi.giganet.stockapi.common.utils.statuses;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;

import java.util.List;

public class StatusesUtil {

    public static Boolean isApproved(List<? extends StatusesItem> items) {
        if (items != null) {
            return items
                    .stream()
                    .allMatch(item -> item.getStatus()
                            .equals(ProcessStatus.APPROVED)
                    );
        }
        return Boolean.FALSE;
    }

    public static Boolean isPending(List<? extends StatusesItem> items) {
        if (items != null) {
            return items
                    .stream()
                    .anyMatch(item -> item.getStatus()
                            .equals(ProcessStatus.PENDING)
                    );
        }
        return Boolean.FALSE;
    }

    public static Boolean isPartiallyApproved(List<? extends StatusesItem> items) {
        if (items != null) {
            return !isPending(items) && !isApproved(items) && !isRejected(items) &&
                    (items.stream().filter(item -> item.getStatus().equals(ProcessStatus.APPROVED)).count() >
                            items.stream().filter(item -> item.getStatus().equals(ProcessStatus.REJECTED)).count());
        }
        return Boolean.FALSE;
    }

    public static Boolean isRejected(List<? extends StatusesItem> items) {
        if (items != null) {
            return items
                    .stream()
                    .allMatch(item -> item.getStatus()
                            .equals(ProcessStatus.REJECTED)
                    );
        }
        return Boolean.FALSE;
    }

    public static Boolean isPartiallyRejected(List<? extends StatusesItem> items) {
        if (items != null) {
            return !isPending(items) && !isApproved(items) && !isRejected(items) &&
                    (items.stream().filter(item -> item.getStatus().equals(ProcessStatus.REJECTED)).count() >
                            items.stream().filter(item -> item.getStatus().equals(ProcessStatus.APPROVED)).count());
        }
        return Boolean.FALSE;
    }

    public static Boolean isRealized(List<? extends StatusesItem> items) {
        if (items != null) {
            return items
                    .stream()
                    .allMatch(item -> item.getStatus()
                            .equals(ProcessStatus.REALIZED)
                    );
        }
        return Boolean.FALSE;
    }

    public static ProcessStatus getStatus(List<? extends StatusesItem> items) {
        if (items != null && !items.isEmpty()) {
            if (isApproved(items)) {
                return ProcessStatus.APPROVED;
            } else if (isRejected(items)) {
                return ProcessStatus.REJECTED;
            } else if (isPartiallyApproved(items)) {
                return ProcessStatus.PARTIALLY_APPROVED;
            } else if (isPartiallyRejected(items)) {
                return ProcessStatus.PARTIALLY_REJECTED;
            } else if (isPending(items)) {
                return ProcessStatus.PENDING;
            } else if (isRealized(items)) {
                return ProcessStatus.REALIZED;
            }
            return ProcessStatus.PENDING;
        }
        return null;
    }
}
