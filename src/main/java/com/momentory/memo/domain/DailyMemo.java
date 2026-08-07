package com.momentory.memo.domain;

import com.momentory.common.persistence.BaseTimeEntity;
import com.momentory.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_memos",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_memos_user_memo_date", columnNames = {"user_id", "memo_date"})
)
public class DailyMemo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "memo_date", nullable = false)
    private LocalDate memoDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected DailyMemo() {
    }

    private DailyMemo(User user, LocalDate memoDate, String content) {
        this.user = user;
        this.memoDate = memoDate;
        this.content = content;
    }

    public static DailyMemo create(User user, LocalDate memoDate, String content) {
        return new DailyMemo(user, memoDate, content);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getMemoDate() {
        return memoDate;
    }

    public String getContent() {
        return content;
    }
}
