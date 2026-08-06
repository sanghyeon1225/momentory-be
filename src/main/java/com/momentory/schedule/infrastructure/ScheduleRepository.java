package com.momentory.schedule.infrastructure;

import com.momentory.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUser_IdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(Long userId, LocalDate scheduleDate);

    Optional<Schedule> findTopByUser_IdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderDesc(Long userId, LocalDate scheduleDate);

    Optional<Schedule> findByIdAndUser_Id(Long id, Long userId);

    Optional<Schedule> findByIdAndUser_IdAndExternalIdIsNullAndDeletedAtIsNull(Long id, Long userId);
}
