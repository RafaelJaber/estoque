package br.psi.giganet.stockapi.common.utils.model.enums;

public enum Settings {

    SHED_IPATINGA_HORTO_ID(1L),
    MAINTENANCE_IPATINGA_HORTO_ID(2L),
    OBSOLETE_IPATINGA_HORTO_ID(3L),
    DEFECTIVE_IPATINGA_HORTO_ID(4L),
    CUSTOMERS_IPATINGA_HORTO_ID(5L),

    SHED_GOVERNADOR_VALADARES_ID(6L),
    MAINTENANCE_GOVERNADOR_VALADARES_ID(7L),
    OBSOLETE_GOVERNADOR_VALADARES_ID(8L),
    DEFECTIVE_GOVERNADOR_VALADARES_ID(9L),
    CUSTOMERS_GOVERNADOR_VALADARES_ID(10L);

    private final Object value;

    Settings(Object value) {
        this.value = value;
    }

    public Object get() {
        return value;
    }

    public <T> T get(Class<T> type) {
        try {
            return type.cast(this.value);
        } catch (Exception e) {
            return null;
        }
    }

}
