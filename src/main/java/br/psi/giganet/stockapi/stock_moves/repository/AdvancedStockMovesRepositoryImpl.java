package br.psi.giganet.stockapi.stock_moves.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.common.utils.ReflectionUtil;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveSimpleReportProjection;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.stock_moves.model.TechnicianStockMove;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class AdvancedStockMovesRepositoryImpl implements AdvancedStockMovesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<? extends StockMove> findAll(List<String> queries, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StockMove> criteria = builder.createQuery(StockMove.class);

        Root<StockMove> root = criteria.from(StockMove.class);

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getPredicate(query, root, builder);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates);

        }

        criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        TypedQuery<? extends StockMove> query = entityManager.createQuery(criteria);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countFindAll(queries));
    }

    public Page<? extends StockMove> findAll(List<String> queries, HashMap<String, Object> criteries,
            BranchOffice branchOffice, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StockMove> criteria = builder.createQuery(StockMove.class);

        Root<StockMove> root = criteria.from(StockMove.class);

        Predicate branchOfficePredicate = builder.or(
                builder.isNull(root.get("branchOffice")),
                builder.equal(root.get("branchOffice"), branchOffice));

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getPredicate(query, root, builder);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());

            predicateList.addAll(getCriteriesToPredicate(criteries, root, builder));

            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates, branchOfficePredicate);

        } else if (criteries != null && !criteries.isEmpty()) {
            List<Predicate> predicateList = getCriteriesToPredicate(criteries, root, builder);
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates, branchOfficePredicate);

        } else {
            criteria.where(branchOfficePredicate);
        }

        criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        TypedQuery<? extends StockMove> query = entityManager.createQuery(criteria);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countFindAll(queries, branchOffice));
    }


    private List<Predicate> getCriteriesToPredicate(HashMap<String, Object> criteries, Root<StockMove> root, CriteriaBuilder builder) {
        if (criteries == null || criteries.isEmpty()) {
            return Collections.emptyList();
        }

        Join<Product, StockMove> product = root.join("product", JoinType.INNER);

        Join<Stock, StockMove> from = root.join("from", JoinType.LEFT).join("stock", JoinType.LEFT);
        Join<Stock, StockMove> to = root.join("to", JoinType.LEFT).join("stock", JoinType.LEFT);

        List<Predicate> predicateCriteriesList = new ArrayList<>();
        criteries.forEach((key, value) -> {
            switch (key) {

            case "product":
                predicateCriteriesList.add(builder.equal(product.get("id"), String.valueOf(value)));
                break;

            case "from":
                predicateCriteriesList.add(builder.equal(from.get("id"), value));
                break;

            case "to":
                predicateCriteriesList.add(builder.equal(to.get("id"), value));
                break;

            default:
                break;
            }
        });
        return predicateCriteriesList;
    }

    public Page<? extends StockMove> findAll(List<String> queries, BranchOffice branchOffice, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StockMove> criteria = builder.createQuery(StockMove.class);

        Root<StockMove> root = criteria.from(StockMove.class);

        Predicate branchOfficePredicate = builder.or(
                builder.isNull(root.get("branchOffice")),
                builder.equal(root.get("branchOffice"), branchOffice));

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getPredicate(query, root, builder);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates, branchOfficePredicate);

        } else {
            criteria.where(branchOfficePredicate);
        }

        criteria.orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder));

        TypedQuery<? extends StockMove> query = entityManager.createQuery(criteria);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countFindAll(queries, branchOffice));
    }

    private Long countFindAll(List<String> queries, BranchOffice branchOffice) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<StockMove> root = criteria.from(StockMove.class);

        Predicate branchOfficePredicate = builder.or(
                builder.isNull(root.get("branchOffice")),
                builder.equal(root.get("branchOffice"), branchOffice));

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getPredicate(query, root, builder);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates, branchOfficePredicate);

        } else {
            criteria.where(branchOfficePredicate);
        }

        criteria.select(builder.count(root));
        return entityManager.createQuery(criteria).getSingleResult();
    }

    private Long countFindAll(List<String> queries) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<StockMove> root = criteria.from(StockMove.class);

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getPredicate(query, root, builder);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates);

        }

        criteria.select(builder.count(root));
        return entityManager.createQuery(criteria).getSingleResult();
    }

    private Predicate getPredicate(String query, Root<StockMove> root, CriteriaBuilder builder) {
        final String operators = "[:<>~]";
        Pattern pattern = Pattern.compile("[A-Za-z]+" + operators + "\\X*");

        Join<Product, StockMove> product = root.join("product", JoinType.INNER);

        Join<Stock, StockMove> from = root.join("from", JoinType.LEFT).join("stock", JoinType.LEFT);
        Join<Stock, StockMove> to = root.join("to", JoinType.LEFT).join("stock", JoinType.LEFT);

        Join<Employee, StockMove> requester = root.join("requester", JoinType.INNER);
        Join<Employee, StockMove> responsible = root.join("responsible", JoinType.LEFT);

        if (pattern.matcher(query).matches()) {
            String[] fields = query.split(operators);
            final String key = fields[0];
            final String value = fields.length > 1 ? fields[1] : "";
            final String operator = query.replaceAll("(" + key + "|" + value + ")", "");

            switch (key) {
            case "type":
                return builder.equal(root.get("type"), MoveType.valueOf(value));

            case "requester":
                return builder.like(builder.upper(requester.get("name")), "%" + value.toUpperCase() + "%");

            case "responsible":
                return builder.like(builder.upper(responsible.get("name")), "%" + value.toUpperCase() + "%");

            case "id":
                return builder.equal(root.get("id"), value);

            case "createdDate":
                return operator.equals(">") ? builder.greaterThanOrEqualTo(
                        root.get("createdDate").as(LocalDate.class), LocalDate.parse(value)) :
                        operator.equals("<") ? builder.lessThanOrEqualTo(
                                root.get("createdDate").as(LocalDate.class), LocalDate.parse(value)) :
                                null;

            case "lastModifiedDate":
                return operator.equals(">") ? builder.greaterThanOrEqualTo(
                        root.get("lastModifiedDate").as(LocalDate.class), LocalDate.parse(value)) :
                        operator.equals("<") ? builder.lessThanOrEqualTo(
                                root.get("lastModifiedDate").as(LocalDate.class), LocalDate.parse(value)) :
                                null;

            case "product":
                return builder.like(builder.upper(product.get("name")), "%" + value.toUpperCase() + "%");

            case "status":
                return builder.equal(root.get("status"), MoveStatus.valueOf(value));

            case "from":
                return builder.like(builder.upper(from.get("name")), "%" + value.toUpperCase() + "%");

            case "to":
                return builder.like(builder.upper(to.get("name")), "%" + value.toUpperCase() + "%");

            case "serviceOrder":
                return builder.like(builder.treat(root, TechnicianStockMove.class).get("orderId"), value);

            case "customer":
                return builder.like(builder.treat(root, TechnicianStockMove.class).get("customerName"),
                        "%" + value.toUpperCase() + "%");

            default:
                return null;

            }

        }

        return null;
    }

    public Page<? extends StockMoveSimpleReportProjection> findAllStockMovesSimpleReport(List<String> groupProperties, List<String> queries,
            BranchOffice branchOffice, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<? extends Object[]> criteria = builder.createQuery(Object[].class);

        Root<StockMove> root = criteria.from(StockMove.class);

        Predicate branchOfficePredicate = builder.or(
                builder.isNull(root.get("branchOffice")),
                builder.equal(root.get("branchOffice"), branchOffice));

        if (queries != null && !queries.isEmpty()) {
            List<Predicate> predicateList = queries.stream()
                    .map(query -> {
                        Predicate predicate = getPredicate(query, root, builder);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Consulta inválida. '" + query + "'");
                        }
                        return predicate;
                    })
                    .collect(Collectors.toList());
            Predicate[] queryPredicates = new Predicate[predicateList.size()];
            Predicate queryAndPredicates = builder.and(predicateList.toArray(queryPredicates));

            criteria.where(queryAndPredicates, branchOfficePredicate);
        } else {
            criteria.where(branchOfficePredicate);
        }

        List<String> properties = new ArrayList<>();

        criteria.multiselect(getGroupsExpression(builder, criteria, root, groupProperties, properties));

        TypedQuery<? extends Object[]> query = entityManager.createQuery(criteria);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<StockMoveSimpleReportProjection> results =
                ReflectionUtil.transform(
                        StockMoveSimpleReportProjection.class,
                        query.getResultList(),
                        properties
                );

        return new PageImpl<>(results, pageable, 1000);
    }

    private List<Selection<?>> getGroupsExpression(CriteriaBuilder builder, CriteriaQuery<?> criteria, Root<StockMove> root,
            List<String> groupProperties, List<String> properties) {
        Join<Product, StockMove> product = root.join("product", JoinType.INNER);
        Join<Stock, StockMove> from = root.join("from", JoinType.LEFT).join("stock", JoinType.LEFT);
        Join<Stock, StockMove> to = root.join("to", JoinType.LEFT).join("stock", JoinType.LEFT);

        List<Selection<?>> groupSelections = new ArrayList<>();
        criteria.groupBy(new ArrayList<>());
        criteria.orderBy(new ArrayList<>());

        groupProperties.forEach(queryProperty -> {
            switch (queryProperty) {
            case "from":
                properties.add("idFrom");
                properties.add("from");

                groupSelections.add(from.get("id").alias("idFrom"));
                groupSelections.add(from.get("name").alias("from"));

                criteria.getGroupList().add(from.get("id"));
                criteria.getGroupList().add(from.get("name"));
                criteria.getOrderList().add(builder.asc(from.get("name")));
                break;

            case "to":
                properties.add("idTo");
                properties.add("to");

                groupSelections.add(to.get("id").alias("idTo"));
                groupSelections.add(to.get("name").alias("to"));

                criteria.getGroupList().add(to.get("id"));
                criteria.getGroupList().add(to.get("name"));
                criteria.getOrderList().add(builder.asc(to.get("name")));
                break;

            case "product":
                properties.add("idProduct");
                properties.add("product");

                groupSelections.add(product.get("id").alias("idProduct"));
                groupSelections.add(product.get("name").alias("product"));

                criteria.getGroupList().add(product.get("id"));
                criteria.getGroupList().add(product.get("name"));
                criteria.getOrderList().add(builder.asc(product.get("name")));
                break;

            default:
                throw new IllegalArgumentException("Agrupamento de campo inválido '" + queryProperty + "'");
            }
        });
        properties.add("total");

        groupSelections.add(builder.count(root.get("id"))
                .alias("total"));

        return groupSelections;
    }

}
