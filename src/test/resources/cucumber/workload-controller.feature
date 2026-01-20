Feature: Workload Controller

  Scenario: Get monthly summary successfully
    Given the workload service returns a valid summary
    When the client requests monthly summary for "trainer1" year 2025 month 5
    Then the response status should be 200
    And the response should contain username "trainer1"

  Scenario: Generate transactionId when missing
    Given the workload service returns a valid summary
    When the client requests monthly summary without transactionId
    Then the response status should be 200

  Scenario: Service exception results in 404 response
    Given the workload service throws ServiceException
    When the client requests monthly summary for "trainer1" year 2025 month 5
    Then the response status should be 404


