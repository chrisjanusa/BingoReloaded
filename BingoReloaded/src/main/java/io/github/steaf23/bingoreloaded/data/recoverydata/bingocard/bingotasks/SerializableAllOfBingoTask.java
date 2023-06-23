package io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.bingotasks;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.tasks.AllOfTask;
import io.github.steaf23.bingoreloaded.tasks.AnyOfTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.AllOfBingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.AnyOfBingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.ChildHavingBingoTask;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;


@SerializableAs("AllOfBingoTask")
public record SerializableAllOfBingoTask(
        UUID completedBy,
        int completedAt,
        boolean voided,
        AllOfTask taskData,
        Map<String, List<SerializableBingoTask>> childrenTasks
) implements ConfigurationSerializable, SerializableBingoTask {

    private static final String COMPLETED_BY_ID = "completed_by";

    private static final String COMPLETED_AT_ID = "completed_at";

    private static final String VOIDED_ID = "voided";

    private static final String DATA_ID = "task_data";
    private static final String CHILDREN_ID = "children";

    public SerializableAllOfBingoTask(AllOfBingoTask task) {
        this(
            task.completedBy.map(BingoParticipant::getId).orElse(null),
            (int) task.completedAt,
            task.isVoided(),
            task.data,
            getSerializableChildrenPerTeam(task.childrenPerTeam)
        );
    }

    private static Map<String, List<SerializableBingoTask>> getSerializableChildrenPerTeam(Map<String, List<BingoTask<?>>> childrenPerTeam) {
        Map<String, List<SerializableBingoTask>> serializableChildrenPerTeam = new HashMap<>();
        for (String team : childrenPerTeam.keySet()) {
            ArrayList<SerializableBingoTask> children = new ArrayList<>();
            serializableChildrenPerTeam.put(team, children);
            for (BingoTask<?> child : childrenPerTeam.get(team)) {
                children.add(SerializableBingoTask.toSerializedTask(child));
            }
        }
        return serializableChildrenPerTeam;
    }

    private static Map<String, List<BingoTask<?>>> getUnserializedChildrenPerTeam(Map<String, List<SerializableBingoTask>> serializableChildrenPerTeam, BingoSession session, ChildHavingBingoTask<?> parentTask) {
        Map<String, List<BingoTask<?>>> childrenPerTeam = new HashMap<>();
        for (String team : serializableChildrenPerTeam.keySet()) {
            ArrayList<BingoTask<?>> children = new ArrayList<>();
            childrenPerTeam.put(team, children);
            for (SerializableBingoTask child : serializableChildrenPerTeam.get(team)) {
                children.add(child.toBingoTask(session, parentTask));
            }
        }
        return childrenPerTeam;
    }

    public static SerializableAllOfBingoTask deserialize(Map<String, Object> data)
    {
        String completedByString = (String)data.getOrDefault(COMPLETED_BY_ID, null);
        UUID completedBy = null;
        if (completedByString != null) {
            completedBy = UUID.fromString(completedByString);
        }
        return new SerializableAllOfBingoTask(
                completedBy,
                (Integer) data.getOrDefault(COMPLETED_AT_ID, -1),
                (Boolean) data.getOrDefault(VOIDED_ID, false),
                (AllOfTask) data.getOrDefault(DATA_ID, null),
                (Map<String, List<SerializableBingoTask>>) data.getOrDefault(CHILDREN_ID, null)
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
        AllOfBingoTask task = new AllOfBingoTask(taskData, parentTask, session.teamManager.getActiveTeams());
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
