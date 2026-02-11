package com.paymentteamproject.domain.plan.controller;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.domain.plan.dto.GetPlanResponse;
import com.paymentteamproject.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    // 구독 플랜 목록 조회
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<GetPlanResponse>>> getAllPlans() {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK, "구독 플랜 목록 조회를 성공했습니다.", planService.getAll()));
    }

    // 구독 플랜 단건 조회
    @GetMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<GetPlanResponse>> getOnePlan(
            @PathVariable Long planId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK, "구독 플랜 단건 조회를 성공했습니다.", planService.getOne(planId)));
    }
}
