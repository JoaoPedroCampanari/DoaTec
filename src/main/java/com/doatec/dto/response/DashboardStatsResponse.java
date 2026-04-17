package com.doatec.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DashboardStatsResponse(
    long totalDonations,
    LocalDate lastDonationDate
) {}