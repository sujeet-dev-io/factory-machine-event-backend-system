package com.file.intern.assignment.controller;

import com.file.intern.assignment.dto.BatchIngestResponse;
import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.service.EventIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventIngestionController {

    private final EventIngestionService ingestionService;
   //  POST /events/batch
    @PostMapping("/events/batch")
    public ResponseEntity<BatchIngestResponse> ingestBatch(
            @RequestBody List<EventIngestRequest> events
    ) {
        BatchIngestResponse  response = ingestionService.ingestBatch(events);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
