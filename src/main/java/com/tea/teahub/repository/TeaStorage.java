package com.tea.teahub.repository;

import com.tea.teahub.model.Tea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeaStorage extends JpaRepository<Tea, UUID>, JpaSpecificationExecutor<Tea> {

}
