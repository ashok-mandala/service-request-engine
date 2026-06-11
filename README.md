# Service Request Workflow Engine

A production-grade BPMN-based workflow engine for managing service requests using Spring Boot, Flowable, and H2.

## Quick Start

```bash
git clone https://github.com/ashok-mandala/service-request-engine.git
cd service-request-engine
mvn clean install
mvn spring-boot:run
```

Server: http://localhost:8080  
H2 Console: http://localhost:8080/h2-console

## Key Features

- BPMN Workflow Engine (Flowable 6.7.2)
- Strategy-Based Assignment (SKILL_LOAD_BALANCE, ROUND_ROBIN, LEAST_LOADED)
- Event-Driven Notifications (Email, Slack, SMS, System)
- Comprehensive Audit Trail
- SLA Management with escalation
- Multi-level Approval Gating
- H2 In-Memory Database (upgradeable to PostgreSQL)
- Production-ready Spring Boot

## API Quick Reference

**Submit:** `POST /api/requests`  
**Status:** `GET /api/requests/{id}`  
**List:** `GET /api/requests?state=TRIAGE&priority=HIGH`  
**Assign:** `POST /api/requests/{id}/assign`  
**Auto-Assign:** `POST /api/requests/{id}/auto-assign`  
**Complete:** `POST /api/requests/{id}/complete`  
**Approve:** `POST /api/requests/{id}/approve`  
**Audit:** `GET /api/audit/{id}`  
**Operators:** `GET /api/operators`  

## Process Flow

```
SUBMITTED
    ↓
TRIAGE (15 min SLA)
    ↓
ASSIGN (30 min SLA)
    ↓
WORK (480 min SLA)
    ↓
[Approval Needed?]
    ├─ YES → REVIEW (60 min SLA) → APPROVE/REJECT
    └─ NO  → COMPLETE
```

## Technology Stack

- Spring Boot 2.7.14
- Flowable 6.7.2 BPMN
- Spring Data JPA
- H2 1.4+ Database
- Lombok
- JUnit 4

## Project Structure

```
src/main/java/com/wissen/engine/
├── Application.java                    # Spring Boot entry point
├── domain/                            # Entity models
│   ├── ServiceRequest.java
│   ├── Operator.java
│   ├── AuditLog.java
│   └── Notification.java
├── repository/                        # Spring Data JPA
│   ├── ServiceRequestRepository.java
│   ├── OperatorRepository.java
│   ├── AuditLogRepository.java
│   └── NotificationRepository.java
├── service/                           # Business logic
│   ├── ServiceRequestService.java
│   ├── AssignmentService.java         # Routing strategies
│   ├── AuditService.java
│   ├── NotificationService.java
│   └── AssignmentStrategy.java        # Enum: SKILL_LOAD_BALANCE, etc.
├── event/                             # Spring events
│   ├── ServiceRequestSubmittedEvent.java
│   ├── ServiceRequestAssignedEvent.java
│   ├── ServiceRequestCompletedEvent.java
│   └── ServiceRequestEscalatedEvent.java
├── controller/                        # REST endpoints
│   ├── ServiceRequestController.java
│   ├── OperatorController.java
│   └── AuditController.java
├── listener/                          # Event listeners
│   └── NotificationEventListener.java
├── dto/                               # Data transfer objects
│   ├── ServiceRequestDTO.java
│   ├── OperatorDTO.java
│   ├── AuditLogDTO.java
│   ├── CreateRequestDTO.java
│   ├── ApprovalDTO.java
│   └── AssignmentStrategyDTO.java
└── config/                            # Configuration
    └── application.properties

src/main/resources/
└── bpmn/
    └── service-request-process.bpmn20.xml  # BPMN diagram

src/test/java/com/wissen/engine/
├── service/
│   ├── AssignmentServiceTest.java
│   └── AuditServiceTest.java
└── controller/
    └── ServiceRequestControllerTest.java
```

## Design Highlights

### 1. Flowable BPMN Engine
- Visual process model (editable in Flowable Modeler)
- Native timer support for SLA escalation
- Gateways for conditional routing
- Straightforward to extend

### 2. Strategy-Based Assignment

```java
// Auto-assign using strategy
POST /api/requests/{id}/auto-assign
{"strategy": "SKILL_LOAD_BALANCE"}

// Strategies:
// - SKILL_LOAD_BALANCE: Matches skills, balances load (recommended)
// - ROUND_ROBIN: Cycles through available operators
// - LEAST_LOADED: Lowest current load
// - FIRST_AVAILABLE: First operator with matching skills
```

### 3. Event-Driven Notifications

```java
@EventListener
public void onServiceRequestSubmitted(ServiceRequestSubmittedEvent event) {
    notificationService.notify(
        event.getRequest().getId(),
        SUBMITTED,
        requesterEmail,
        EMAIL,
        "Request received and triaging..."
    );
}
```

### 4. Comprehensive Audit Trail

Every action is recorded:
- Actor (user email or "system")
- Action type (SUBMITTED, ASSIGNED, COMPLETED, etc.)
- Timestamp
- Optional metadata

```
GET /api/audit/{requestId}
[
  {"action": "SUBMITTED", "actor": "user@example.com", "timestamp": "..."},
  {"action": "TRIAGED", "actor": "system", "timestamp": "..."},
  {"action": "ASSIGNED", "actor": "op1", "timestamp": "..."}
]
```

### 5. SLA Management

Configurable in application.properties:
```properties
app.sla.triage=15        # 15 minutes
app.sla.assign=30        # 30 minutes
app.sla.work=480         # 8 hours
app.sla.review=60        # 1 hour
```

On SLA breach:
1. Record escalation event
2. Emit ServiceRequestEscalatedEvent
3. Notify manager via Slack/Email
4. Re-prioritize to higher level

### 6. Approval Gating

Requests requiring approval:
- Priority = HIGH or CRITICAL
- Sensitivity flag = true

These move to REVIEW state and require approver sign-off.

## Running Examples

### Submit a Request

```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{
    "type": "INCIDENT",
    "priority": "HIGH",
    "sensitivity": true,
    "title": "Production Database Down",
    "description": "Primary DB connection lost",
    "requesterEmail": "user@example.com"
  }'
```

### Auto-Assign to Operator

```bash
curl -X POST http://localhost:8080/api/requests/{id}/auto-assign \
  -H "Content-Type: application/json" \
  -d '{"strategy": "SKILL_LOAD_BALANCE"}'
```

### Complete Work

```bash
curl -X POST http://localhost:8080/api/requests/{id}/complete \
  -H "Content-Type: application/json" \
  -d '{"resolution": "Database restarted and verified"}'
```

### Approve Request

```bash
curl -X POST http://localhost:8080/api/requests/{id}/approve \
  -H "Content-Type: application/json" \
  -d '{
    "approverEmail": "manager@company.com",
    "approved": true,
    "comments": "Looks good, proceeding to close"
  }'
```

## Testing

```bash
mvn clean test
```

Tests cover:
- Assignment strategies
- Audit trail recording
- State transitions
- Approval workflows
- Notification delivery


## Quick Commands

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn clean test

# Check code quality
mvn checkstyle:check
```

## Database Schema

Automatically created by JPA/Hibernate:

- `service_request` - Core request data
- `audit_log` - State transition history
- `operator` - Operator profiles and availability
- `notification` - Notification queue and status

Access H2 Console: http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:testdb`  
Username: `sa`  
Password: (blank)
