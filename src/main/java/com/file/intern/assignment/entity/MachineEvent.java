package com.file.intern.assignment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "machine_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_machine_time", columnList = "machine_id,event_time"),
                @Index(name = "idx_factory_time", columnList = "factory_id,event_time")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineEvent{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "event_time", nullable = false)
    @JsonProperty("eventTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private Instant eventTime;



    @Column(name = "received_time", nullable = false)
    private Instant receivedTime;


    @Column(name = "machine_id", nullable = false, length = 64)
    private String machineId;

    @Column(name = "line_id", length = 64)
    private String lineId;


    @Column(name = "factory_id", length = 64)
    private String factoryId;


    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "defect_count", nullable = false)
    private Integer defectCount;


    // Hash of payload fields (used for dedupe comparison)
    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;


    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}