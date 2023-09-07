package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;

import java.util.Optional;

public interface MoveHandler<T extends StockMove> {

    Optional<T> create(T move);

    Optional<T> execute(T move);

    Optional<T> approve(T move);

    Optional<T> cancel(T move);

    Optional<T> reject(T move);

    void onMoveFailed(T move, String error) throws IllegalArgumentException;

    void validate(T move) throws IllegalArgumentException;

    boolean requesterHasPermission(T move);

    boolean responsibleHasPermission(T move);
}
