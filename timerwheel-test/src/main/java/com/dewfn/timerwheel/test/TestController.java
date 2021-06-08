package com.dewfn.timerwheel.test;

import com.dewfn.timerwheel.ITimerWheel;
import com.dewfn.timerwheel.TimerWheel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {
    @Autowired
    private ITimerWheel timerWheel;
    @GetMapping("/test")
    public String test(String id){
        SourceData sourceData=new SourceData();
        sourceData.setId(id);
        sourceData.setAge(new Random(100).nextInt());

        if(timerWheel.submitTask(sourceData,3, TimeUnit.SECONDS,id)){
            return "成功";
        }
        return "失败";
    }
}
