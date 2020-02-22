package com.gmail;

import java.time.LocalTime;
import java.util.Objects;

/**
 * @author Alexander Balabolov on 12.02.2020
 */
public class TimeOffer implements Comparable<TimeOffer> {
    private Company company;
    private LocalTime startTime;
    private LocalTime endTime;
    
    TimeOffer(Company company, LocalTime startTime, LocalTime endTime) {
        this.company = company;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeOffer timeOffer = (TimeOffer) o;
        return company == timeOffer.company &&
            Objects.equals(startTime, timeOffer.startTime) &&
            Objects.equals(endTime, timeOffer.endTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(company, startTime, endTime);
    }
    
    @Override
    public int compareTo(TimeOffer timeOffer) {
        return company.equals(timeOffer.company) ? startTime.compareTo(timeOffer.getStartTime()) :
            Company.POSH.equals(company) ? -1 : 1;
    }
    
    @Override
    public String toString() {
        return company + " " + startTime + " " + endTime;
    }
}
