package com.dewfn.timerwheel;

import java.util.concurrent.TimeUnit;

public interface TimeTask<C,D> {



    public C getTask() ;

    public void setTask(C task) ;

    public D getSourceData() ;

    public void setSourceData(D sourceData) ;

    public String getTaskId() ;

    public void setTaskId(String taskId);

    public long getPlanExecTime() ;

    /**
     * 计划执行时间，时间戳 单位毫秒 setDelayTime只用设置一个值即可
     * @param planExecTime
     */
    public void setPlanExecTime(long planExecTime) ;

    /**
     * 延时时间，N秒或N分分钟后执行  ，与setPlanExecTime只用设置一个值即可
     * @param planExecTime
     * @param unit
     */
    public void setDelayTime(long planExecTime, TimeUnit unit);
    public long getDelayTime();

    public long getActualExecTime() ;


    public default long getSpanExecTime() {
        return getActualExecTime() - getPlanExecTime();
    }

    public default int getSpanWheelNum(int surplusWheelNum, long timerFrequency) {
        return surplusWheelNum - (int) (Math.round((double) (getPlanExecTime() - System.currentTimeMillis()) / timerFrequency));
    }

    public void setActualExecTime(long actualExecTime) ;




    public int getZoomIndex() ;

    public default int reduceZoomIndex() {
        int z=getZoomIndex()-1;
        setZoomIndex(z);
        return z;
    }

    public void setZoomIndex(int zoomIndex) ;
}
