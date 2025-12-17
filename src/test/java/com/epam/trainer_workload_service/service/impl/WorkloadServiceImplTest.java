package com.epam.trainer_workload_service.service.impl;

import com.epam.trainer_workload_service.dto.ActionType;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.dto.TrainingSummaryDto;
import com.epam.trainer_workload_service.entity.TrainerMonthlyWorkload;
import com.epam.trainer_workload_service.entity.TrainingEventRecord;
import com.epam.trainer_workload_service.repository.TrainerMonthlyWorkloadRepository;
import com.epam.trainer_workload_service.repository.TrainingEventRecordRepository;
import com.epam.trainer_workload_service.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {
    private static final String NULL_ARGUMENT = "Check the argument. It might be null.";
    private static final String NULL_ACTION_TYPE = "actionType must not be null";
    private static final String NULL_USERNAME = "username must not be null";
    private static final String NEGATIVE_DURATION_MINUTES = "durationMinutes must be non-negative";
    private static final String TRAINING_TO_DELETE_NOT_FOUND = "Training event to delete not found";

    @Mock
    private TrainerMonthlyWorkloadRepository workloadRepository;

    @Mock
    private TrainingEventRecordRepository eventRepository;

    @InjectMocks
    private WorkloadServiceImpl service;

    @Test
    void shouldProcessTrainerEventWhenActionTypeIsAddEvent() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 1, 15));
        dto.setDurationMinutes(60);
        dto.setActionType(ActionType.ADD);

        when(eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(
                anyString(), any(LocalDate.class), anyLong()))
                .thenReturn(Optional.empty());

        when(workloadRepository.findByUsernameAndYearAndMonth(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        // When
        service.processTrainerEvent(dto);

        // Then
        verify(eventRepository).save(any(TrainingEventRecord.class));
        verify(workloadRepository).save(any(TrainerMonthlyWorkload.class));
        assertDoesNotThrow(() -> service.processTrainerEvent(dto));
    }

    @Test
    void shouldDoNothingWhenProcessTrainerEventIsDuplicateAdd() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(90);
        dto.setActionType(ActionType.ADD);

        TrainingEventRecord eventRecord = new TrainingEventRecord();
        eventRecord.setId(1L);
        eventRecord.setUsername("John.Doe");
        eventRecord.setFirstName("John");
        eventRecord.setLastName("Doe");
        eventRecord.setActive(true);
        eventRecord.setTrainingDate(LocalDate.of(2025, 5, 10));
        eventRecord.setDurationMinutes(90);
        eventRecord.setCreatedAt(LocalDateTime.now());

        when(eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(
                anyString(), any(LocalDate.class), anyLong()))
                .thenReturn(Optional.of(eventRecord));

        // When
        service.processTrainerEvent(dto);

        // Then
        verify(eventRepository, never()).save(any());
        verify(workloadRepository, never()).save(any());
        assertDoesNotThrow(() -> service.processTrainerEvent(dto));
    }

    @Test
    void shouldRemoveRecordAndDecreaseWorkloadWhenProcessTrainerEventIsDeleteEvent() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(60);
        dto.setActionType(ActionType.DELETE);

        TrainingEventRecord eventRecord = new TrainingEventRecord();
        eventRecord.setId(1L);
        eventRecord.setUsername("John.Doe");
        eventRecord.setFirstName("John");
        eventRecord.setLastName("Doe");
        eventRecord.setActive(true);
        eventRecord.setTrainingDate(LocalDate.of(2025, 5, 10));
        eventRecord.setDurationMinutes(60);
        eventRecord.setCreatedAt(LocalDateTime.now());

        TrainerMonthlyWorkload workload = new TrainerMonthlyWorkload();
        workload.setId(1L);
        workload.setUsername("John.Doe");
        workload.setFirstName("John");
        workload.setLastName("Doe");
        workload.setActive(true);
        workload.setYear(2025);
        workload.setMonth(5);
        workload.setTotalMinutes(eventRecord.getDurationMinutes());

        when(eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(
                anyString(), any(LocalDate.class), anyLong()))
                .thenReturn(Optional.of(eventRecord));

        when(workloadRepository.findByUsernameAndYearAndMonth(anyString(), anyInt(), anyInt()))
                .thenReturn(Optional.of(workload));

        // When
        service.processTrainerEvent(dto);

        // Then
        verify(eventRepository).delete(any(TrainingEventRecord.class));
        verify(workloadRepository).save(any(TrainerMonthlyWorkload.class));
        assertDoesNotThrow(() -> service.processTrainerEvent(dto));
    }

    @Test
    void shouldExecuteGetSummaryForTrainerWhenValidValues() throws ServiceException {
        // Given
        TrainingEventRecord eventRecord = new TrainingEventRecord();
        eventRecord.setId(1L);
        eventRecord.setUsername("John.Doe");
        eventRecord.setFirstName("John");
        eventRecord.setLastName("Doe");
        eventRecord.setActive(true);
        eventRecord.setTrainingDate(LocalDate.of(2025, 5, 10));
        eventRecord.setDurationMinutes(60);
        eventRecord.setCreatedAt(LocalDateTime.now());

        TrainerMonthlyWorkload workload = new TrainerMonthlyWorkload();
        workload.setId(1L);
        workload.setUsername("John.Doe");
        workload.setFirstName("John");
        workload.setLastName("Doe");
        workload.setActive(true);
        workload.setYear(2025);
        workload.setMonth(5);
        workload.setTotalMinutes(eventRecord.getDurationMinutes());

        when(workloadRepository.findByUsername("John.Doe")).thenReturn(List.of(workload));

        // When
        TrainingSummaryDto summary = service.getSummaryForTrainer("John.Doe", 2025, 5);

        // Then
        verify(workloadRepository).findByUsername("John.Doe");
        assertNotNull(summary);
        assertEquals("John.Doe", summary.getUsername());
        assertEquals("John", summary.getFirstName());
        assertEquals("Doe", summary.getLastName());
        assertTrue(summary.isActive());
        assertFalse(summary.getYears().isEmpty());
        assertEquals(2025, summary.getYears().get(0).getYear());
        assertEquals(5, summary.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(60, summary.getYears().get(0).getMonths().get(0).getTotalMinutes());
    }

    @Test
    void shouldUpdateTrainerInfoFromFirstRecordWhenGetSummaryForTrainerCalled() throws ServiceException {
        // Given
        List<TrainerMonthlyWorkload> workloads = Arrays.asList(
                new TrainerMonthlyWorkload("John.Doe", "John", "Doe", true, 2024, 1, 100L),
                new TrainerMonthlyWorkload("John.Doe", "John", "Doe", false, 2024, 2, 150L)
        );

        when(workloadRepository.findByUsername("John.Doe"))
                .thenReturn(workloads);

        // When
        TrainingSummaryDto result = service.getSummaryForTrainer("John.Doe", 2024, 1);

        // Then
        verify(workloadRepository).findByUsername("John.Doe");
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.isActive());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenProcessTrainerEventWithNullDto() {
        // Given

        // When
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> service.processTrainerEvent(null));

        // Then
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(workloadRepository);
        assertEquals(NULL_ARGUMENT, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenProcessTrainerEventWithNullUsername_() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername(null);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(60);
        dto.setActionType(ActionType.ADD);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.processTrainerEvent(dto));

        // Then
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(workloadRepository);
        assertEquals(NULL_USERNAME, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenProcessTrainerEventWithNullActionType_() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(60);
        dto.setActionType(null);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.processTrainerEvent(dto));

        // Then
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(workloadRepository);
        assertEquals(NULL_ACTION_TYPE, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenProcessTrainerEventWithNegativeDuration() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 1, 15));
        dto.setDurationMinutes(-10);
        dto.setActionType(ActionType.ADD);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.processTrainerEvent(dto));

        // Then
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(workloadRepository);
        assertEquals(NEGATIVE_DURATION_MINUTES, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotFoundOnDeleteEvent() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 1, 15));
        dto.setDurationMinutes(60);
        dto.setActionType(ActionType.DELETE);

        when(eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(
                anyString(), any(LocalDate.class), anyLong()))
                .thenReturn(Optional.empty());

        // When
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.processTrainerEvent(dto));

        // Then
        verify(eventRepository, times(1))
                .findFirstByUsernameAndTrainingDateAndDurationMinutes(
                        anyString(), any(LocalDate.class), anyLong());
        verify(eventRepository, never()).delete(any());
        verify(workloadRepository, never()).save(any());
        assertTrue(ex.getMessage().contains(TRAINING_TO_DELETE_NOT_FOUND));
    }

    @Test
    void shouldThrowExceptionWhenInvalidDto() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.processTrainerEvent(dto));

        // Then
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(workloadRepository);
        assertEquals(NULL_USERNAME, ex.getMessage());
    }

    @Test
    void shouldNotAllowNegativeTotalMinutesWhenSubtractOnDeleteAction() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2024, 1, 15));
        dto.setDurationMinutes(90L);
        dto.setActionType(ActionType.DELETE);

        TrainingEventRecord existingEvent = new TrainingEventRecord(
                "John.Doe", "John", "Doe", true,
                LocalDate.of(2024, 1, 15),
                90L,
                LocalDateTime.now()
        );

        lenient().when(eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(
                        eq("John.Doe"), eq(LocalDate.of(2024, 1, 15)), eq(90L)))
                .thenReturn(Optional.of(existingEvent));

        TrainerMonthlyWorkload existingWorkload = new TrainerMonthlyWorkload(
                "John.Doe", "John", "Doe", true, 2024, 1, 50L
        );

        when(workloadRepository.findByUsernameAndYearAndMonth(
                eq("John.Doe"), eq(2024), eq(1)))
                .thenReturn(Optional.of(existingWorkload));

        // When
        service.processTrainerEvent(dto);

        // Then
        ArgumentCaptor<TrainerMonthlyWorkload> captor =
                ArgumentCaptor.forClass(TrainerMonthlyWorkload.class);
        verify(eventRepository).delete(existingEvent);
        verify(workloadRepository).save(captor.capture());
        assertEquals(0L, captor.getValue().getTotalMinutes());
    }


    @Test
    void getSummaryForTrainer_noRecords_returnsEmpty() throws ServiceException {
        // Given
        when(workloadRepository.findByUsername("john")).thenReturn(Collections.emptyList());

        // When
        TrainingSummaryDto summary = service.getSummaryForTrainer("john", 2025, 1);

        // Then
        verify(workloadRepository).findByUsername("john");
        verifyNoMoreInteractions(workloadRepository);
        assertNotNull(summary);
        assertEquals("john", summary.getUsername());
        assertFalse(summary.isActive());
        assertTrue(summary.getYears().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenNullUsernameOnGetSummaryForTrainer() {
        // Given

        // When
        Exception ex = assertThrows(ServiceException.class, () -> service.getSummaryForTrainer(null, 2025, 1));

        // Then
        verifyNoInteractions(workloadRepository);
        assertTrue(ex.getMessage().contains("Username should not be null"));
    }
}