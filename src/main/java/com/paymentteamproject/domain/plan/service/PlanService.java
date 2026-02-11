package com.paymentteamproject.domain.plan.service;

import com.paymentteamproject.domain.plan.dto.GetPlanResponse;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.plan.exception.PlanNotFoundException;
import com.paymentteamproject.domain.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;

    // 구독 플랜 목록 조회
    @Transactional(readOnly = true)
    public List<GetPlanResponse> getAll() {
        List<Plan> plans = planRepository.findAll();

        return plans.stream()
                .map(p -> new GetPlanResponse(
                        p.getPlanId(),
                        p.getName(),
                        p.getPrice(),
                        p.getBillingCycle()
                )).toList();
    }

    // 구독 플랜 단건 조회
    @Transactional(readOnly = true)
    public GetPlanResponse getOne(Long planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(
                () -> new PlanNotFoundException("존재하지 않는 구독 플랜입니다."));

        return new GetPlanResponse(
                plan.getPlanId(),
                plan.getName(),
                plan.getPrice(),
                plan.getBillingCycle());
    }
}
