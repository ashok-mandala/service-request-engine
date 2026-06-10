package com.wissen.engine.controller;

import com.wissen.engine.domain.Operator;
import com.wissen.engine.dto.OperatorDTO;
import com.wissen.engine.service.AssignmentService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operators")
@Slf4j
public class OperatorController {
    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<OperatorsResponse> listOperators() {
        List<Operator> operators = assignmentService.getAllOperators();
        List<OperatorDTO> dtos = operators.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(OperatorsResponse.builder().total(operators.size()).operators(dtos).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorDTO> getOperator(@PathVariable String id) {
        Operator operator = assignmentService.getAllOperators().stream()
            .filter(op -> op.getId().equals(id)).findFirst().orElseThrow();
        return ResponseEntity.ok(mapToDTO(operator));
    }

    private OperatorDTO mapToDTO(Operator operator) {
        return OperatorDTO.builder()
            .id(operator.getId())
            .name(operator.getName())
            .email(operator.getEmail())
            .skills(operator.getSkillsList())
            .load(operator.getLoad())
            .available(operator.getAvailable())
            .maxCapacity(operator.getMaxCapacity())
            .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperatorsResponse {
        private Integer total;
        private List<OperatorDTO> operators;
    }
}
