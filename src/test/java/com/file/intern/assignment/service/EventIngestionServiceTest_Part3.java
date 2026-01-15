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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@Transactional
class EventIngestionServiceTest_Part3 {


    @Autowired
    private EventIngestionService ingestionService;


    @Autowired
    private MachineEventRepository repository;

    // Test 3: Different payload + older receivedTime → ignored
    @Test
    void differentPayloadWithOlderReceivedTime_shouldBeIgnored() throws InterruptedException {
        Instant eventTime = Instant.now().minusSeconds(300);


        EventIngestRequest  original = EventIngestRequest.builder()
                .eventId("E-3")
                .eventTime(eventTime)
                .machineId("M-002")
                .lineId("L-2")
                .factoryId("F-1")
                .durationMs(1500L)
                .defectCount(1)
                .build();


        // First ingestion
        BatchIngestResponse firstResponse =
                ingestionService.ingestBatch(List.of(original));


        assertThat(firstResponse.getAccepted(), equalTo(1));

        // Capture stored receivedTime
        MachineEvent stored = repository.findByEventId("E-3").orElseThrow();
        Instant storedReceivedTime = stored.getReceivedTime();
        MachineEvent  after = null;

        try {
            // Simulate "older" update by using same eventTime (eventTime will not be newer)
            EventIngestRequest olderPayload = EventIngestRequest.builder()
                    .eventId("E-3")
                    .eventTime(eventTime)
                    .machineId("M-002")
                    .lineId("L-2")
                    .factoryId("F-1")
                    .durationMs(3000L) // changed payload
                    .defectCount(5)
                    .build();


            BatchIngestResponse  secondResponse =
                    ingestionService.ingestBatch(List.of(olderPayload));


            assertThat(secondResponse.getUpdated(), equalTo(0));
            assertThat(secondResponse.getDeduped(), equalTo(1));


            after = repository.findByEventId("E-3").orElseThrow();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // Ensure data was NOT overwritten
        assertThat(after.getDurationMs(), equalTo(1500L));
        assertThat(after.getDefectCount(), equalTo(1));
        assertThat(after.getReceivedTime(), equalTo(storedReceivedTime));
    }
}