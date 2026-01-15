package com.file.intern.assignment.controller;

import com.file.intern.assignment.dto.MachineStatsResponse;
import com.file.intern.assignment.dto.TopDefectLineResponse;
import com.file.intern.assignment.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
class StatsController {


    private final StatsService statsService;

    // GET /stats?machineId=&start=&end=
    @GetMapping
    public ResponseEntity<MachineStatsResponse> getMachineStats(
            @RequestParam String machineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        MachineStatsResponse response =
                statsService.getMachineStats(machineId, start, end);
        return ResponseEntity.ok(response);
    }

    // GET /stats/top-defect-linesmachine_event
    @GetMapping("/top-defect-lines")
    public ResponseEntity<List<TopDefectLineResponse>> getTopDefectLines(
            @RequestParam String factoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TopDefectLineResponse> response =
                statsService.getTopDefectLines(factoryId, from, to, limit);
        return ResponseEntity.ok(response);
    }
}