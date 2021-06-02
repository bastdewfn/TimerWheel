package com.dewfn.timerwheel;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface ITimerWheel<C> {
    int getMaxTaskCount();

    void setMaxTaskCount(int maxTaskCount);

    int getWheelSize();

    void setWheelSize(int wheelSize);

    long getTimerFrequency();

    void setTimerFrequency(long timerFrequency);

    int getTaskExecCoreThreadSize();

    void setTaskExecCoreThreadSize(int taskExecCoreThreadSize);

    int getTaskExecMaxThreadSize();

    void setTaskExecMaxThreadSize(int taskExecMaxThreadSize);

    long getTaskExecMaxThreadKeepLiveTime();

     void setTaskExecMaxThreadKeepLiveTime(long taskExecMaxThreadKeepLiveTime,TimeUnit unit) ;

        int getTaskExecWorkingMaxThreadSize();

    void setTaskExecWorkingMaxThreadSize(int taskExecWorkingMaxThreadSize);

    int getTaskExecWorkingWaitForSize();

    void setTaskExecWorkingWaitForSize(int taskExecWorkingWaitForSize);

    int getDeviationSizeCorrectTask();

    void setDeviationSizeCorrectTask(int deviationSizeCorrectTask);

    void stop();

    //准备好后开始,只启动一次，要保证线程安全
    void start();

    //注册一个通过的 任务回调函数
    void registConsumerTimerTask(C consumerTimerTask);

    /**
     * 提交任务
     * @param dataSource 回调时返回的原始数据
     * @param initialDelay 延时时间
     * @param unit  延时时间单位
     * @param taskId  任务id
     * @param <T>
     * @return
     */
    <T> boolean submitTask(T dataSource,
                           long initialDelay,
                           TimeUnit unit, String taskId);

    /***
     *  提交任务
     * @param dataSource
     * @param initialDelay
     * @param unit
     * @param <T>
     * @return
     */
    <T> boolean submitTask(T dataSource,
                           long initialDelay,
                           TimeUnit unit);

    /**
     * 提交任务
     * @param command
     * @param sourceData
     * @param initialDelay
     * @param unit
     * @param taskId
     * @param <T>
     * @return
     */
    <T> boolean submitTask(C command, T sourceData,
                           long initialDelay,
                           TimeUnit unit, String taskId);

    /**
     * 提交任务
     * @param timeTask
     * @param <T>
     * @return
     */
    <T> boolean submitTask(TimeTask<TimeTaskConsumer, T> timeTask);

    /**
     * 批量提交任务，一般是在系统 启动后，把以前未执行的任务再添加进来
     * @param timeTaskCollection
     * @param <T>
     */
    <T> void submitBatchTask(Collection<TimeTask<TimeTaskConsumer, T>> timeTaskCollection);
}
