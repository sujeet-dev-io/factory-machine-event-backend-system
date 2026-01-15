package com.file.intern.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineStatsResponse {

    private String machineId;
    private Instant start;
    private Instant end;
    private long eventsCount;
    private long defectsCount;
    private double avgDefectRate;
    private String status;
}
