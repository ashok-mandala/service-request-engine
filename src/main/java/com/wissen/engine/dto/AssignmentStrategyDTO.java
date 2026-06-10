package com.wissen.engine.dto;

import com.wissen.engine.service.AssignmentStrategy;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentStrategyDTO {
    private AssignmentStrategy strategy;
}
