package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.DashboardResponse;
import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
    final DashboardResponse response = dashboardService.getDashboardData();
    return ResponseEntity.ok(ApiResponse.of("OK", "Dashboard data retrieved", response));
  }
}
