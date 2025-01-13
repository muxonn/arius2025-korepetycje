package com.example.myapplication.HttpModels;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DaySchedule {
    private String availableFrom;
    private String availableUntil;
    private List<Integer> workingDays;
    private List<String> occupiedDates;

    // Constructor with parameters
    public DaySchedule(String availableFrom, String availableUntil, List<Integer> workingDays, List<String> occupiedDates) {
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.workingDays = workingDays;
        this.occupiedDates = occupiedDates;
    }

    // Constructor to initialize DaySchedule from CalendarResponse and a list of lessons
    public DaySchedule(CalendarResponse calendarResponse, List<Lesson> lessons) {
        this.availableFrom = calendarResponse.getAvailableFrom();
        this.availableUntil = calendarResponse.getAvailableUntil();
        this.workingDays = calendarResponse.getWorkingDays();
        if (lessons != null) {
            this.occupiedDates = lessons.stream()
                    .map(Lesson::getDate)
                    .collect(Collectors.toList());
        } else {
            this.occupiedDates = new ArrayList<>();
        }
    }

    // Getters and setters
    public String getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(String availableFrom) {
        this.availableFrom = availableFrom;
    }

    public String getAvailableUntil() {
        return availableUntil;
    }

    public void setAvailableUntil(String availableUntil) {
        this.availableUntil = availableUntil;
    }

    public List<Integer> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(List<Integer> workingDays) {
        this.workingDays = workingDays;
    }

    public List<String> getOccupiedDates() {
        return occupiedDates;
    }

    public void setOccupiedDates(List<String> occupiedDates) {
        this.occupiedDates = occupiedDates;
    }
}
