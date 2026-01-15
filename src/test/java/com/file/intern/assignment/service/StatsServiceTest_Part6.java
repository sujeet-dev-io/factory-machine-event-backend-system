package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.dto.MachineStatsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class StatsServiceTest_Part6 {


    @Autowired
    private EventIngestionService ingestionService;


    @Autowired
    private StatsService statsService;

   // Test 6: defectCount = -1 ignored in defect totals

    @Test
    void defectCountMinusOne_shouldBeIgnoredInStats() {
        Instant baseTime = Instant.now().minusSeconds(3600);


        EventIngestRequest validDefectEvent = EventIngestRequest.builder()
                .eventId("E-6-1")
                .eventTime(baseTime.plusSeconds(10))
                .machineId("M-100")
                .lineId("L-10")
                .factoryId("F-10")
                .durationMs(1000L)
                .defectCount(3)
                .build();


        EventIngestRequest  unknownDefectEvent = EventIngestRequest.builder()
                .eventId("E-6-2")
                .eventTime(baseTime.plusSeconds(20))
                .machineId("M-100")
                .lineId("L-10")
                .factoryId("F-10")
                .durationMs(1200L)
                .defectCount(-1) // should be ignored
                .build();


        ingestionService.ingestBatch(List.of(validDefectEvent, unknownDefectEvent));


        MachineStatsResponse stats = statsService.getMachineStats(
                "M-100",
                baseTime,
                baseTime.plusSeconds(3600)
        );


        assertThat(stats.getEventsCount()).isEqualTo(2);
        assertThat(stats.getDefectsCount()).isEqualTo(3); // -1 ignored
        assertThat(stats.getAvgDefectRate()).isGreaterThan(0);
    }
}