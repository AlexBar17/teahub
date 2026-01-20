package com.tea.teahub.service;

import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

interface TeaSpecification {

    static Specification<Tea> hasType(TeaType type) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }

    static Specification<Tea> hasOriginCountry(String originCountry) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("originCountry"), originCountry);
    }

    static Specification<Tea> hasOriginRegion(String originRegion) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("originRegion"), originRegion);
    }

    static Specification<Tea> hasWordsInName(String[] words) {
        return Arrays.stream(words)
                .map(TeaSpecification::hasWordInName)
                .reduce(Specification.unrestricted(), Specification::and);
    }

    static Specification<Tea> amountGreaterThan(Integer amount) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("amount"), amount);
    }

    static Specification<Tea> amountLessThan(Integer amount) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThan(root.get("amount"), amount);
    }

    private static Specification<Tea> hasWordInName(String word) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + word.toLowerCase() + "%"
                );
    }

    static Specification<Tea> isActive() {
        return ((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("active"), true));
    }
}
