package br.psi.giganet.stockapi.common.adapter;

import br.psi.giganet.stockapi.common.utils.ReflectionUtil;

public abstract class AbstractAdapter<T> {

    /**
     *
     * @param t
     * @param <T>
     * @return
     */
    protected <P> P transformToProjection(Class<P> clazz, T t) {
        return ReflectionUtil.transform(clazz, t);
    }
}
