package com.file.intern.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopDefectLineResponse {

    private String lineId;
    private long totalDefects;
    private long eventCount;

    // defects per 100 events (rounded to 2 decimals)
    private double defectsPercent;
}
