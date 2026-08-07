package com.momentory.schedule.application;

import com.momentory.schedule.domain.Schedule;
import com.momentory.schedule.domain.ScheduleEmotion;
import com.momentory.schedule.infrastructure.ScheduleRepository;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import com.momentory.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return scheduleRepository.findByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(userId, date)
                .stream()
                .map(ScheduleResult::from)
                .toList();
    }

    @Transactional
    public ScheduleResult createManualSchedule(Long userId, LocalDate date, String title) {
        requireUser(userId);
        long displayOrder = scheduleRepository
                .findTopByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderDesc(userId, date)
                .map(schedule -> schedule.getDisplayOrder() + 1)
                .orElse(0L);

        Schedule schedule = scheduleRepository.save(Schedule.createManual(userId, date, title, displayOrder));
        return ScheduleResult.from(schedule);
    }

    @Transactional
    public ScheduleResult updateManualSchedule(Long userId, Long scheduleId, LocalDate date, String title) {
        requireUser(userId);
        Schedule schedule = scheduleRepository.findByIdAndUserIdAndExternalIdIsNullAndDeletedAtIsNull(scheduleId, userId)
                .orElseThrow(ScheduleNotFoundException::new);
        schedule.update(date, title);
        return ScheduleResult.from(schedule);
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        requireUser(userId);
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(ScheduleNotFoundException::new);
        schedule.delete(Instant.now());
    }

    @Transactional
    public ScheduleCompletionResult changeCompletion(Long userId, Long scheduleId, boolean completed, ScheduleEmotion emotion) {
        requireUser(userId);
        Schedule schedule = scheduleRepository.findByIdAndUserIdAndDeletedAtIsNull(scheduleId, userId)
                .orElseThrow(ScheduleNotFoundException::new);
        schedule.changeCompletion(completed, emotion);
        return ScheduleCompletionResult.from(schedule);
    }

    @Transactional
    public void changeScheduleOrder(Long userId, LocalDate date, List<Long> scheduleIds) {
        requireUser(userId);
        if (scheduleIds.stream().distinct().count() != scheduleIds.size()) {
            throw new InvalidScheduleOrderException();
        }

        List<Schedule> requestedSchedules = scheduleRepository.findAllById(scheduleIds);
        if (requestedSchedules.size() != scheduleIds.size()) {
            throw new InvalidScheduleOrderException();
        }
        if (requestedSchedules.stream().anyMatch(schedule -> !schedule.belongsTo(userId))) {
            throw new ScheduleNotFoundException();
        }
        if (requestedSchedules.stream().anyMatch(Schedule::isDeleted)
                || requestedSchedules.stream().anyMatch(schedule -> !date.equals(schedule.getScheduleDate()))) {
            throw new InvalidScheduleOrderException();
        }

        Set<Long> requestedScheduleIds = Set.copyOf(scheduleIds);
        Set<Long> activeScheduleIds = scheduleRepository
                .findByUserIdAndScheduleDateAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(userId, date)
                .stream()
                .map(Schedule::getId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!activeScheduleIds.equals(requestedScheduleIds)) {
            throw new InvalidScheduleOrderException();
        }

        Map<Long, Schedule> schedulesById = requestedSchedules.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Schedule::getId, schedule -> schedule));
        for (int index = 0; index < scheduleIds.size(); index++) {
            schedulesById.get(scheduleIds.get(index)).changeDisplayOrder(index);
        }
    }

    private void requireUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(AuthenticatedUserNotFoundException::new);
    }
}
