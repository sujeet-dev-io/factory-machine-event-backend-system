package com.file.intern.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchIngestResponse {
    private int accepted;
    private int deduped;
    private int updated;
    private int rejected;

    private List<EventRejection> rejections;
}
