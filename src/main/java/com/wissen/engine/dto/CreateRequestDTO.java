package com.wissen.engine.dto;

import com.wissen.engine.domain.ServiceRequest;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequestDTO {
    private ServiceRequest.RequestType type;
    private ServiceRequest.Priority priority;
    private Boolean sensitivity;
    private String title;
    private String description;
    private String requesterEmail;
}
