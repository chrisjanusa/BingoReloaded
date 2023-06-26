package io.github.steaf23.bingoreloaded.tasks;

import java.util.List;

public interface ChildHavingTask extends TaskData
{
    public List<TaskData> getChildren();
}
