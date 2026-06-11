package com.wissen.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.wissen.engine.domain.Operator;
import com.wissen.engine.domain.ServiceRequest;
import com.wissen.engine.repository.OperatorRepository;
import com.wissen.engine.repository.ServiceRequestRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SampleDataLoader implements ApplicationRunner {

    private final OperatorRepository operatorRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SampleDataLoader(OperatorRepository operatorRepository, ServiceRequestRepository serviceRequestRepository) {
        this.operatorRepository = operatorRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Load sample-data.json from classpath
        ClassPathResource resource = new ClassPathResource("sample-data.json");
        if (!resource.exists()) {
            return;
        }

        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);

            // Operators
            JsonNode operatorsNode = root.path("operators");
            List<Operator> operators = new ArrayList<>();
            if (operatorsNode.isArray()) {
                for (JsonNode n : operatorsNode) {
                    Operator op = Operator.builder()
                            .id(n.path("id").asText())
                            .name(n.path("name").asText())
                            .email(n.path("email").asText())
                            .skills(n.path("skills").asText())
                            .load(n.path("load").asInt(0))
                            .available(n.path("available").asBoolean(true))
                            .maxCapacity(n.path("maxCapacity").asInt(5))
                            .build();
                    operators.add(op);
                }
                operatorRepository.saveAll(operators);
            }

            // Service requests
            JsonNode requestsNode = root.path("serviceRequests");
            List<ServiceRequest> requests = new ArrayList<>();
            if (requestsNode.isArray()) {
                for (JsonNode n : requestsNode) {
                    ServiceRequest.ServiceRequestBuilder b = ServiceRequest.builder()
                            .id(UUID.fromString(n.path("id").asText()))
                            .type(ServiceRequest.RequestType.valueOf(n.path("type").asText()))
                            .priority(ServiceRequest.Priority.valueOf(n.path("priority").asText()))
                            .sensitivity(n.path("sensitivity").asBoolean(false))
                            .state(ServiceRequest.RequestState.valueOf(n.path("state").asText()))
                            .title(n.path("title").asText(null))
                            .description(n.path("description").asText(null))
                            .requesterEmail(n.path("requesterEmail").asText(null))
                            .approvalNeeded(n.path("approvalNeeded").asBoolean(false));

                    // optional simple fields
                    if (!n.path("currentOperatorId").isMissingNode() && !n.path("currentOperatorId").isNull()) {
                        b.currentOperatorId(n.path("currentOperatorId").asText(null));
                    }
                    if (!n.path("resolution").isMissingNode() && !n.path("resolution").isNull()) {
                        b.resolution(n.path("resolution").asText(null));
                    }
                    if (!n.path("approved").isMissingNode()) {
                        b.approved(n.path("approved").asBoolean(false));
                    }
                    if (!n.path("escalationCount").isMissingNode()) {
                        b.escalationCount(n.path("escalationCount").asInt(0));
                    }

                    // timestamps: parse if present, otherwise let prePersist set createdAt/stateChangedAt
                    if (!n.path("createdAt").isMissingNode() && !n.path("createdAt").isNull()) {
                        b.createdAt(LocalDateTime.parse(n.path("createdAt").asText()));
                    }
                    if (!n.path("stateChangedAt").isMissingNode() && !n.path("stateChangedAt").isNull()) {
                        b.stateChangedAt(LocalDateTime.parse(n.path("stateChangedAt").asText()));
                    }
                    if (!n.path("assignedAt").isMissingNode() && !n.path("assignedAt").isNull()) {
                        b.assignedAt(LocalDateTime.parse(n.path("assignedAt").asText()));
                    }
                    if (!n.path("completedAt").isMissingNode() && !n.path("completedAt").isNull()) {
                        b.completedAt(LocalDateTime.parse(n.path("completedAt").asText()));
                    }
                    if (!n.path("approvedAt").isMissingNode() && !n.path("approvedAt").isNull()) {
                        b.approvedAt(LocalDateTime.parse(n.path("approvedAt").asText()));
                    }

                    ServiceRequest req = b.build();
                    requests.add(req);
                }
                serviceRequestRepository.saveAll(requests);
            }
        }
    }
}

