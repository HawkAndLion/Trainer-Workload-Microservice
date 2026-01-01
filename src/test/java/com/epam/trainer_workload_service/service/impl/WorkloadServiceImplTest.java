package com.epam.trainer_workload_service.service.impl;

import com.epam.trainer_workload_service.dto.*;
import com.epam.trainer_workload_service.entity.TrainerMonthlyWorkload;
import com.epam.trainer_workload_service.entity.TrainingEventRecord;
import com.epam.trainer_workload_service.mapper.WorkloadMapper;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {
    private static final String NULL_DTO = "TrainingEventDto must not be null";
    private static final String NULL_ACTION_TYPE = "Action type must not be null";
    private static final String NULL_USERNAME = "Username must not be null";
    private static final String NEGATIVE_DURATION_MINUTES = "DurationMinutes must be non-negative";
    private static final String TRAINING_TO_DELETE_NOT_FOUND = "Training not found: ";

    @Mock
    private TrainerMonthlyWorkloadRepository workloadRepository;

    @Mock
    private TrainingEventRecordRepository eventRepository;

    @Mock
    private WorkloadMapper workloadMapper;

    @InjectMocks
    private WorkloadServiceImpl service;


    @Test
    void shouldDoNothingWhenProcessTrainerEventIsDuplicateAdd() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setTrainingId(1L);
        dto.setUsername("John.Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(90);
        dto.setActionType(ActionType.ADD);

        TrainingEventRecord eventRecord = new TrainingEventRecord(1L, "John.Doe", LocalDate.of(2025, 5, 10), 90);

        when(eventRepository.existsByTrainingId(dto.getTrainingId())).thenReturn(true);

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

        TrainingEventRecord eventRecord = new TrainingEventRecord(1L, "John.Doe", LocalDate.of(2025, 5, 10), 60);

        TrainerMonthlyWorkload workload = new TrainerMonthlyWorkload();
        workload.setId(1L);
        workload.setUsername("John.Doe");
        workload.setFirstName("John");
        workload.setLastName("Doe");
        workload.setActive(true);
        workload.setYear(2025);
        workload.setMonth(5);
        workload.setTotalMinutes(eventRecord.getDurationMinutes());

        when(eventRepository.findByTrainingId(dto.getTrainingId())).thenReturn(Optional.of(eventRecord));

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
        TrainingEventRecord eventRecord = new TrainingEventRecord(1L, "John.Doe", LocalDate.of(2025, 5, 10), 60);

        TrainerMonthlyWorkload workload = new TrainerMonthlyWorkload();
        workload.setId(1L);
        workload.setUsername("John.Doe");
        workload.setFirstName("John");
        workload.setLastName("Doe");
        workload.setActive(true);
        workload.setYear(2025);
        workload.setMonth(5);
        workload.setTotalMinutes(eventRecord.getDurationMinutes());

        List<TrainerMonthlyWorkload> records = new ArrayList<>();
        records.add(workload);

        MonthSummaryDto monthSummaryDto = new MonthSummaryDto(5, 60);
        List<MonthSummaryDto> months = new ArrayList<>();
        months.add(monthSummaryDto);
        YearSummaryDto yearSummaryDto = new YearSummaryDto(2025, months);
        List<YearSummaryDto> years = new ArrayList<>();
        years.add(yearSummaryDto);

        when(workloadMapper.toTrainingSummary("John.Doe", records))
                .thenReturn(new TrainingSummaryDto("John.Doe", "John", "Doe", true, years));

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
        when(workloadMapper.toTrainingSummary("John.Doe", workloads))
                .thenReturn(new TrainingSummaryDto("John.Doe", "John", "Doe", true, List.of()));

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
        assertEquals(NULL_DTO, exception.getMessage());
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

        when(eventRepository.findByTrainingId(dto.getTrainingId())).thenReturn(Optional.empty());

        // When
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.processTrainerEvent(dto));

        // Then
        verify(eventRepository, times(1))
                .findByTrainingId(dto.getTrainingId());
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

        TrainingEventRecord existingEvent = new TrainingEventRecord(1L,
                "John.Doe",
                LocalDate.of(2024, 1, 15),
                90L);

        lenient().when(eventRepository.findByTrainingId(dto.getTrainingId()))
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
        assertTrue(ex.getMessage().contains(NULL_USERNAME));
    }
}