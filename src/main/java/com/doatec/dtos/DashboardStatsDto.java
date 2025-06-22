package com.doatec.dtos;

import java.time.LocalDate;

public class DashboardStatsDto {

    private long totalDonations;
    private LocalDate lastDonationDate;

    public DashboardStatsDto(long totalDonations, LocalDate lastDonationDate) {
        this.totalDonations = totalDonations;
        this.lastDonationDate = lastDonationDate;
    }

    public long getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(long totalDonations) {
        this.totalDonations = totalDonations;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }
}