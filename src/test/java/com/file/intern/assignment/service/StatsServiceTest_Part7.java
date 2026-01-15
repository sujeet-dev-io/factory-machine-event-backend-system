package com.file.intern.assignment.service;


import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.dto.MachineStatsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@Transactional
class StatsServiceTest_Part7 {

    @Autowired
    private EventIngestionService ingestionService;

    @Autowired
    private StatsService statsService;

   // Test 7: start inclusive, end exclusive boundary check
    @Test
    void statsWindow_shouldBeStartInclusive_endExclusive() {
        Instant baseTime = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant start = baseTime;
        Instant end = baseTime.plusSeconds(3600); // 1 hour window


        EventIngestRequest   atStart = EventIngestRequest.builder()
                .eventId("E-7-1")
                .eventTime(start) // INCLUDED
                .machineId("M-200")
                .lineId("L-20")
                .factoryId("F-20")
                .durationMs(1000L)
                .defectCount(1)
                .build();


        EventIngestRequest  beforeEnd = EventIngestRequest.builder()
                .eventId("E-7-2")
                .eventTime(end.minusSeconds(1)) // INCLUDED
                .machineId("M-200")
                .lineId("L-20")
                .factoryId("F-20")
                .durationMs(1000L)
                .defectCount(2)
                .build();


        EventIngestRequest  atEnd = EventIngestRequest.builder()
                .eventId("E-7-3")
                .eventTime(end) // EXCLUDED
                .machineId("M-200")
                .lineId("L-20")
                .factoryId("F-20")
                .durationMs(1000L)
                .defectCount(5)
                .build();

        ingestionService.ingestBatch(List.of(atStart, beforeEnd, atEnd));
        MachineStatsResponse stats = statsService.getMachineStats(
                "M-200",
                start,
                end
        );

        assertThat(stats.getEventsCount()).isEqualTo(2);
        assertThat(stats.getDefectsCount()).isEqualTo(3); // 1 + 2
    }
}


