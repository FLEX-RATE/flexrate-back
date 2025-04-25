package com.flexrate.flexrate_back.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
    // 테스트용 API
    @GetMapping("/test")
    public String test() {
        log.info("Logback -> Test API is working!");
        return "Test API is working!";
    }

}
