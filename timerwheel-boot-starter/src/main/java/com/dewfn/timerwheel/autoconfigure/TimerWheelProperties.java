package com.dewfn.timerwheel.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timer-wheel")
public class TimerWheelProperties {
    private int maxTaskCount=10000000; //延时任务最大值
    private int wheelSize=100;//时间轮大小
    private int timerFrequency=1000;//时间刻度单位秒
    private int taskExecCoreThreadSize=3;    //执行任务时的核心线程数，相当于消费数，过少会引起阻塞，如果执行的任务消耗时间过长可设置大点

    private int taskExecMaxThreadSize=10;    //执行任务时最大线程数，相当于最大消费数

    private int taskExecMaxThreadKeepLiveTime=1000*60*5; //秒单位 线程，超过N秒后，最大线程会回收，只保留核心线程数
    private int taskExecWorkingMaxThreadSize=100000;//线程运行中  应该执行还未执行的任务最大数，最大积压数
    private int taskExecWorkingWaitForSize=10;// 线程运行中  应该执行还未执行的任务数，超过这积压数  就扩大线程
    private int deviationSizeCorrectTask=5;//时间轮自带修正任务偏移功能，如遇被分配，后期任务时间可能会不准，可以设置偏移超过N个刻度的任务
    private boolean autoStart=true;

    private String timerWheelClass="TimerWheel";

    public String getTimerWheelClass() {
        return timerWheelClass;
    }

    public void setTimerWheelClass(String timerWheelClass) {
        this.timerWheelClass = timerWheelClass;
    }

    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    public void setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
    }

    public int getWheelSize() {
        return wheelSize;
    }

    public void setWheelSize(int wheelSize) {
        this.wheelSize = wheelSize;
    }

    public int getTimerFrequency() {
        return timerFrequency;
    }

    public void setTimerFrequency(int timerFrequency) {
        this.timerFrequency = timerFrequency;
    }

    public int getTaskExecCoreThreadSize() {
        return taskExecCoreThreadSize;
    }

    public void setTaskExecCoreThreadSize(int taskExecCoreThreadSize) {
        this.taskExecCoreThreadSize = taskExecCoreThreadSize;
    }

    public int getTaskExecMaxThreadSize() {
        return taskExecMaxThreadSize;
    }

    public void setTaskExecMaxThreadSize(int taskExecMaxThreadSize) {
        this.taskExecMaxThreadSize = taskExecMaxThreadSize;
    }

    public int getTaskExecMaxThreadKeepLiveTime() {
        return taskExecMaxThreadKeepLiveTime;
    }

    public void setTaskExecMaxThreadKeepLiveTime(int taskExecMaxThreadKeepLiveTime) {
        this.taskExecMaxThreadKeepLiveTime = taskExecMaxThreadKeepLiveTime;
    }

    public int getTaskExecWorkingMaxThreadSize() {
        return taskExecWorkingMaxThreadSize;
    }

    public void setTaskExecWorkingMaxThreadSize(int taskExecWorkingMaxThreadSize) {
        this.taskExecWorkingMaxThreadSize = taskExecWorkingMaxThreadSize;
    }

    public int getTaskExecWorkingWaitForSize() {
        return taskExecWorkingWaitForSize;
    }

    public void setTaskExecWorkingWaitForSize(int taskExecWorkingWaitForSize) {
        this.taskExecWorkingWaitForSize = taskExecWorkingWaitForSize;
    }

    public int getDeviationSizeCorrectTask() {
        return deviationSizeCorrectTask;
    }

    public void setDeviationSizeCorrectTask(int deviationSizeCorrectTask) {
        this.deviationSizeCorrectTask = deviationSizeCorrectTask;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}
