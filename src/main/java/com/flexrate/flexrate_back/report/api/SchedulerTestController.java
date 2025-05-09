package com.flexrate.flexrate_back.report.api;

import com.flexrate.flexrate_back.report.scheduler.ConsumptionHabitReportScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class SchedulerTestController {

    private final ConsumptionHabitReportScheduler scheduler;

    @PostMapping("/run-report-scheduler")
    public ResponseEntity<Void> runScheduler() {
        scheduler.generateMonthlyReports();
        return ResponseEntity.ok().build();
    }
}
