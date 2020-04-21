package com.pinframework;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class SpecialParserListDTO {
    private List<Long> longList;
    private List<DayOfWeek> dayOfWeekList;
    private List<LocalDateTime> localDateTimeList;
    private List<LocalDate> localDateList;
    private List<ZonedDateTime> zonedDateTimeList;

    public List<Long> getLongList() {
        return longList;
    }

    public void setLongList(List<Long> longList) {
        this.longList = longList;
    }

    public List<DayOfWeek> getDayOfWeekList() {
        return dayOfWeekList;
    }

    public void setDayOfWeekList(List<DayOfWeek> dayOfWeekList) {
        this.dayOfWeekList = dayOfWeekList;
    }

    public List<LocalDateTime> getLocalDateTimeList() {
        return localDateTimeList;
    }

    public void setLocalDateTimeList(List<LocalDateTime> localDateTimeList) {
        this.localDateTimeList = localDateTimeList;
    }

    public List<LocalDate> getLocalDateList() {
        return localDateList;
    }

    public void setLocalDateList(List<LocalDate> localDateList) {
        this.localDateList = localDateList;
    }

    public List<ZonedDateTime> getZonedDateTimeList() {
        return zonedDateTimeList;
    }

    public void setZonedDateTimeList(List<ZonedDateTime> zonedDateTimeList) {
        this.zonedDateTimeList = zonedDateTimeList;
    }
}
