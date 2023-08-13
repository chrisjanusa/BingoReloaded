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
        } else if (task instanceof EveryoneBingoTask everyoneBingoTask) {
            return new SerializableEveryoneBingoTask(everyoneBingoTask);
        } else if (task instanceof MostOfItemBingoTask mostOfItemBingoTask) {
            return new SerializableMostOfItemBingoTask(mostOfItemBingoTask);
        } else if (task instanceof MostOfStatisticBingoTask mostOfStatisticBingoTask) {
            return new SerializableMostOfStatisticBingoTask(mostOfStatisticBingoTask);
        } else if (task instanceof BreedBingoTask breedBingoTask) {
            return new SerializableBreedBingoTask(breedBingoTask);
        } else if (task instanceof DeathMessageBingoTask deathMessageBingoTask) {
            return new SerializableDeathMessageBingoTask(deathMessageBingoTask);
        } else if (task instanceof LevelBingoTask levelBingoTask) {
            return new SerializableLevelBingoTask(levelBingoTask);
        } else if (task instanceof AnyAdvancementsBingoTask anyAdvancementsBingoTask) {
            return new SerializableAnyAdvancementsBingoTask(anyAdvancementsBingoTask);
        }
        return null;
    }
}
