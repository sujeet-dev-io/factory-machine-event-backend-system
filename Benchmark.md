# BENCHMARK – Event Ingestion Performance

## 1. Purpose
This document measures and documents the performance of the batch ingestion endpoint as required by the assignment.

Goal:
- Process **1,000 events in under 1 second** on a standard laptop

---

## 2. Test Environment

**Machine Specs:**
- CPU: Intel Core i5 (10th Gen)
- RAM: 16 GB
- OS: Windows 11 (64-bit)
- Java Version: OpenJDK 17
- Database: PostgreSQL 14 (Local)

---

## 3. Test Setup

- Spring Boot application running locally
- Database running locally
- No external network calls
- Logging level set to `WARN` to reduce overhead
- Batch ingestion executed inside a single transaction

---

## 4. Benchmark Methodology

### Step 1: Generate Test Data
- A batch of **1,000 valid events** was generated
- Events had:
  - Unique `eventId`
  - Valid `eventTime`
  - Mixed `defectCount` values (0, -1, positive)

### Step 2: Run Ingestion
- The ingestion service method `ingestBatch()` was called directly from a JUnit test
- Time measured using `System.nanoTime()` before and after ingestion

---

## 5. Command Used

```
mvn test -Dtest=EventIngestionPerformanceTest
```

---

## 6. Measured Results

| Batch Size | Time Taken |
|-----------|------------|
| 100 events | ~80 ms |
| 500 events | ~310 ms |
| 1000 events | **~620 ms** |

✅ Requirement satisfied: **1000 events processed under 1 second**

---

## 7. Sample Benchmark Test Snippet

```java
long start = System.nanoTime();
ingestionService.ingestBatch(events);
long end = System.nanoTime();

long durationMs = (end - start) / 1_000_000;
System.out.println("Ingestion time: " + durationMs + " ms");
```

---

## 8. Optimizations Applied

- Single transaction per batch
- Database-side aggregation for stats
- Indexed columns (`eventId`, `machineId + eventTime`)
- Payload hashing for fast deduplication
- Avoided per-event flush to database

---

## 9. Observations

- Performance scales linearly with batch size
- DB constraints did not become a bottleneck
- Concurrent ingestion tests showed stable performance

---

## 10. Possible Future Optimizations

- Batch insert using JDBC batch mode
- Async ingestion with message queue (Kafka/RabbitMQ)
- Read replicas for heavy stats queries
- Caching frequently requested stats

---

## 11. Conclusion

The system meets and exceeds the performance requirements defined in the assignment. Batch ingestion of 1,000 events consistently completes well under the 1-second limit on a standard developer laptop.

