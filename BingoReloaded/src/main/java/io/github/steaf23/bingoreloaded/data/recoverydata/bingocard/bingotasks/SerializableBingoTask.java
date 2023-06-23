package io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.bingotasks;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.*;

public interface SerializableBingoTask {
    BingoTask<?> toBingoTask(BingoSession session, ChildHavingBingoTask<?> parentTask);

    static SerializableBingoTask toSerializedTask(BingoTask<?> task) {
        if (task instanceof StatisticBingoTask statisticBingoTask) {
            return new SerializableStatisticBingoTask(statisticBingoTask);
        } else if (task instanceof AdvancementBingoTask advancementBingoTask) {
            return new SerializableAdvancementBingoTask(advancementBingoTask);
        } else if (task instanceof ItemBingoTask itemBingoTask) {
            return new SerializableItemBingoTask(itemBingoTask);
        } else if (task instanceof AnyOfBingoTask anyOfBingoTask) {
            return new SerializableAnyOfBingoTask(anyOfBingoTask);
        } else if (task instanceof AllOfBingoTask allOfBingoTask) {
            return new SerializableAllOfBingoTask(allOfBingoTask);
        } else if (task instanceof LastToBingoTask lastToBingoTask) {
            return new SerializableLastToBingoTask(lastToBingoTask);
        }
        return null;
    }
}
