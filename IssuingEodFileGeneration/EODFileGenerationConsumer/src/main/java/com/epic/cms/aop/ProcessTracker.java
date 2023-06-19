package com.epic.cms.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProcessTracker {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");

    @Pointcut("execution(* com.epic.cms.service.ConsumerService.*(..))")
    public void consumerPc() {
    }

    @Around("consumerPc()")
    public void consumerAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long starTime = System.currentTimeMillis();
        joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        logInfo.info("Class Name: {}, Method Name: {}, Process Start Time: {}, Process End Time: {}, Time taken for Execution is : {} ms", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), starTime, endTime, (endTime - starTime));
    }
}
