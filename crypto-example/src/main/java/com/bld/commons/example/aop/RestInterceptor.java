package com.bld.commons.example.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bld.commons.example.model.ResponseMethod;
import com.fasterxml.jackson.core.JsonProcessingException;

@Aspect
@Component
public class RestInterceptor {

//	private final static Logger logger=LoggerFactory.getLogger(RestInterceptor.class);
//	
//	@Around("@within(com.bldcommons.example.annotation.ApiController)")
//	public Object aroundAsync(ProceedingJoinPoint joinPoint) throws JsonProcessingException {
//		Object[] objs = joinPoint.getArgs();
//		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//		Method method = signature.getMethod();
//		for(int i=0;i<objs.length;i++)
//			logger.info(method.getParameters()[i].getName()+": "+objs[i].toString());
//		return new ResponseMethod("success");
//	}
}
