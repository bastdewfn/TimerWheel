package com.dewfn.timerwheel;

import java.util.Iterator;
import java.util.List;

public interface WheelPartition<C,D> {

    public void setPartition(int partition) ;

    public List<TimeTask<C,D>> getTaskList();
    public void setTaskList(List<TimeTask<C, D>> taskList) ;

    public int getPartition();

    public Iterator<TimeTask<C,D>> getTaskListIterator();
    public boolean addTask(TimeTask timeTask);

    public void clearAllTask();
}
