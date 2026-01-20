Feature: Trainer Workload Service

  Scenario: Add new training for existing trainer
    Given a trainer with username "John.Doe" and first name "John" and last name "Doe"
    When a training event with ID 1 and duration 60 minutes is added
    Then the monthly summary for May 2025 should show 60 minutes

  Scenario: Generate transaction ID if missing
    Given a training event with ID 4 and missing transaction ID
    When it is processed
    Then a new transaction ID should be generated

  Scenario: Prevent duplicate training ADD
    Given a training event with ID 5 already exists
    When the same training event is processed again
    Then the system should not increase workload

  Scenario: Fail when username is null
    Given a training event with null username
    When processing the event
    Then the system should throw an exception "Username must not be null"

  Scenario: Fail when action type is null
    Given a training event with null action type
    When processing the event
    Then the system should throw an exception "Action type must not be null"

  Scenario: Fail when duration is negative
    Given a training event with negative duration -10
    When processing the event
    Then the system should throw an exception "DurationMinutes must be non-negative"

  Scenario: Fail delete when training not found
    Given a training event with ID 6 does not exist
    When the training event is deleted
    Then the system should throw an exception "Training not found"
