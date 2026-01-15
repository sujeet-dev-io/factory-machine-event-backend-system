package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.BatchIngestResponse;
import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.repository.MachineEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
class EventIngestionServiceTest_Part5 {


    @Autowired
    private EventIngestionService ingestionService;


    @Autowired
    private MachineEventRepository repository;

   // Test 5: Future eventTime rejected (> 15 minutes)

    @Test
    void futureEventTimeBeyondThreshold_shouldBeRejected() {
        Instant futureEventTime = Instant.now().plus(Duration.ofMinutes(20));


        EventIngestRequest futureEvent = EventIngestRequest.builder()
                .eventId("E-5")
                .eventTime(futureEventTime) // invalid future time
                .machineId("M-004")
                .lineId("L-4")
                .factoryId("F-2")
                .durationMs(1200L)
                .defectCount(0)
                .build();


        BatchIngestResponse response =
                ingestionService.ingestBatch(List.of(futureEvent));


        assertThat(response.getAccepted()).isEqualTo(0);
        assertThat(response.getRejected()).isEqualTo(1);
        assertThat(response.getRejections()).hasSize(1);
        assertThat(response.getRejections().get(0).getReason())
                .isEqualTo("FUTURE_EVENT_TIME");


        // Ensure event was not stored
        assertThat(repository.findByEventId("E-5")).isNotPresent();
    }
}