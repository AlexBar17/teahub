package com.tea.teahub.repository;

import com.tea.teahub.model.Tea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeaStorage extends JpaRepository<Tea, UUID>, JpaSpecificationExecutor<Tea> {

    Page<Tea> findAll(@Nullable Specification<Tea> spec, Pageable pageable);

    Optional<Tea> findByName(String name);

}
