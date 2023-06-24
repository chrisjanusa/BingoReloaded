package io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.bingotasks;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.tasks.EveryoneTask;
import io.github.steaf23.bingoreloaded.tasks.LastToTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.ChildHavingBingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.EveryoneBingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.LastToBingoTask;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;


@SerializableAs("EveryoneBingoTask")
public record SerializableEveryoneBingoTask(
        UUID completedBy,
        int completedAt,
        boolean voided,
        EveryoneTask taskData,
        Map<String, Map<String, SerializableBingoTask>> childrenTasks
) implements ConfigurationSerializable, SerializableBingoTask {

    private static final String COMPLETED_BY_ID = "completed_by";

    private static final String COMPLETED_AT_ID = "completed_at";

    private static final String VOIDED_ID = "voided";

    private static final String DATA_ID = "task_data";
    private static final String CHILDREN_ID = "children";

    public SerializableEveryoneBingoTask(EveryoneBingoTask task) {
        this(
            task.completedBy.map(BingoParticipant::getId).orElse(null),
            (int) task.completedAt,
            task.isVoided(),
            task.data,
            getSerializableChildrenPerTeam(task.childrenPerTeam)
        );
    }

    private static Map<String, Map<String, SerializableBingoTask>> getSerializableChildrenPerTeam(Map<String, Map<String, BingoTask<?>>> childrenPerTeam) {
        Map<String, Map<String, SerializableBingoTask>> serializableChildrenPerTeam = new HashMap<>();
        for (String team : childrenPerTeam.keySet()) {
            Map<String, SerializableBingoTask> children = new HashMap<>();
            serializableChildrenPerTeam.put(team, children);
            for (String playerName : childrenPerTeam.get(team).keySet()) {
                children.put(playerName, SerializableBingoTask.toSerializedTask(childrenPerTeam.get(team).get(playerName)));
            }
        }
        return serializableChildrenPerTeam;
    }

    private static Map<String, Map<String, BingoTask<?>>> getUnserializedChildrenPerTeam(Map<String, Map<String, SerializableBingoTask>> serializableChildrenPerTeam, BingoSession session, ChildHavingBingoTask<?> parentTask) {
        Map<String, Map<String, BingoTask<?>>> childrenPerTeam = new HashMap<>();
        for (String team : serializableChildrenPerTeam.keySet()) {
            Map<String, BingoTask<?>> children = new HashMap<>();
            childrenPerTeam.put(team, children);
            for (String playerName : serializableChildrenPerTeam.get(team).keySet()) {
                children.put(playerName, serializableChildrenPerTeam.get(team).get(playerName).toBingoTask(session, parentTask));
            }
        }
        return childrenPerTeam;
    }

    public static SerializableEveryoneBingoTask deserialize(Map<String, Object> data)
    {
        String completedByString = (String)data.getOrDefault(COMPLETED_BY_ID, null);
        UUID completedBy = null;
        if (completedByString != null) {
            completedBy = UUID.fromString(completedByString);
        }
        return new SerializableEveryoneBingoTask(
                completedBy,
                (Integer) data.getOrDefault(COMPLETED_AT_ID, -1),
                (Boolean) data.getOrDefault(VOIDED_ID, false),
                (EveryoneTask) data.getOrDefault(DATA_ID, null),
                (Map<String, Map<String, SerializableBingoTask>>) data.getOrDefault(CHILDREN_ID, null)
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
        data.put(CHILDREN_ID, childrenTasks);

        return data;
    }

    @Override
    public BingoTask<?> toBingoTask(BingoSession session, ChildHavingBingoTask<?> parentTask) {
        EveryoneBingoTask task = new EveryoneBingoTask(taskData, parentTask, session.teamManager.getActiveTeams());
        if (completedBy != null) {
            BingoParticipant completedParticipant = session.teamManager.getBingoParticipant(completedBy);
            if (completedParticipant != null) {
                task.completedBy = Optional.of(completedParticipant);
                task.completedAt = completedAt;
            }
        }
        task.setVoided(voided);
        task.childrenPerTeam = getUnserializedChildrenPerTeam(childrenTasks, session, task);
        return task;
    }
}
