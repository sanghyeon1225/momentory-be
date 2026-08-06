package com.momentory.schedule.application;

import com.momentory.schedule.domain.Schedule;
import com.momentory.schedule.infrastructure.ScheduleRepository;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import com.momentory.user.domain.User;
import com.momentory.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleResult> getSchedules(Long userId, LocalDate date) {
        requireUser(userId);
        return scheduleRepository.findByUser_IdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(userId, date)
                .stream()
                .map(ScheduleResult::from)
                .toList();
    }

    @Transactional
    public ScheduleResult createManualSchedule(Long userId, LocalDate date, String title) {
        User user = requireUser(userId);
        long displayOrder = scheduleRepository
                .findTopByUser_IdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderDesc(userId, date)
                .map(schedule -> schedule.getDisplayOrder() + 1)
                .orElse(0L);

        Schedule schedule = scheduleRepository.save(Schedule.createManual(user, date, title, displayOrder));
        return ScheduleResult.from(schedule);
    }

    @Transactional
    public ScheduleResult updateManualSchedule(Long userId, Long scheduleId, LocalDate date, String title) {
        requireUser(userId);
        Schedule schedule = scheduleRepository.findByIdAndUser_IdAndExternalIdIsNullAndDeletedAtIsNull(scheduleId, userId)
                .orElseThrow(ScheduleNotFoundException::new);
        schedule.update(date, title);
        return ScheduleResult.from(schedule);
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        requireUser(userId);
        Schedule schedule = scheduleRepository.findByIdAndUser_Id(scheduleId, userId)
                .orElseThrow(ScheduleNotFoundException::new);
        schedule.delete(Instant.now());
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(AuthenticatedUserNotFoundException::new);
    }
}
