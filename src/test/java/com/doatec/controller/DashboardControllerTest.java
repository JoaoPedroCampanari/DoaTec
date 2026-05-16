package com.doatec.controller;

import com.doatec.dto.response.DashboardStatsResponse;
import com.doatec.service.DoacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController")
class DashboardControllerTest {

    @Mock
    private DoacaoService doacaoService;

    @InjectMocks
    private DashboardController dashboardController;

    @Nested
    @DisplayName("getStats endpoint")
    class GetStatsTests {

        @Test
        @DisplayName("GET /api/dashboard/stats retorna 200 com estatisticas")
        void retornaDashboardStats() {
            DashboardStatsResponse stats = DashboardStatsResponse.builder()
                    .totalDonations(42L)
                    .lastDonationDate(LocalDate.of(2026, 5, 15))
                    .build();

            when(doacaoService.getDashboardStats()).thenReturn(stats);

            ResponseEntity<DashboardStatsResponse> response = dashboardController.getStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(42L, response.getBody().totalDonations());
            assertEquals(LocalDate.of(2026, 5, 15), response.getBody().lastDonationDate());
            verify(doacaoService).getDashboardStats();
        }

        @Test
        @DisplayName("GET /api/dashboard/stats retorna 200 com zeros quando nao ha doacoes")
        void retornaStatsVazio() {
            DashboardStatsResponse stats = DashboardStatsResponse.builder()
                    .totalDonations(0L)
                    .lastDonationDate(null)
                    .build();

            when(doacaoService.getDashboardStats()).thenReturn(stats);

            ResponseEntity<DashboardStatsResponse> response = dashboardController.getStats();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(0L, response.getBody().totalDonations());
            assertNull(response.getBody().lastDonationDate());
            verify(doacaoService).getDashboardStats();
        }
    }
}
