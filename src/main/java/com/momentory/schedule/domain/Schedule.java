package com.momentory.schedule.domain;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "schedules")
public class Schedule extends BaseTimeEntity {

    public static final int TITLE_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    @Column(length = 30)
    private String emotion;

    @Column(name = "display_order", nullable = false)
    private long displayOrder;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Schedule() {
    }

    private Schedule(User user, LocalDate scheduleDate, String title, long displayOrder) {
        this.user = Objects.requireNonNull(user);
        this.scheduleDate = Objects.requireNonNull(scheduleDate);
        this.title = validateTitle(title);
        this.displayOrder = displayOrder;
        this.completed = false;
    }

    public static Schedule createManual(User user, LocalDate scheduleDate, String title, long displayOrder) {
        return new Schedule(user, scheduleDate, title, displayOrder);
    }

    public void update(LocalDate scheduleDate, String title) {
        this.scheduleDate = Objects.requireNonNull(scheduleDate);
        this.title = validateTitle(title);
    }

    public void delete(Instant deletedAt) {
        if (this.deletedAt == null) {
            this.deletedAt = Objects.requireNonNull(deletedAt);
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getEmotion() {
        return emotion;
    }

    public long getDisplayOrder() {
        return displayOrder;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    private String validateTitle(String title) {
        String normalizedTitle = Objects.requireNonNull(title).strip();
        if (normalizedTitle.isBlank() || normalizedTitle.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("title must be between 1 and 255 characters");
        }
        return normalizedTitle;
    }
}
