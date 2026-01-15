package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.BatchIngestResponse;
import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.repository.MachineEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@Transactional
class EventIngestionServiceTest_Part4 {


    @Autowired
    private EventIngestionService ingestionService;

    @Autowired
    private MachineEventRepository repository;


 //    Test 4: Invalid duration rejected

    @Test
    void invalidDuration_shouldBeRejected() {
        Instant eventTime = Instant.now().minusSeconds(60);


        EventIngestRequest invalidEvent = EventIngestRequest.builder()
                .eventId("E-4")
                .eventTime(eventTime)
                .machineId("M-003")
                .lineId("L-3")
                .factoryId("F-2")
                .durationMs(-100L) // INVALID duration
                .defectCount(0)
                .build();


        BatchIngestResponse response =
                ingestionService.ingestBatch(List.of(invalidEvent));


        assertThat(response.getAccepted()).isEqualTo(0);
        assertThat(response.getRejected()).isEqualTo(1);
        assertThat(response.getRejections()).hasSize(1);
        assertThat(response.getRejections().get(0).getReason())
                .isEqualTo("INVALID_DURATION");


        // Ensure nothing persisted
        assertThat(repository.findByEventId("E-4")).isNotPresent();
    }
}