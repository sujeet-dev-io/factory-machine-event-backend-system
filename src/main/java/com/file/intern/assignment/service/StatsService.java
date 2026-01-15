package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.MachineStatsResponse;
import com.file.intern.assignment.dto.TopDefectLineResponse;
import com.file.intern.assignment.repository.MachineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final MachineEventRepository repository;

    public MachineStatsResponse getMachineStats(
            String machineId,
            Instant start,
            Instant end
    ) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("INVALID_TIME_WINDOW");
        }

        long eventsCount = repository.countEventsForMachine(machineId, start, end);
        long defectsCount = repository.sumDefectsForMachine(machineId, start, end);

        double windowHours = Duration.between(start, end).toSeconds() / 3600.0;
        double avgDefectRate = windowHours == 0
                ? 0.0
                : defectsCount / windowHours;

        String status = avgDefectRate < 2.0 ? "Healthy" : "Warning";

        return MachineStatsResponse.builder()
                .machineId(machineId)
                .start(start)
                .end(end)
                .eventsCount(eventsCount)
                .defectsCount(defectsCount)
                .avgDefectRate(round(avgDefectRate))
                .status(status)
                .build();
    }

    // B) Top Defect Lines Logic
    public List<TopDefectLineResponse> getTopDefectLines(
            String factoryId,
            Instant from,
            Instant to,
            int limit
    ) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("INVALID_TIME_WINDOW");
        }

        List<Object[]> rawResults =
                repository.findTopDefectLinesRaw(factoryId, from, to);

        List<TopDefectLineResponse> response = new ArrayList<>();

        for (Object[] row : rawResults) {
            if (response.size() >= limit) break;

            String lineId = (String) row[0];
            long totalDefects = (long) row[1];
            long eventCount = (long) row[2];

            double defectsPercent = eventCount == 0
                    ? 0.0
                    : (totalDefects * 100.0) / eventCount;

            response.add(TopDefectLineResponse.builder()
                    .lineId(lineId)
                    .totalDefects(totalDefects)
                    .eventCount(eventCount)
                    .defectsPercent(round(defectsPercent))
                    .build());
        }
        return response;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}

