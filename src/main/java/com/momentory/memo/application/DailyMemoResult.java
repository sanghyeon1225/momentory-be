package com.momentory.memo.application;

import com.momentory.memo.domain.DailyMemo;

import java.time.LocalDate;

public record DailyMemoResult(LocalDate date, String content) {

    public static DailyMemoResult from(DailyMemo dailyMemo) {
        return new DailyMemoResult(dailyMemo.getMemoDate(), dailyMemo.getContent());
    }
}
