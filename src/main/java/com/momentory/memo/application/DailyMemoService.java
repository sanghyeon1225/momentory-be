package com.momentory.memo.application;

import com.momentory.memo.infrastructure.DailyMemoRepository;
import com.momentory.user.application.AuthenticatedUserNotFoundException;
import com.momentory.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DailyMemoService {

    private final DailyMemoRepository dailyMemoRepository;
    private final UserRepository userRepository;

    public DailyMemoService(DailyMemoRepository dailyMemoRepository, UserRepository userRepository) {
        this.dailyMemoRepository = dailyMemoRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DailyMemoResult getDailyMemo(Long userId, LocalDate date) {
        requireUser(userId);
        return dailyMemoRepository.findByUserIdAndMemoDate(userId, date)
                .map(DailyMemoResult::from)
                .orElseThrow(DailyMemoNotFoundException::new);
    }

    @Transactional
    public DailyMemoResult saveDailyMemo(Long userId, LocalDate date, String content) {
        requireUser(userId);
        dailyMemoRepository.upsert(userId, date, content);
        return dailyMemoRepository.findByUserIdAndMemoDate(userId, date)
                .map(DailyMemoResult::from)
                .orElseThrow(IllegalStateException::new);
    }

    @Transactional
    public void deleteDailyMemo(Long userId, LocalDate date) {
        requireUser(userId);
        dailyMemoRepository.deleteByUserIdAndMemoDate(userId, date);
    }

    private void requireUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(AuthenticatedUserNotFoundException::new);
    }
}
