package com.pinframework;

import com.google.gson.annotations.SerializedName;

public enum CustomMonth {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    @SerializedName(value = "MAYO", alternate = { "mayo", "mayito" })
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;
}
