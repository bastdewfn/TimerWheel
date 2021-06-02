package com.dewfn.timerwheel;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ConsumerTimeTask<T> implements TimeTask<TimeTaskConsumer<T>,T> {

    public ConsumerTimeTask(String taskId, TimeTaskConsumer<T> task, T sourceData, long delayTime) {
        this.taskId = taskId;
        this.task = task;
        this.sourceData = sourceData;
        this.delayTime = delayTime;
    }
    public ConsumerTimeTask(String taskId,  T sourceData, long delayTime) {
        this.taskId = taskId;
        this.sourceData = sourceData;
        this.delayTime = delayTime;
    }
    private String taskId;
    private TimeTaskConsumer<T> task;
    private int zoomIndex = 0;
    private long planExecTime;
    private long actualExecTime;
    private T sourceData;
    private long delayTime;
    public void setPlanExecTime(Date date){
        setPlanExecTime(date.getTime());
    }

    public TimeTaskConsumer<T> getTask() {
        return task;
    }


    public void setTask(TimeTaskConsumer task) {
        this.task = task;
    }

    public T getSourceData() {
        return sourceData;
    }

    public void setSourceData(T sourceData) {
        this.sourceData = sourceData;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getPlanExecTime() {
        return planExecTime;
    }

    public void setPlanExecTime(long planExecTime) {
        this.planExecTime = planExecTime;
        this.delayTime = planExecTime - System.currentTimeMillis();
    }

    @Override
    public void setDelayTime(long delayTime, TimeUnit unit) {
        this.delayTime=unit.toMillis(delayTime);
    }

    @Override
    public long getDelayTime() {
        return this.delayTime;
    }

    public long getActualExecTime() {
        return actualExecTime;
    }

    public long getSpanExecTime() {
        return actualExecTime - planExecTime;
    }

    public int getSpanWheelNum(int surplusWheelNum, long timerFrequency) {
        return surplusWheelNum - (int) (Math.round((double) (planExecTime - System.currentTimeMillis()) / timerFrequency));
    }

    public void setActualExecTime(long actualExecTime) {
        this.actualExecTime = actualExecTime;
    }

    public ConsumerTimeTask() {
    }

    public ConsumerTimeTask(TimeTaskConsumer task, int zoomIndex) {
        this.task = task;
        this.zoomIndex = zoomIndex;
    }


    public int getZoomIndex() {
        return zoomIndex;
    }

    public int reduceZoomIndex() {
        return zoomIndex--;
    }

    public void setZoomIndex(int zoomIndex) {
        this.zoomIndex = zoomIndex;
    }
}
