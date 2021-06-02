package com.dewfn.timerwheel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LinkedWheelPartition<C extends TimeTask,D> implements WheelPartition<C,D>{
    public void setPartition(int partition) {
        this.partition = partition;
    }

    public List<TimeTask<C,D>> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TimeTask<C,D>> taskList) {
        this.taskList = taskList;
    }

    private int partition;
    private List<TimeTask<C,D>> taskList;

    public LinkedWheelPartition() {
        taskList=new LinkedList<>();
    }


    public LinkedWheelPartition(int partition) {
        this();
        this.partition = partition;
    }

    public int getPartition() {
        return partition;
    }

    public Iterator<TimeTask<C,D>> getTaskListIterator(){
        return taskList.iterator();
    }
    public boolean addTask(TimeTask timeTask){
        return taskList.add(timeTask);
    }

    public void clearAllTask(){
        taskList.clear();
    }
}
