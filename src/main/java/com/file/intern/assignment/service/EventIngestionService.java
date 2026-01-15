package com.file.intern.assignment.service;

import com.file.intern.assignment.dto.BatchIngestResponse;
import com.file.intern.assignment.dto.EventIngestRequest;
import com.file.intern.assignment.dto.EventRejection;
import com.file.intern.assignment.entity.MachineEvent;
import com.file.intern.assignment.repository.MachineEventRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventIngestionService {

    private static final long MAX_DURATION_MS = Duration.ofHours(6).toMillis();
    private static final Duration FUTURE_THRESHOLD = Duration.ofMinutes(15);

    @Autowired
    private final MachineEventRepository repository;

    @Transactional
    public BatchIngestResponse ingestBatch(List<EventIngestRequest> events) {

        int accepted = 0;
        int deduped = 0;
        int updated = 0;
        int rejected = 0;

        List<EventRejection> rejections = new ArrayList<>();
        for (EventIngestRequest ingestRequest : events) {
            try {
                validate(ingestRequest);
                String payloadHash = generatePayloadHash(ingestRequest);
                Instant now = Instant.now();
                Optional<MachineEvent> existingOpt =
                        repository.findByEventIdForUpdate(ingestRequest.getEventId());

                if (existingOpt.isEmpty()) {
                    MachineEvent machineEvent = toEntity(ingestRequest, payloadHash, now);
                    repository.save(machineEvent);
                    accepted++;
                    continue;
                }

                MachineEvent existing = existingOpt.get();
                if (existing.getPayloadHash().equals(payloadHash)) {
                    deduped++;
                    continue;
                }

                if (now.isAfter(existing.getReceivedTime())) {
                    updateEntity(existing, ingestRequest, payloadHash, now);
                    repository.save(existing);
                    updated++;
                } else {
                    deduped++;
                }

            } catch (ValidationException ex) {
                rejected++;
                rejections.add(EventRejection.builder()
                        .eventId(ingestRequest.getEventId())
                        .reason(ex.getReason())
                        .build());
            }
        }
        return BatchIngestResponse.builder()
                .accepted(accepted)
                .updated(updated)
                .deduped(deduped)
                .rejected(rejected)
                .rejections(rejections)
                .build();
    }

    //        Validation Logic

    private void validate(EventIngestRequest dto) {

        if (dto.getDurationMs() == null
                || dto.getDurationMs() < 0
                || dto.getDurationMs() > MAX_DURATION_MS) {
            throw new ValidationException("INVALID_DURATION");
        }
        if (dto.getEventTime() == null
                || dto.getEventTime().isAfter(Instant.now().plus(FUTURE_THRESHOLD))) {
            throw new ValidationException("FUTURE_EVENT_TIME");
        }
    }

    // Mapping Helpers
    private MachineEvent toEntity(EventIngestRequest request,
                                  String payloadHash,
                                  Instant receivedTime) {

        return MachineEvent.builder()
                .eventId(request.getEventId())
                .eventTime(request.getEventTime())
                .receivedTime(receivedTime)
                .machineId(request.getMachineId())
                .lineId(request.getLineId())
                .factoryId(request.getFactoryId())
                .durationMs(request.getDurationMs())
                .defectCount(request.getDefectCount())
                .payloadHash(payloadHash)
                .build();
    }

    private void updateEntity(MachineEvent entity,
                              EventIngestRequest request,
                              String payloadHash,
                              Instant receivedTime) {

        entity.setEventTime(request.getEventTime());
        entity.setMachineId(request.getMachineId());
        entity.setLineId(request.getLineId());
        entity.setFactoryId(request.getFactoryId());
        entity.setDurationMs(request.getDurationMs());
        entity.setDefectCount(request.getDefectCount());
        entity.setPayloadHash(payloadHash);
        entity.setReceivedTime(receivedTime);
    }

    //       Payload Hash (Dedupe)
    private String generatePayloadHash(EventIngestRequest dto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String payload = dto.getEventId() + "|"
                    + dto.getEventTime() + "|"
                    + dto.getMachineId() + "|"
                    + dto.getLineId() + "|"
                    + dto.getFactoryId() + "|"
                    + dto.getDurationMs() + "|"
                    + dto.getDefectCount();

            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new RuntimeException("HASH_GENERATION_FAILED", e);
        }
    }


     @Getter
    public static class ValidationException extends RuntimeException {
        private final String reason;
        ValidationException(String reason) {
            this.reason = reason;
        }
    }

}
