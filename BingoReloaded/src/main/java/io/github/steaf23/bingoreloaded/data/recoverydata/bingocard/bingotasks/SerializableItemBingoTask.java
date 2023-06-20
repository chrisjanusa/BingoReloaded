package io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.bingotasks;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.tasks.ItemTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.ItemBingoTask;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@SerializableAs("ItemBingoTask")
public record SerializableItemBingoTask(
        UUID completedBy,
        int completedAt,
        boolean voided,
        ItemTask taskData

) implements ConfigurationSerializable, SerializableBingoTask {

    private static final String COMPLETED_BY_ID = "completed_by";

    private static final String COMPLETED_AT_ID = "completed_at";

    private static final String VOIDED_ID = "voided";

    private static final String DATA_ID = "task_data";

    public SerializableItemBingoTask(ItemBingoTask task) {
        this(
            task.completedBy.map(BingoParticipant::getId).orElse(null),
            (int) task.completedAt,
            task.isVoided(),
            task.data
        );
    }

    public static SerializableItemBingoTask deserialize(Map<String, Object> data)
    {
        String completedByString = (String)data.getOrDefault(COMPLETED_BY_ID, null);
        UUID completedBy = null;
        if (completedByString != null) {
            completedBy = UUID.fromString(completedByString);
        }
        return new SerializableItemBingoTask(
                completedBy,
                (Integer) data.getOrDefault(COMPLETED_AT_ID, -1),
                (Boolean) data.getOrDefault(VOIDED_ID, false),
                (ItemTask) data.getOrDefault(DATA_ID, null)
        );
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        if (completedBy != null) {
            data.put(COMPLETED_BY_ID, completedBy.toString());
        }
        data.put(COMPLETED_AT_ID, completedAt);
        data.put(VOIDED_ID, voided);
        data.put(DATA_ID, taskData);

        return data;
    }

    @Override
    public BingoTask<?> toBingoTask(BingoSession session) {
        BingoTask<?> task = BingoTask.getBingoTask(taskData);
        if (completedBy != null) {
            BingoParticipant completedParticipant = session.teamManager.getBingoParticipant(completedBy);
            if (completedParticipant != null) {
                task.completedBy = Optional.of(completedParticipant);
                task.completedAt = completedAt;
            }
        }
        task.setVoided(voided);
        return task;
    }
}
