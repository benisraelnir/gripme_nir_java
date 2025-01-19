package com.grip.core;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class GripRequestInterceptor {
    @Autowired
    private Grip grip;
    
    @Before("execution(* com.grip.core.Grip.*(..)) && !execution(* com.grip.core.Grip.retrieveStyles())")
    public void beforeRequest() {
        grip.retrieveStyles();
    }
}