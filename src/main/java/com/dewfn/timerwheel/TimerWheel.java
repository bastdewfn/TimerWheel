package com.dewfn.timerwheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TimerWheel implements ITimerWheel<TimeTaskConsumer> {
    private Logger log = LoggerFactory.getLogger(TimerWheel.class);
    private AtomicInteger taskCount = new AtomicInteger(0);
    //同时最多可执行多少任务
    private int maxTaskCount = Integer.MAX_VALUE;
    //时间轮转一圈多少刻度
    private int wheelSize = 60;
    //时间刻度
    private long timerFrequency = TimeUnit.SECONDS.toMillis(1); //毫秒单位，刻度值，  默认1秒
    private volatile int currentOffset = 0;
    private WheelPartition<TimeTaskConsumer, Object>[] wheelPartitions = new WheelPartition[wheelSize];
    private Thread timeThread;
    private final ReentrantLock lock = new ReentrantLock();


    @Override
    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    @Override
    public void setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
    }

    @Override
    public int getWheelSize() {
        return wheelSize;
    }

    @Override
    public void setWheelSize(int wheelSize) {
        this.wheelSize = wheelSize;
    }

    @Override
    public long getTimerFrequency() {
        return timerFrequency;
    }

    @Override
    public void setTimerFrequency(long timerFrequency) {
        this.timerFrequency = timerFrequency;
    }

    @Override
    public int getTaskExecCoreThreadSize() {
        return taskExecCoreThreadSize;
    }

    @Override
    public void setTaskExecCoreThreadSize(int taskExecCoreThreadSize) {
        this.taskExecCoreThreadSize = taskExecCoreThreadSize;
    }

    @Override
    public int getTaskExecMaxThreadSize() {
        return taskExecMaxThreadSize;
    }

    @Override
    public void setTaskExecMaxThreadSize(int taskExecMaxThreadSize) {
        this.taskExecMaxThreadSize = taskExecMaxThreadSize;
    }

    @Override
    public long getTaskExecMaxThreadKeepLiveTime() {
        return taskExecMaxThreadKeepLiveTime;
    }

    @Override
    public void setTaskExecMaxThreadKeepLiveTime(long taskExecMaxThreadKeepLiveTime,TimeUnit unit) {
        this.taskExecMaxThreadKeepLiveTime = unit.toMillis(taskExecMaxThreadKeepLiveTime);
    }

    @Override
    public int getTaskExecWorkingMaxThreadSize() {
        return taskExecWorkingMaxThreadSize;
    }

    @Override
    public void setTaskExecWorkingMaxThreadSize(int taskExecWorkingMaxThreadSize) {
        this.taskExecWorkingMaxThreadSize = taskExecWorkingMaxThreadSize;
    }

    @Override
    public int getTaskExecWorkingWaitForSize() {
        return taskExecWorkingWaitForSize;
    }

    @Override
    public void setTaskExecWorkingWaitForSize(int taskExecWorkingWaitForSize) {
        this.taskExecWorkingWaitForSize = taskExecWorkingWaitForSize;
    }

    @Override
    public int getDeviationSizeCorrectTask() {
        return deviationSizeCorrectTask;
    }

    @Override
    public void setDeviationSizeCorrectTask(int deviationSizeCorrectTask) {
        this.deviationSizeCorrectTask = deviationSizeCorrectTask;
    }

    //执行任务时的核心线程数，相当于消费数，过少会引起阻塞，如果执行的任务消耗时间过长可设置大点
    private int taskExecCoreThreadSize = 1;
    //执行任务时最大线程数，相当于最大消费数
    private int taskExecMaxThreadSize = 3;
    private long taskExecMaxThreadKeepLiveTime = TimeUnit.SECONDS.toMillis(60 * 1); //秒单位 线程，超过N秒后，最大线程会回收，只保留核心线程数
    private int taskExecWorkingMaxThreadSize = 10000000;// 线程运行中  应该执行还未执行的任务最大数，最大积压数
    private int taskExecWorkingWaitForSize = 10;// 线程运行中  应该执行还未执行的任务数，超过这积压数  就扩大线程
    private int deviationSizeCorrectTask = 5;  //时间轮自带修正任务偏移功能，如遇被分配，后期任务时间可能会不准，可以设置偏移超过N个刻度的任务


    ThreadPoolExecutor executorService;

    private long openExpansionLastTime = 0;
    private volatile long lastTurnTime = 0;
    private boolean isOpenExpansionLast = false;

    private volatile boolean isEnd = true;
    //相差N个刻度时 修正未执行任务的所在时间轮，0为不修正

    @Override
    public void stop() {
        try {
            lock.lock();

            if (!isEnd) {
                isEnd = true;
                timeThread = null;
                for (WheelPartition wheelPartition : wheelPartitions) {
                    wheelPartition.clearAllTask();
                }
                executorService.shutdownNow();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void start() {
        if(!isEnd){
            return;
        }
        isEnd = false;

        for (int i = 0; i < wheelSize; i++) {
            wheelPartitions[i] = new LinkedWheelPartition(i);
        }
        executorService = new ThreadPoolExecutor(taskExecCoreThreadSize, taskExecCoreThreadSize, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(taskExecWorkingMaxThreadSize), new TimerTaskExecThreadFactory("TimerTaskThread"), new ThreadPoolExecutor.AbortPolicy());

        timeThread = new Thread(() -> {
            while (!isEnd) {

                settingThreadSize();

                lock.lock();
                try {
                    WheelPartition<TimeTaskConsumer, Object> wheelPartition = wheelPartitions[currentOffset];

                    Iterator<TimeTask<TimeTaskConsumer, Object>> timeTaskIterator = wheelPartition.getTaskListIterator();
                    while (timeTaskIterator.hasNext()) {

                        TimeTask<TimeTaskConsumer, Object> timeTask = timeTaskIterator.next();

                        if (timeTask.getZoomIndex() == 0) {
                            timeTask.setActualExecTime(System.currentTimeMillis());
//                            if (timeTask.getSpanExecTime() > 500 || timeTask.getSpanExecTime() < -500) {
//                            log.warn("任务id:{},相差时间:{},{},******************", timeTask.getTaskId(), timeTask.getSpanExecTime(),timeTask.getSourceData());
//                            }
                            if (timeTask.getSpanExecTime() > 3000 || timeTask.getSpanExecTime() < -3000) {
                                log.warn("任务id:{},相差时间:{}毫秒", timeTask.getTaskId(), timeTask.getSpanExecTime());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("任务id:{},相差时间:{}毫秒，计划执行时间{},实际执行时间{}", timeTask.getTaskId(), timeTask.getSpanExecTime(), timeTask.getPlanExecTime(), timeTask.getActualExecTime());
                            }
                            executorService.execute(() -> {
                                timeTask.getTask().exec(timeTask.getSourceData());
                            });
                            timeTaskIterator.remove();
                            taskCount.decrementAndGet();
                        } else {
                            timeTask.reduceZoomIndex();
                            if (deviationSizeCorrectTask <= 0) {
                                continue;
                            }
                            //修整任务偏移，使任务准确
                            correctTaskWheel(timeTaskIterator, timeTask);
                        }
                    }

                    lastTurnTime = System.currentTimeMillis();
                    currentOffset++;
                    if (currentOffset >= wheelSize) {
                        currentOffset = 0;
                    }
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(timerFrequency);
                } catch (InterruptedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("等待被中断", e);
                    }
                }
            }
        });
        timeThread.setName("TimerThread");
        timeThread.start();
    }


    private void correctTaskWheel(Iterator<TimeTask<TimeTaskConsumer, Object>> timeTaskIterator, TimeTask timeTask) {
        int spanWheelNum = timeTask.getSpanWheelNum((timeTask.getZoomIndex() + 1) * wheelSize, timerFrequency);
        //如果
        if (spanWheelNum >= deviationSizeCorrectTask) {
//                           log.info("任务id:{},调整前 计划时间:{},当前时间:{},层数:{},当前轮:{}, 相差:{}",timeTask.getTaskId(),timeTask.getPlanExecTime(),System.currentTimeMillis(),timeTask.getZoomIndex(),currentOffset,spanWheelNum);
            //回调时间轮上所在区
            int newCurrentOffset = 0;
            int moveWheelIndex = currentOffset - spanWheelNum;

            if (moveWheelIndex >= 0) {
                wheelPartitions[moveWheelIndex].addTask(timeTask);
            } else {
                int moveZoomIndex = moveWheelIndex / wheelSize;
                int newOffset = moveWheelIndex % wheelSize;
                //没有可移动的，就下一个马上执行
                if (spanWheelNum > (timeTask.getZoomIndex() + 1) * wheelSize) {
                    timeTask.setZoomIndex(0);
                    if (currentOffset + 1 >= wheelSize) {
                        wheelPartitions[0].addTask(timeTask);
                        newCurrentOffset = 0;
                    } else {
                        newCurrentOffset = currentOffset + 1;
                        wheelPartitions[currentOffset + 1].addTask(timeTask);
                    }
                } else {
                    timeTask.setZoomIndex(timeTask.getZoomIndex() + moveZoomIndex);
                    if (newOffset == 0) {
                        newCurrentOffset = 0;
                        timeTask.setZoomIndex(timeTask.getZoomIndex() + 1);
                        wheelPartitions[newOffset].addTask(timeTask);
                    } else {
                        newCurrentOffset = wheelSize + newOffset;
                        wheelPartitions[wheelSize + newOffset].addTask(timeTask);
                    }
                }


            }
            timeTaskIterator.remove();
//                           log.info("任务id:{},调整后 计划时间:{},当前时间:{},层数:{},当前轮:{}, 相差:{}",timeTask.getTaskId(),timeTask.getPlanExecTime(),System.currentTimeMillis(),timeTask.getZoomIndex(),newCurrentOffset,((timeTask.getPlanExecTime()-System.currentTimeMillis())/1000-(timeTask.getZoomIndex()*wheelSize+newCurrentOffset-currentOffset)));


        }
    }

    private void settingThreadSize() {
        if (executorService.getQueue().size() > taskExecWorkingWaitForSize) {
            openExpansionLastTime = System.currentTimeMillis();
            if (!isOpenExpansionLast) {
                log.info("延时任务,触发扩容线程池,积压数:{}", executorService.getQueue().size());
                isOpenExpansionLast = true;
                executorService.setCorePoolSize(taskExecMaxThreadSize);
                executorService.setMaximumPoolSize(taskExecMaxThreadSize);
            }
        } else {
            if (isOpenExpansionLast) {
                if (System.currentTimeMillis() - openExpansionLastTime > taskExecMaxThreadKeepLiveTime) {
                    log.info("延时任务,回收扩容");
                    executorService.setCorePoolSize(taskExecCoreThreadSize);
                    executorService.setMaximumPoolSize(taskExecCoreThreadSize);
                    isOpenExpansionLast = false;
                }
            }
        }
    }


    @Override
    public void registConsumerTimerTask(TimeTaskConsumer consumerTimerTask) {
        this.consumerTimerTask = consumerTimerTask;
    }


    private TimeTaskConsumer consumerTimerTask;

    @Override
    public <T> boolean submitTask(T dataSource,
                                  long initialDelay,
                                  TimeUnit unit, String taskId) {
        return submitTask(consumerTimerTask, dataSource, initialDelay, unit, taskId);
    }

    @Override
    public <T> boolean submitTask(T dataSource,
                                  long initialDelay,
                                  TimeUnit unit) {

        return submitTask(dataSource, initialDelay, unit, null);
    }


    @Override
    public <T> void submitBatchTask(Collection<TimeTask<TimeTaskConsumer, T>> timeTaskCollection) {
        timeTaskCollection.parallelStream().forEach(task -> {
            submitTask(task);
        });
    }
    public <T> boolean submitTask(TimeTask<TimeTaskConsumer, T> timeTask) {
        Objects.requireNonNull(timeTask);
        if(timeTask.getTask()==null){
            timeTask.setTask(consumerTimerTask);
        }
        Objects.requireNonNull(timeTask.getTask(), "执行消息的任务方法不能为空,通过registConsumerTimerTask注册或调用时command参数添加");
        if (taskCount.get() > maxTaskCount) {
            log.error("添加任务超过最大数");
            return false;
        }

        long secondes = timeTask.getDelayTime() / timerFrequency;
        if (System.currentTimeMillis() - lastTurnTime < 500) {
            secondes--;
            if (secondes < 0)
                secondes = 0;
        }
        if (secondes == 0) {
            try {
                executorService.execute(() -> {
                    timeTask.getTask().exec(timeTask.getSourceData());
                });
                return true;
            } catch (Exception ex) {
                log.error("添加定时任务失败,{}", ex, timeTask.getTaskId());
                return false;
            }
        }
        boolean result = false;

        lock.lock();
        try {
            int wheelIndex = 0;
            if (secondes < wheelSize) {

            } else {
                int zoomIndex = (int) (secondes) / wheelSize;
                timeTask.setZoomIndex(zoomIndex);
            }

            wheelIndex = (int) (secondes + currentOffset) % wheelSize;
            if (log.isDebugEnabled()) {
                log.debug("添加任务id:{},放在扇区:{},第{}层,当前在第{}扇区", timeTask.getTaskId(), wheelIndex, timeTask.getZoomIndex(), currentOffset);
            }
            result = wheelPartitions[wheelIndex].addTask(timeTask);

        } finally {
            lock.unlock();
        }
        if (result)
            taskCount.incrementAndGet();
        return result;

    }
    @Override
    public <T> boolean submitTask(TimeTaskConsumer command, T sourceData,
                                  long initialDelay,
                                  TimeUnit unit, String taskId) {
        Objects.requireNonNull(command, "执行消息的任务方法不能为空,通过registConsumerTimerTask注册或调用时command参数添加");

        TimeTask<TimeTaskConsumer, T> timeTask = new ConsumerTimeTask(command, 0);
        timeTask.setTaskId(taskId);
        timeTask.setSourceData(sourceData);
        timeTask.setDelayTime(initialDelay,unit);
        timeTask.setPlanExecTime(System.currentTimeMillis() + unit.toMillis(initialDelay));
        return submitTask(timeTask);

    }


}
