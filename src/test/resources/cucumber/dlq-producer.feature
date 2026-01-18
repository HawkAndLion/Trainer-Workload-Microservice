Feature: DLQ Producer

  Scenario: Send correct error DTO to DLQ
    Given a training event with ID 42 and error message "Something went wrong"
    When sending to DLQ
    Then an error message should be published to DLQ

  Scenario: Handle null training event
    Given a null training event and error message "Something went wrong"
    When sending to DLQ
    Then an error message should be published to DLQ

