package com.epam.trainer_workload_service.service.impl;

import com.epam.trainer_workload_service.dto.ActionType;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.entity.TrainingEventRecord;
import com.epam.trainer_workload_service.mapper.WorkloadMapper;
import com.epam.trainer_workload_service.model.MonthSummary;
import com.epam.trainer_workload_service.model.TrainingSummary;
import com.epam.trainer_workload_service.model.YearSummary;
import com.epam.trainer_workload_service.mongo.MonthWorkload;
import com.epam.trainer_workload_service.mongo.TrainerWorkloadDocument;
import com.epam.trainer_workload_service.mongo.YearWorkload;
import com.epam.trainer_workload_service.repository.TrainerWorkloadRepository;
import com.epam.trainer_workload_service.repository.TrainingEventRecordRepository;
import com.epam.trainer_workload_service.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {
    private static final String NULL_DTO = "TrainingEventDto must not be null";
    private static final String NULL_ACTION_TYPE = "Action type must not be null";
    private static final String NULL_USERNAME = "Username must not be null";
    private static final String NEGATIVE_DURATION_MINUTES = "DurationMinutes must be non-negative";
    private static final String TRAINING_TO_DELETE_NOT_FOUND = "Training not found: ";
    private static final String USERNAME = "John.Doe";
    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "Doe";

    @Mock
    private TrainerWorkloadRepository workloadRepository;

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
        dto.setUsername(USERNAME);
        dto.setFirstName(FIRSTNAME);
        dto.setLastName(LASTNAME);
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(90);
        dto.setActionType(ActionType.ADD);

        TrainingEventRecord eventRecord = new TrainingEventRecord(1L, USERNAME, LocalDate.of(2025, 5, 10), 90);

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
        dto.setUsername(USERNAME);
        dto.setFirstName(FIRSTNAME);
        dto.setLastName(LASTNAME);
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(60);
        dto.setActionType(ActionType.DELETE);

        TrainingEventRecord eventRecord = new TrainingEventRecord(1L, USERNAME, LocalDate.of(2025, 5, 10), 60);

        MonthWorkload monthSummary = new MonthWorkload();
        monthSummary.setMonth(5);
        monthSummary.setTotalMinutes(60L);
        List<MonthWorkload> months = new ArrayList<>();
        months.add(monthSummary);
        YearWorkload yearSummary = new YearWorkload();
        yearSummary.setYear(2025);
        yearSummary.setMonths(months);
        List<YearWorkload> years = new ArrayList<>();
        years.add(yearSummary);

        TrainerWorkloadDocument workloadDocument = new TrainerWorkloadDocument();
        workloadDocument.setUsername(USERNAME);
        workloadDocument.setFirstName(FIRSTNAME);
        workloadDocument.setLastName(LASTNAME);
        workloadDocument.setActive(true);
        workloadDocument.setYears(years);

        when(eventRepository.findByTrainingId(dto.getTrainingId())).thenReturn(Optional.of(eventRecord));

        when(workloadRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(workloadDocument));

        // When
        service.processTrainerEvent(dto);

        // Then
        verify(eventRepository).delete(any(TrainingEventRecord.class));
        verify(workloadRepository).save(any(TrainerWorkloadDocument.class));
    }

    @Test
    void shouldExecuteGetSummaryForTrainerWhenValidValues() throws ServiceException {
        // Given
        MonthWorkload month = new MonthWorkload();
        month.setMonth(5);
        month.setTotalMinutes(60L);
        List<MonthWorkload> months = new ArrayList<>();
        months.add(month);

        YearWorkload year = new YearWorkload();
        year.setYear(2025);
        year.setMonths(months);
        List<YearWorkload> years = new ArrayList<>();
        years.add(year);

        TrainerWorkloadDocument workloadDocument = new TrainerWorkloadDocument();
        workloadDocument.setUsername(USERNAME);
        workloadDocument.setFirstName(FIRSTNAME);
        workloadDocument.setLastName(LASTNAME);
        workloadDocument.setActive(true);
        workloadDocument.setYears(years);

        MonthSummary monthSummary = new MonthSummary()
                .month(5)
                .totalMinutes(60L);

        YearSummary yearSummary = new YearSummary()
                .year(2025)
                .months(List.of(monthSummary));

        TrainingSummary summary = new TrainingSummary();
        summary.setUsername(USERNAME);
        summary.setFirstName(FIRSTNAME);
        summary.setLastName(LASTNAME);
        summary.setActive(true);
        summary.setYears(List.of(yearSummary));

        when(workloadRepository.findByUsername(USERNAME)).thenReturn(Optional.of(workloadDocument));

        when(workloadMapper.toTrainingSummary(workloadDocument, 2025, 5)).thenReturn(summary);

        // When
        TrainingSummary result = service.getSummaryForTrainer(USERNAME, 2025, 5);

        // Then
        verify(workloadRepository).findByUsername(USERNAME);
        verify(workloadMapper).toTrainingSummary(workloadDocument, 2025, 5);
        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(FIRSTNAME, result.getFirstName());
        assertEquals(LASTNAME, result.getLastName());
        assertTrue(result.getActive());
        assertFalse(result.getYears().isEmpty());
        assertEquals(2025, result.getYears().get(0).getYear());
        assertEquals(5, result.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(60, result.getYears().get(0).getMonths().get(0).getTotalMinutes());
    }

    @Test
    void shouldUpdateTrainerInfoFromFirstRecordWhenGetSummaryForTrainerCalled() throws ServiceException {
        // Given
        MonthWorkload month = new MonthWorkload();
        month.setMonth(5);
        month.setTotalMinutes(60L);
        List<MonthWorkload> months = new ArrayList<>();
        months.add(month);

        YearWorkload year = new YearWorkload();
        year.setYear(2025);
        year.setMonths(months);
        List<YearWorkload> years = new ArrayList<>();
        years.add(year);

        TrainerWorkloadDocument workloadDocument = new TrainerWorkloadDocument();
        workloadDocument.setUsername(USERNAME);
        workloadDocument.setFirstName(FIRSTNAME);
        workloadDocument.setLastName(LASTNAME);
        workloadDocument.setActive(true);
        workloadDocument.setYears(years);

        MonthSummary monthSummary = new MonthSummary()
                .month(5)
                .totalMinutes(60L);

        YearSummary yearSummary = new YearSummary()
                .year(2025)
                .months(List.of(monthSummary));

        TrainingSummary summary = new TrainingSummary();
        summary.setUsername(USERNAME);
        summary.setFirstName(FIRSTNAME);
        summary.setLastName(LASTNAME);
        summary.setActive(true);
        summary.setYears(List.of(yearSummary));

        when(workloadMapper.toTrainingSummary(workloadDocument, 2025, 5))
                .thenReturn(summary);

        when(workloadRepository.findByUsername(USERNAME)).thenReturn(Optional.of(workloadDocument));

        // When
        TrainingSummary result = service.getSummaryForTrainer(USERNAME, 2025, 5);

        // Then
        verify(workloadRepository).findByUsername(USERNAME);
        assertEquals(FIRSTNAME, result.getFirstName());
        assertEquals(LASTNAME, result.getLastName());
        assertTrue(result.getActive());
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
        dto.setUsername(USERNAME);
        dto.setFirstName(FIRSTNAME);
        dto.setLastName(LASTNAME);
        dto.setActive(true);
        dto.setTrainingDate(LocalDate.of(2024, 1, 15));
        dto.setDurationMinutes(90L);
        dto.setActionType(ActionType.DELETE);

        TrainingEventRecord existingEvent = new TrainingEventRecord(1L,
                USERNAME,
                LocalDate.of(2024, 1, 15),
                90L);

        lenient().when(eventRepository.findByTrainingId(dto.getTrainingId()))
                .thenReturn(Optional.of(existingEvent));

        MonthWorkload month = new MonthWorkload();
        month.setMonth(1);
        month.setTotalMinutes(90L);
        List<MonthWorkload> months = new ArrayList<>();
        months.add(month);

        YearWorkload year = new YearWorkload();
        year.setYear(2024);
        year.setMonths(months);
        List<YearWorkload> years = new ArrayList<>();
        years.add(year);

        TrainerWorkloadDocument workloadDocument = new TrainerWorkloadDocument();
        workloadDocument.setUsername(USERNAME);
        workloadDocument.setFirstName(FIRSTNAME);
        workloadDocument.setLastName(LASTNAME);
        workloadDocument.setActive(true);
        workloadDocument.setYears(years);

        when(workloadRepository.findByUsername(USERNAME)).thenReturn(Optional.of(workloadDocument));

        // When
        service.processTrainerEvent(dto);

        // Then
        ArgumentCaptor<TrainerWorkloadDocument> captor =
                ArgumentCaptor.forClass(TrainerWorkloadDocument.class);

        verify(workloadRepository).save(captor.capture());

        TrainerWorkloadDocument saved = captor.getValue();
        YearWorkload year2 = saved.getYears().get(0);
        MonthWorkload month2 = year2.getMonths().get(0);

        verify(eventRepository).delete(existingEvent);
        assertEquals(0L, month2.getTotalMinutes());

    }


    @Test
    void getSummaryForTrainer_noRecords_returnsEmpty() throws ServiceException {
        // Given
        MonthWorkload month = new MonthWorkload();
        month.setMonth(1);
        month.setTotalMinutes(60L);
        List<MonthWorkload> months = new ArrayList<>();
        months.add(month);

        YearWorkload year = new YearWorkload();
        year.setYear(2025);
        year.setMonths(months);
        List<YearWorkload> years = new ArrayList<>();
        years.add(year);

        TrainerWorkloadDocument workloadDocument = new TrainerWorkloadDocument();
        workloadDocument.setUsername(USERNAME);
        workloadDocument.setFirstName(FIRSTNAME);
        workloadDocument.setLastName(LASTNAME);
        workloadDocument.setActive(true);
        workloadDocument.setYears(years);

        MonthSummary monthSummary = new MonthSummary()
                .month(1)
                .totalMinutes(60L);

        YearSummary yearSummary = new YearSummary()
                .year(2025)
                .months(List.of(monthSummary));

        TrainingSummary summary = new TrainingSummary();
        summary.setUsername(USERNAME);
        summary.setFirstName(FIRSTNAME);
        summary.setLastName(LASTNAME);
        summary.setActive(true);
        summary.setYears(List.of(yearSummary));

        when(workloadRepository.findByUsername(USERNAME)).thenReturn(Optional.of(workloadDocument));

        when(workloadMapper.toTrainingSummary(workloadDocument, 2025, 1)).thenReturn(summary);

        // When
        TrainingSummary result = service.getSummaryForTrainer(USERNAME, 2025, 1);

        // Then
        verify(workloadRepository).findByUsername(USERNAME);
        verifyNoMoreInteractions(workloadRepository);
        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertTrue(result.getActive());
        assertFalse(result.getYears().isEmpty());
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