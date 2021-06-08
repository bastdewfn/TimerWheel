package com.dewfn.timerwheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

public class TimerTaskConsumerManager {
    private static Map<Class, List<TimerTaskMethodInvokeAdapter>> consumerTimerTaskMap = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(TimerTaskConsumerManager.class);


    public static List<TimerTaskMethodInvokeAdapter> getConsumerTimerTaskList(Class type) {
        return consumerTimerTaskMap.getOrDefault(type, null);
    }



    public static void addConsumerTimerTaskList(Method consumerTimerTask, Class paramType,Object targetObject) {
        synchronized (consumerTimerTaskMap) {

            List<TimerTaskMethodInvokeAdapter> methods = consumerTimerTaskMap.computeIfAbsent(paramType, (key) -> {
                return new LinkedList<TimerTaskMethodInvokeAdapter>();
            });
            methods.add(new TimerTaskMethodInvokeAdapter(targetObject,consumerTimerTask));
        }
        logger.info("添加一个延时任务事件,方法名:{},类型:{}",consumerTimerTask.getName(),targetObject.getClass().getName());
    }


}
