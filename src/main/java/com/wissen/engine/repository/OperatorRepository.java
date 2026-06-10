package com.wissen.engine.repository;

import com.wissen.engine.domain.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, String> {
    List<Operator> findByAvailableTrue();
}
