package com.epic.cms.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import static com.epic.cms.util.LogManager.infoLogger;

@Aspect
@Component
public class ProcessTracker {
    @Pointcut("execution(* com.epic.cms.service.ConsumerService.*(..))")
    public void consumerPc() {
    }

    @Around("consumerPc()")
    public void consumerAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long starTime = System.currentTimeMillis();
        joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        infoLogger.info("Class Name: {}, Method Name: {}, Process Start Time: {}, Process End Time: {}, Time taken for Execution is : {} ms", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), starTime, endTime, (endTime - starTime));
    }
}
