package com.file.intern.assignment.repository;

import com.file.intern.assignment.entity.MachineEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MachineEventRepository extends JpaRepository<MachineEvent, Long> {

    //Update support
    Optional<MachineEvent> findByEventId(String eventId);

    //1. Used in concurrent ingestion to avoid race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MachineEvent e where e.eventId = :eventId")
    Optional<MachineEvent> findByEventIdForUpdate(@Param("eventId") String eventId);

    // 2. Machine stats queries
    @Query("""
            select count(e)
            from MachineEvent e
            where e.machineId = :machineId
            and e.eventTime >= :start
            and e.eventTime < :end
            """)
    long countEventsForMachine(
            @Param("machineId") String machineId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
            select coalesce(sum(e.defectCount), 0)
            from MachineEvent e
            where e.machineId = :machineId
            and e.eventTime >= :start
            and e.eventTime < :end
            and e.defectCount <> -1
            """)
    long sumDefectsForMachine(
            @Param("machineId") String machineId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    //3. Top defect lines query
    @Query("""
            select e.lineId as lineId,
            sum(case when e.defectCount <> -1 then e.defectCount else 0 end) as totalDefects,
            count(e) as eventCount
            from MachineEvent e
            where e.factoryId = :factoryId
            and e.eventTime >= :from
            and e.eventTime < :to
            group by e.lineId
            order by totalDefects desc
            """)
    List<Object[]> findTopDefectLinesRaw(
            @Param("factoryId") String factoryId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // 4. Concurrency test support methods only for tests
    List<MachineEvent> findAllByMachineId(String machineId);
    @Query("select count(e) from MachineEvent e where e.machineId = :machineId")
    long countByMachineId(@Param("machineId") String machineId);


}
