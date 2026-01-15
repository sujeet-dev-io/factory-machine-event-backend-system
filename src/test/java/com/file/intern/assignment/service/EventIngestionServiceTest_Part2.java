package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.BatchIngestResponse;
import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.entity.MachineEvent;
import com.file.intern.assignment.repository.MachineEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class EventIngestionServiceTest_Part2 {


    @Autowired
    private EventIngestionService ingestionService;


    @Autowired
    private MachineEventRepository repository;


    //Test 2: Different payload + newer receivedTime → update

    @Test
    void differentPayloadWithNewerReceivedTime_shouldUpdate() throws InterruptedException {
        Instant eventTime = Instant.now().minusSeconds(120);


        EventIngestRequest original = EventIngestRequest.builder()
                .eventId("E-2")
                .eventTime(eventTime)
                .machineId("M-001")
                .lineId("L-1")
                .factoryId("F-1")
                .durationMs(1000L)
                .defectCount(0)
                .build();


        // First ingestion
        BatchIngestResponse firstResponse =
                ingestionService.ingestBatch(List.of(original));


        assertThat(firstResponse.getAccepted()).isEqualTo(1);

       // Ensure receivedTime will be newer
        Thread.sleep(10);


        EventIngestRequest  updatedPayload = EventIngestRequest.builder()
                .eventId("E-2") // same eventId
                .eventTime(eventTime)
                .machineId("M-001")
                .lineId("L-1")
                .factoryId("F-1")
                .durationMs(2000L) // payload changed
                .defectCount(2)
                .build();


        BatchIngestResponse  secondResponse =
                ingestionService.ingestBatch(List.of(updatedPayload));


        assertThat(secondResponse.getUpdated()).isEqualTo(1);
        assertThat(secondResponse.getDeduped()).isEqualTo(0);


        MachineEvent entity = repository.findByEventId("E-2").orElseThrow();
        assertThat(entity.getDurationMs()).isEqualTo(2000L);
        assertThat(entity.getDefectCount()).isEqualTo(2);
    }
}