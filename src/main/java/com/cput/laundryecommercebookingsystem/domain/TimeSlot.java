package com.cput.laundryecommercebookingsystem.domain;


// 222665963 Libolwetu Nokenke


import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_slot_id")
    private Long id;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "date")
    private LocalDate date;

    protected TimeSlot() {
    }
    private TimeSlot(Builder builder) {
        this.id = builder.id;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.date = builder.date;
        this.isAvailable = builder.isAvailable;
    }
    public static Builder builder() {
        return new Builder();
    }
    public Long getId() {
        return id;
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public LocalTime getEndTime() {
        return endTime;
    }
    public LocalDate getDate() {
        return date;
    }
    public boolean isAvailable() {
        return isAvailable;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return isAvailable == timeSlot.isAvailable &&
                Objects.equals(id, timeSlot.id) &&
                Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime) &&
                Objects.equals(date, timeSlot.date);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, startTime, endTime, date, isAvailable);
    }
    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", date=" + date +
                ", isAvailable=" + isAvailable +
                '}';
    }
    public static class Builder {

        private Long id;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate date;
        private boolean isAvailable;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }
        public Builder setStartTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }
        public Builder setEndTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }
        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }
        public Builder setAvailable(boolean isAvailable) {
            this.isAvailable = isAvailable;
            return this;
        }
        public TimeSlot build() {
            return new TimeSlot(this);
        }
    }
}