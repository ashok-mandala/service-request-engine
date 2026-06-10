package com.wissen.engine.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorDTO {
    private String id;
    private String name;
    private String email;
    private List<String> skills;
    private Integer load;
    private Boolean available;
    private Integer maxCapacity;
}
