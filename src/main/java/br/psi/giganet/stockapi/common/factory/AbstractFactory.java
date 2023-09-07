package br.psi.giganet.stockapi.common.factory;

import br.psi.giganet.stockapi.common.utils.ReflectionUtil;

public class AbstractFactory<E> {

    private Class<E> type;

    /**
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> E create(T t) {
        return ReflectionUtil.transform(this.type, t);
    }

    /**
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> E merge(E e, T t) {
        return ReflectionUtil.merge(e, t);
    }
}
