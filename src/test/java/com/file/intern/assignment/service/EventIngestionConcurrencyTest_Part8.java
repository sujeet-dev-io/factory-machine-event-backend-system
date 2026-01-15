package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.repository.MachineEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
class EventIngestionConcurrencyTest_Part8 {


    @Autowired
    private EventIngestionService ingestionService;


    @Autowired
    private MachineEventRepository repository;

  //  Test 8: Thread-safety – concurrent ingestion

    @Test
    void concurrentIngestion_shouldNotCreateDuplicateOrCorruptData() throws Exception {
        int threads = 10;
        int eventsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        Instant baseTime = Instant.now().minusSeconds(300);


        for (int t = 0; t < threads; t++) {
            final int threadIndex = t;
            executor.submit(() -> {
                try {
                    List<EventIngestRequest> batch = new ArrayList<>();
                    for (int i = 0; i < eventsPerThread; i++) {
                        batch.add(EventIngestRequest.builder()
                                .eventId("E-8-" + i) // SAME IDs across threads
                                .eventTime(baseTime.plusSeconds(i))
                                .machineId("M-CONC")
                                .lineId("L-CONC")
                                .factoryId("F-CONC")
                                .durationMs(1000L + threadIndex)
                                .defectCount(1)
                                .build());
                    }
                    ingestionService.ingestBatch(batch);
                } finally {
                    latch.countDown();
                }
            });
        }


        latch.await();
        executor.shutdown();


// Expect only one record per eventId
        long storedCount = repository.countByMachineId("M-CONC");


        assertThat(storedCount).isEqualTo(eventsPerThread);


// Verify no corruption (all events have valid duration)
        repository.findAllByMachineId("M-CONC").forEach(event ->
                assertThat(event.getDurationMs()).isGreaterThan(0)
        );
    }
}