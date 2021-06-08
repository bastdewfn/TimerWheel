package com.dewfn.timerwheel.test;

import com.dewfn.timerwheel.autoconfigure.annotation.TimerTaskCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventService {

    Logger log= LoggerFactory.getLogger(this.getClass());
    @TimerTaskCall(SourceData.class)
    private void callBack(SourceData sourceData){
        log.info("任务执行了,id:{},年龄:{}",sourceData.getId(),sourceData.getAge());
    }
}
