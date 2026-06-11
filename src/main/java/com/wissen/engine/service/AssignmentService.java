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
        Operator result = null;
        switch (strategy) {
            case SKILL_LOAD_BALANCE:
                result = findBySkillLoadBalance(request.getType().name(), available);
                break;
            case ROUND_ROBIN:
            case LEAST_LOADED:
                result = findByLeastLoaded(available);
                break;
            case FIRST_AVAILABLE:
                result = findFirstAvailable(request.getType().name(), available);
                break;
            default:
                log.warn("Unknown assignment strategy: {}", strategy);
        };
        return result;
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
