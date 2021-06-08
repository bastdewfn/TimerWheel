package com.dewfn.timerwheel.autoconfigure;

import com.dewfn.timerwheel.ITimerWheel;
import com.dewfn.timerwheel.TimerTaskConsumerManager;
import com.dewfn.timerwheel.TimerTaskMethodInvokeAdapter;
import com.dewfn.timerwheel.TimerWheel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(TimerWheelProperties.class)
@Configuration
@ConditionalOnClass({TimerWheel.class})
@Import(TimerWheelEventListenerMethodProcessor.class)
public class TimerWheelAutoConfiguration implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(TimerWheelAutoConfiguration.class);

    private  final TimerWheelProperties properties;

    public TimerWheelAutoConfiguration(TimerWheelProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ITimerWheel timerWheel() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        TimerWheel timerWheel=(TimerWheel)Class.forName("com.dewfn.timerwheel."+properties.getTimerWheelClass()).newInstance();
        timerWheel.setMaxTaskCount(properties.getMaxTaskCount());
        timerWheel.setWheelSize(properties.getWheelSize());
        timerWheel.setTimerFrequency(properties.getTimerFrequency());
        timerWheel.setTaskExecCoreThreadSize(properties.getTaskExecCoreThreadSize());
        timerWheel.setTaskExecMaxThreadSize(properties.getTaskExecMaxThreadSize());
        timerWheel.setTaskExecMaxThreadKeepLiveTime(properties.getTaskExecMaxThreadKeepLiveTime(), TimeUnit.MILLISECONDS);
        timerWheel.setTaskExecWorkingMaxThreadSize(properties.getTaskExecWorkingMaxThreadSize());
        timerWheel.setTaskExecWorkingWaitForSize(properties.getTaskExecWorkingWaitForSize());
        timerWheel.setDeviationSizeCorrectTask(properties.getDeviationSizeCorrectTask());
        timerWheel.registConsumerTimerTask((data)->{
            List<TimerTaskMethodInvokeAdapter> timerTaskMethodInvokeAdapters= TimerTaskConsumerManager.getConsumerTimerTaskList(data.getClass());
            if(timerTaskMethodInvokeAdapters!=null&&!timerTaskMethodInvokeAdapters.isEmpty()) {
                for (TimerTaskMethodInvokeAdapter adapter : timerTaskMethodInvokeAdapters) {
                    try {
                        adapter.doInvoke(data);
                    } catch (Exception ex) {
                        logger.error("call TimerWheelTask fail", ex);
                    }
                }
            }
        });
        return timerWheel;
    }


//    public TimerWheelEventListenerMethodProcessor timerWheelEventListenerMethodProcessor(){
//        TimerWheelEventListenerMethodProcessor timerWheelEventListenerMethodProcessor=new TimerWheelEventListenerMethodProcessor();
//        return timerWheelEventListenerMethodProcessor;
//    }
    @Override
    public void afterPropertiesSet() throws Exception {
        if(properties!=null) {
            if (properties.isAutoStart()) {
                timerWheel().start();
            }
        }
    }

}
