package com.flexrate.flexrate_back.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
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

        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        String paramSummary = paramSummary(signature, args);
        if (!paramSummary.isEmpty()) {
            log.info("{} API 진입:\nparameters: {}", methodName, paramSummary);
        } else {
            log.info("{} API 진입", methodName);
        }

        Object result;
        try {
            result = joinPoint.proceed();
            String resultSummary = resultSummary(result);
            if (!resultSummary.isEmpty()) {
                log.info("{} API 성공:\nresult: {}", methodName, resultSummary);
            } else {
                log.info("{} API 성공", methodName);
            }
            return result;
        } catch (Throwable ex) {
            MDC.put("errorCode", "E001");
            MDC.put("details", ex.getMessage());
            log.warn("{} API 예외 발생", methodName);
            throw ex;
        }
    }

    // 파라미터 요약
    private String paramSummary(MethodSignature signature, Object[] args) {
        String[] paramNames = signature.getParameterNames();
        if (paramNames == null || paramNames.length == 0) return "";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paramNames.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                sb.append(paramNames[i]).append("=null, ");
                continue;
            }
            String typeName = arg.getClass().getSimpleName();
            if (typeName.contains("Principal") || typeName.contains("Servlet")) continue;

            if (isDtoType(typeName)) {
                sb.append(paramNames[i]).append("=").append(typeName).append("{***}, ");
            } else if (arg instanceof java.util.Map<?, ?> mapArg) {
                sb.append(paramNames[i]).append("=").append(maskMap(mapArg)).append(", ");
            } else {
                sb.append(paramNames[i]).append("=***, ");
            }
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    // 반환값 요약
    private String resultSummary(Object result) {
        if (result == null) return "";
        String type = result.getClass().getSimpleName();

        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            if (body == null) return "ResponseEntity<null>";
            if (body instanceof java.util.Map<?, ?> mapBody) {
                return "ResponseEntity<" + maskMap(mapBody) + ">";
            }
            if (isDtoType(body.getClass().getSimpleName())) {
                return "ResponseEntity<" + maskObject(body) + ">";
            }
            return "ResponseEntity<***>";
        }
        if (isDtoType(type)) {
            return maskObject(result);
        }
        if (result instanceof java.util.Map<?, ?> mapResult) {
            return maskMap(mapResult);
        }
        if (result instanceof String strResult) {
            return "String{***}";
        }
        return "";
    }

    private boolean isDtoType(String typeName) {
        return typeName.endsWith("Dto") || typeName.endsWith("Request") ||
                typeName.endsWith("Response") || typeName.endsWith("Command");
    }

    private String maskMap(java.util.Map<?, ?> map) {
        if (map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        map.forEach((key, value) -> sb.append(key).append("=***, "));
        if (sb.length() > 1) sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }

    private String maskObject(Object obj) {
        return obj.getClass().getSimpleName() + "{***}";
    }
}
