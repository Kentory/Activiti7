package com.zjialin.workflow;

import org.activiti.engine.task.Task;

import java.io.Serializable;
import java.util.List;


public class TaskPageVO implements Serializable {

    private static final long serialVersionUID = -5052359301326472840L;

    private Long count;
    private List<Task> tasks;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
