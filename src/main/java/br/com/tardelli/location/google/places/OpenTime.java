package br.com.tardelli.location.google.places;

import java.text.DateFormatSymbols;

public class OpenTime {

    private int day;

    private String time;

    private int getDay() {
        return this.day;
    }

    private String getLocalizedDay() {
        DateFormatSymbols symbols = new DateFormatSymbols();
        return symbols.getWeekdays()[this.getDay() + 1];
    }

    public String getShortLocalizedDay() {
        DateFormatSymbols symbols = new DateFormatSymbols();
        return symbols.getShortWeekdays()[this.getDay() + 1];
    }

    private String getTime() {
        return this.time;
    }

    public String toString() {
        return this.getLocalizedDay() + " at " + this.getTime();
    }
}
