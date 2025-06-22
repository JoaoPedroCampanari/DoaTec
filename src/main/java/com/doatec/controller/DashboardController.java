package com.doatec.controller;

import com.doatec.dtos.DashboardStatsDto;
import com.doatec.service.DoacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DoacaoService doacaoService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        DashboardStatsDto stats = doacaoService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}