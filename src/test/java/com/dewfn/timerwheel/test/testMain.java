package com.dewfn.timerwheel.test;

import com.dewfn.timerwheel.TimerWheel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class testMain {
    Logger log= LoggerFactory.getLogger(this.getClass());
    @Test
    public void testOne() throws InterruptedException {
        TimerWheel timerWheel=new TimerWheel();
        timerWheel.start();
        timerWheel.registConsumerTimerTask(sourceData->{
            log.info("任务执行:{}", sourceData);
        });

        timerWheel.submitTask(1,1, TimeUnit.SECONDS,String.valueOf(1));
        timerWheel.submitTask(3,60,TimeUnit.SECONDS,String.valueOf(3));
        timerWheel.submitTask(2,15,TimeUnit.SECONDS,String.valueOf(2));
        Thread.sleep(1000000000);

    }
    @Test
    public void testOne2() throws InterruptedException {
        TimerWheel timerWheel=new TimerWheel();

        timerWheel.start();

        timerWheel.submitTask((d)->{
            log.info("任务执行1:{}",d);
        },1,5, TimeUnit.SECONDS,String.valueOf(1));
        timerWheel.submitTask((d)->{
            log.info("任务执行2:{}",d);
        },2,12, TimeUnit.SECONDS,String.valueOf(2));
        Thread.sleep(1000000000);

    }
    @Test
    public void testMany() throws InterruptedException {
        Random random=new Random();
        TimerWheel timerWheel=new TimerWheel();
        timerWheel.setMaxTaskCount(1000000); //最大任务数
        timerWheel.setTaskExecCoreThreadSize(3); //执行任务的核心线程数
        timerWheel.setTaskExecMaxThreadSize(5); //执行任务的最大线程数
        timerWheel.setTaskExecWorkingWaitForSize(100); //如果积压数超过N，则增加线程数至最大线程数
        timerWheel.setTaskExecMaxThreadKeepLiveTime(5,TimeUnit.MINUTES); //超过5分钟,则回收线程数 ，减少到核心线程数量
        timerWheel.setTimerFrequency(1000); //时间刻度  1000毫秒
        timerWheel.setDeviationSizeCorrectTask(5); //任务时间不准确时，超过N个刻度 则调整到正确刻度
        timerWheel.setWheelSize(60); //时间轮一圈多少大， 比如一刻度 为1秒，一圈60则为1分钟
        timerWheel.setTaskExecWorkingMaxThreadSize(100000); //排队等待执行的任务数，相当于积压数，如果执行任务时有阻塞，则会排队
        timerWheel.start();
        timerWheel.registConsumerTimerTask(sourceData->{
            log.info("任务执行:{}", sourceData);
        });
                for (int k = 0; k < 5000000; k++) {
            int finalK = k;
            int panSecone=random.nextInt(10000)+2;
//            log.info("添加一个任务:{},时间:{}",k,panSecone);
            timerWheel.submitTask(panSecone,panSecone,TimeUnit.SECONDS,String.valueOf(k));
//            Thread.sleep(200);

        }
        Thread.sleep(1000000000);

    }
}
