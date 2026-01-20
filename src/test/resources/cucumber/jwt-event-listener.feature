Feature: Training Workload Event Listener

  Scenario: Process event with valid JWT
    Given a valid JWT token "valid-token" from service "trainer-workload-service"
    When a training event with ID 1 is received
    Then the event should be processed
    And no message should be sent to DLQ

  Scenario: Send to DLQ when JWT is invalid
    Given an invalid JWT token "invalid-token"
    When a training event with ID 2 is received
    Then the event should not be processed
    And a SecurityException should be sent to DLQ with message "Invalid JWT token"

  Scenario: Send to DLQ when service name does not match
    Given a valid JWT token from service "other-service"
    When a training event with ID 3 is received
    Then the event should not be processed
    And a SecurityException should be sent to DLQ with message "Unauthorized service: other-service"

  Scenario: Send to DLQ when Authorization header is missing
    Given a missing Authorization header
    When a training event is received
    Then a SecurityException should be sent to DLQ with message "Missing Authorization header"
