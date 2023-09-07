package br.psi.giganet.stockapi.patrimonies.repository;

import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AdvancedPatrimonyRepositoryImpl implements AdvancedPatrimonyRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Patrimony> findAllFetchByIsVisibleAndProductOrCodeOrLocation(List<String> queries, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Patrimony> criteria = builder.createQuery(Patrimony.class);

        Root<Patrimony> root = criteria.from(Patrimony.class);

        Join<Product, Patrimony> product = root.join("product", JoinType.INNER);
        Join<PatrimonyLocation, Patrimony> location = root.join("currentLocation", JoinType.INNER);

        Predicate isVisiblePredicate = builder.equal(root.get("isVisible"), Boolean.TRUE);

        List<Predicate> predicateList = new ArrayList<>();
        if (queries != null && !queries.isEmpty()) {
            queries.forEach(query -> {
                predicateList.add(builder.like(builder.upper(root.get("code")), "%" + query.toUpperCase() + "%"));
                predicateList.add(builder.like(builder.upper(product.get("name")), "%" + query.toUpperCase() + "%"));
                predicateList.add(builder.like(builder.upper(location.get("name")), "%" + query.toUpperCase() + "%"));
            });
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryOrPredicates = builder.or(predicateList.toArray(queryPredicates));

            criteria.where(builder.and(isVisiblePredicate, queryOrPredicates));

        } else {
            criteria.where(isVisiblePredicate);
        }

        criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        TypedQuery<Patrimony> query = entityManager.createQuery(criteria);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                findAllFetchByIsVisibleAndProductOrCodeOrLocationTotalElements(queries));
    }

    private Long findAllFetchByIsVisibleAndProductOrCodeOrLocationTotalElements(List<String> queries) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<Patrimony> root = criteria.from(Patrimony.class);

        Join<Product, Patrimony> product = root.join("product", JoinType.INNER);
        Join<PatrimonyLocation, Patrimony> location = root.join("currentLocation", JoinType.INNER);

        Predicate isVisiblePredicate = builder.equal(root.get("isVisible"), Boolean.TRUE);

        List<Predicate> predicateList = new ArrayList<>();
        if (queries != null && !queries.isEmpty()) {
            queries.forEach(query -> {
                predicateList.add(builder.like(builder.upper(root.get("code")), "%" + query.toUpperCase() + "%"));
                predicateList.add(builder.like(builder.upper(product.get("name")), "%" + query.toUpperCase() + "%"));
                predicateList.add(builder.like(builder.upper(location.get("name")), "%" + query.toUpperCase() + "%"));
            });
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryOrPredicates = builder.or(predicateList.toArray(queryPredicates));

            criteria.where(builder.and(isVisiblePredicate, queryOrPredicates));

        } else {
            criteria.where(isVisiblePredicate);
        }

        criteria.select(builder.count(root));
        return entityManager.createQuery(criteria).getSingleResult();
    }
}
