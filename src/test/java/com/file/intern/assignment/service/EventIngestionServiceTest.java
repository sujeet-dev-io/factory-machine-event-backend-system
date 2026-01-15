package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.BatchIngestResponse;
import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.repository.MachineEventRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class EventIngestionServiceTest {


    @Autowired
    private EventIngestionService ingestionService;


    @Autowired
    private MachineEventRepository repository;

  //  Test 1: Identical duplicate eventId → deduped

    @Test
    void identicalDuplicateEvent_shouldBeDeduped() {
        Instant now = Instant.now();


        EventIngestRequest  event1 = EventIngestRequest.builder()
                .eventId("E-1")
                .eventTime(now.minusSeconds(60))
                .machineId("M-001")
                .lineId("L-1")
                .factoryId("F-1")
                .durationMs(1000L)
                .defectCount(0)
                .build();


        EventIngestRequest event2 = EventIngestRequest.builder()
                .eventId("E-1") // same eventId
                .eventTime(event1.getEventTime())
                .machineId(event1.getMachineId())
                .lineId(event1.getLineId())
                .factoryId(event1.getFactoryId())
                .durationMs(event1.getDurationMs())
                .defectCount(event1.getDefectCount())
                .build();


        BatchIngestResponse response = ingestionService.ingestBatch(List.of(event1, event2));


        assertThat(response.getAccepted()).isEqualTo(1);
        assertThat(response.getDeduped()).isEqualTo(1);
        assertThat(response.getUpdated()).isEqualTo(0);
        assertThat(response.getRejected()).isEqualTo(0);


        assertThat(repository.findByEventId("E-1")).isPresent();
    }
}