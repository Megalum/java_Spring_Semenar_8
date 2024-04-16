package ru.gb.springbootlesson8;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;

@Component
@Aspect
@Slf4j
public class LoggableAspect {

    @Pointcut("within(@ru.gb.springbootlesson8.Loggable *)")
    public void beansMethod(){}

    @Pointcut("@annotation(ru.gb.springbootlesson8.Loggable)")
    public void beansWithAnnotation(){}

    @Around("beansMethod() || beansWithAnnotation()")
    public Object loggable(ProceedingJoinPoint proceedingJoinPoint){
        long start = System.currentTimeMillis();
        Level level = extractLevel(proceedingJoinPoint);

        log.atLevel(level).log("target: " + proceedingJoinPoint.getTarget());
        log.atLevel(level).log("args: " + Arrays.toString(proceedingJoinPoint.getArgs()));
        log.atLevel(level).log("method: " + proceedingJoinPoint.getSignature());

        try {
            Thread.sleep(10);
            Object result = proceedingJoinPoint.proceed();
            log.atLevel(level).log(timer(proceedingJoinPoint, start));
            return result;
        } catch (Throwable e){
            log.atLevel(level).log(e.getMessage());
            log.atLevel(level).log(timer(proceedingJoinPoint, start));
        }
        return null;

    }

    private Level extractLevel(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Loggable annotation = signature.getMethod().getAnnotation(Loggable.class);
        if (annotation != null) {
            return annotation.level();
        }

        return joinPoint.getTarget().getClass().getAnnotation(Loggable.class).level();
    }

    private String timer(ProceedingJoinPoint proceedingJoinPoint, long start){
        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        String clas = proceedingJoinPoint.getTarget().toString()
                .replaceAll("ru.gb.springbootlesson8.", "");
        clas = clas.substring(0, clas.indexOf('@'));
        String method = proceedingJoinPoint.getSignature().toString();

        return clas + " - " + method.replaceAll("ru.gb.springbootlesson8.Volvo.", "") + ": " + elapsed + "мс.";
    }
}
