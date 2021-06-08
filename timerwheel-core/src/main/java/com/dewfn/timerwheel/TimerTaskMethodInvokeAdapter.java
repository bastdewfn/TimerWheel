package com.dewfn.timerwheel.autoconfigure;

import com.dewfn.timerwheel.TimerWheel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

public class TimerTaskMethodInvokeAdapter {
    private Logger logger = LoggerFactory.getLogger(TimerTaskMethodInvokeAdapter.class);

    private Object targetObject;
    private Method method;

    public TimerTaskMethodInvokeAdapter(Object targetObject, Method method) {
        this.targetObject = targetObject;
        this.method = method;
    }

    protected Object doInvoke(Object args) {
        Object bean =targetObject;
        if (bean.equals(null)) {
            return null;
        }

        ReflectionUtils.makeAccessible(this.method);
        try {
            return this.method.invoke(bean, args);
        }
        catch (Exception ex) {
            logger.error("call TimerWheelTask fail, method:{}",method.getName(), ex);
        }
        return null;
    }
}
