package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.tasks.TaskData;

import java.util.List;
import java.util.Map;

public abstract class ChildHavingBingoTask<T extends TaskData> extends BingoTask<T>
{
    public Map<String, List<BingoTask<?>>> childrenPerTeam;

    abstract void onChildComplete(BingoParticipant participant, long gameTime);
}
