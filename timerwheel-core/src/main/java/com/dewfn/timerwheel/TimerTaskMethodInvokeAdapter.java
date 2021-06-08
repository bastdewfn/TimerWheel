package com.dewfn.timerwheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class TimerTaskMethodInvokeAdapter {
    private Logger logger = LoggerFactory.getLogger(TimerTaskMethodInvokeAdapter.class);

    private Object targetObject;
    private Method method;

    public TimerTaskMethodInvokeAdapter(Object targetObject, Method method) {
        this.targetObject = targetObject;
        this.method = method;
    }

    public Object doInvoke(Object args) {
        Object bean =targetObject;
        if (bean.equals(null)) {
            return null;
        }

        MyReflectionUtils.makeAccessible(this.method);
        try {
            return this.method.invoke(bean, args);
        }
        catch (Exception ex) {
            logger.error("call TimerWheelTask fail, method:{}",method.getName(), ex);
        }
        return null;
    }


}
