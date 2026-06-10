package com.wissen.engine.service;

import com.wissen.engine.domain.Operator;
import com.wissen.engine.domain.ServiceRequest;
import com.wissen.engine.repository.OperatorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssignmentService {
    @Autowired
    private OperatorRepository operatorRepository;

    @Value("${app.assignment.max-capacity:5}")
    private Integer maxCapacity;

    public Operator assignRequest(ServiceRequest request, AssignmentStrategy strategy) {
        List<Operator> available = getAvailableOperators();
        if (available.isEmpty()) return null;

        return switch (strategy) {
            case SKILL_LOAD_BALANCE -> findBySkillLoadBalance(request.getType().name(), available);
            case ROUND_ROBIN -> findByLeastLoaded(available);
            case LEAST_LOADED -> findByLeastLoaded(available);
            case FIRST_AVAILABLE -> findFirstAvailable(request.getType().name(), available);
        };
    }

    private Operator findBySkillLoadBalance(String requestType, List<Operator> available) {
        List<Operator> qualified = available.stream()
            .filter(op -> op.hasSkill(requestType))
            .filter(op -> op.canAcceptMoreWork(maxCapacity))
            .collect(Collectors.toList());

        if (qualified.isEmpty()) {
            qualified = available.stream()
                .filter(op -> op.canAcceptMoreWork(maxCapacity))
                .collect(Collectors.toList());
        }

        if (qualified.isEmpty()) return null;
        qualified.sort(Comparator.comparingInt(Operator::getLoad));
        return qualified.get(0);
    }

    private Operator findByLeastLoaded(List<Operator> available) {
        List<Operator> qualified = available.stream()
            .filter(op -> op.canAcceptMoreWork(maxCapacity))
            .collect(Collectors.toList());

        if (qualified.isEmpty()) return null;
        qualified.sort(Comparator.comparingInt(Operator::getLoad));
        return qualified.get(0);
    }

    private Operator findFirstAvailable(String requestType, List<Operator> available) {
        return available.stream()
            .filter(op -> op.hasSkill(requestType) && op.canAcceptMoreWork(maxCapacity))
            .findFirst()
            .orElseGet(() -> available.stream()
                .filter(op -> op.canAcceptMoreWork(maxCapacity))
                .findFirst()
                .orElse(null));
    }

    public List<Operator> getAvailableOperators() {
        return operatorRepository.findByAvailableTrue();
    }

    public List<Operator> getAllOperators() {
        return operatorRepository.findAll();
    }

    public void incrementLoad(String operatorId) {
        Operator op = operatorRepository.findById(operatorId).orElse(null);
        if (op != null) {
            op.setLoad(op.getLoad() + 1);
            operatorRepository.save(op);
        }
    }

    public void decrementLoad(String operatorId) {
        Operator op = operatorRepository.findById(operatorId).orElse(null);
        if (op != null && op.getLoad() > 0) {
            op.setLoad(op.getLoad() - 1);
            operatorRepository.save(op);
        }
    }

    public void setOperatorAvailability(String operatorId, Boolean available) {
        Operator op = operatorRepository.findById(operatorId).orElse(null);
        if (op != null) {
            op.setAvailable(available);
            operatorRepository.save(op);
        }
    }
}
