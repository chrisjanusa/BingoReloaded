package io.github.steaf23.bingoreloaded.event;

import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;

public class ChildHavingTaskCompleteEvent extends BingoEvent
{
    BingoTask<?> task;
    final BingoParticipant participant;

    public ChildHavingTaskCompleteEvent(BingoParticipant participant, BingoTask<?> task)
    {
        super(participant.getSession());
        this.task = task;
        this.participant = participant;
    }

    public BingoTask<?> getTask() {
        return task;
    }

    public BingoParticipant getParticipant() {
        return participant;
    }
}
