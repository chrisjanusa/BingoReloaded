package io.github.steaf23.bingoreloaded.player;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.tasks.BingoTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface BingoParticipant
{
    public BingoSession getSession();
    @Nullable
    public BingoTeam getTeam();
    public UUID getId();
    public Optional<Player> sessionPlayer();
    public String getDisplayName();
    public void showDeathMatchTask(BingoTask task);
    public boolean alwaysActive();
    public Location preTeleportLocation();
}
