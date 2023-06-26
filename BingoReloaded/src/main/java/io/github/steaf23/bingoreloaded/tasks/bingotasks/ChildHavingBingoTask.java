package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.TaskData;

import java.util.List;
import java.util.Map;

public abstract class ChildHavingBingoTask<T extends TaskData> extends BingoTask<T>
{
    abstract public List<BingoTask<?>> getChildTasksForPlayer(BingoParticipant participant);

    abstract void onChildComplete(BingoParticipant participant, long gameTime);
}
