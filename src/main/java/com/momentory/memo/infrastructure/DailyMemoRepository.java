package com.momentory.memo.infrastructure;

import com.momentory.memo.domain.DailyMemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMemoRepository extends JpaRepository<DailyMemo, Long> {

    Optional<DailyMemo> findByUserIdAndMemoDate(Long userId, LocalDate memoDate);

    long countByUserIdAndMemoDate(Long userId, LocalDate memoDate);

    @Modifying
    @Query(value = """
            INSERT INTO daily_memos (user_id, memo_date, content, created_at, updated_at)
            VALUES (:userId, :memoDate, :content, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id, memo_date)
            DO UPDATE SET content = EXCLUDED.content, updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsert(@Param("userId") Long userId, @Param("memoDate") LocalDate memoDate, @Param("content") String content);

    @Modifying
    long deleteByUserIdAndMemoDate(Long userId, LocalDate memoDate);
}
