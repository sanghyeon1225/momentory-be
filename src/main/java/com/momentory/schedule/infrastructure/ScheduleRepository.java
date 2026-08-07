package com.momentory.schedule.infrastructure;

import com.momentory.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(Long userId, LocalDate scheduleDate);

    Optional<Schedule> findTopByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderDesc(Long userId, LocalDate scheduleDate);

    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    Optional<Schedule> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Optional<Schedule> findByIdAndUserIdAndExternalIdIsNullAndDeletedAtIsNull(Long id, Long userId);
}
