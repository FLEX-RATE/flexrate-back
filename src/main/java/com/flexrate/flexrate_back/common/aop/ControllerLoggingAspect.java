package com.flexrate.flexrate_back.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {}

    @Around("restController()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String className = signature.getDeclaringTypeName().replace("com.flexrate.flexrate_back.", "");
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        log.info("API 진입: {}.{}({})", className, methodName, paramSummary(signature, args));

        Object result;
        try {
            result = joinPoint.proceed();
            log.info("API 성공: {}.{} → {}", className, methodName, resultSummary(result));
            return result;
        } catch (Throwable ex) {
            log.warn("API 예외 발생: {}.{} - {}", className, methodName, ex.getMessage());
            throw ex;
        }
    }

    // 파라미터 요약
    private String paramSummary(MethodSignature signature, Object[] args) {
        String[] paramNames = signature.getParameterNames();
        if (paramNames == null) return "";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paramNames.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;

            String typeName = arg.getClass().getSimpleName();
            if (typeName.contains("Principal") || typeName.contains("Servlet")) continue;
            sb.append(paramNames[i]).append("=").append(arg).append(", ");
        }

        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    // 반환값 요약 (ResponseEntity, DTO, String)
    private String resultSummary(Object result) {
        if (result == null) return "null";
        String type = result.getClass().getSimpleName();

        if (type.contains("ResponseEntity")) {
            return "ResponseEntity<" + ((org.springframework.http.ResponseEntity<?>) result).getBody() + ">";
        }
        if (type.endsWith("Response") || type.endsWith("Dto") || type.equals("String")) {
            return result.toString();
        }

        return type;
    }
}
