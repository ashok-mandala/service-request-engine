package com.wissen.engine.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDTO {
    private String approverEmail;
    private Boolean approved;
    private String comments;
}
