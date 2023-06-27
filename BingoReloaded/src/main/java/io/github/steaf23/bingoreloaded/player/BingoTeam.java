package io.github.steaf23.bingoreloaded.player;

import io.github.steaf23.bingoreloaded.cards.BingoCard;

import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.util.FlexColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class BingoTeam
{
    // Team used to display prefixes next to player display names
    public final Team team;
    public BingoCard card;
    public boolean outOfTheGame = false;
    private final String id;
    private final ChatColor color;
    private final String name;

    private Set<BingoParticipant> members;
    public Map<String, Location> savedLocations;

    public BingoTeam(Team team, BingoCard card, ChatColor color, String id, String name)
    {
        this.id = id;
        this.team = team;
        this.card = card;
        this.color = color;
        this.name = name;
        this.members = new HashSet<>();
        this.savedLocations = new HashMap<>();
    }

    public String getIdentifier()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return id;
    }

    public ChatColor getColor()
    {
        return color;
    }

    public ItemText getColoredName()
    {
        return new ItemText(name, color, ChatColor.BOLD);
    }

    public Set<BingoParticipant> getMembers()
    {
        return members;
    }

    public void addMember(BingoParticipant player)
    {
        members.add(player);
        team.addEntry(player.getDisplayName());
    }

    public void removeMember(BingoParticipant player)
    {
        members.remove(player);
        team.removeEntry(player.getDisplayName());
    }

    public void saveLocation(String name, Location location) {
        savedLocations.put(name, location);
    }

    public Location getSavedLocation(String name) {
        return savedLocations.get(name);
    }

    public List<String> getSavedLocationNames() {
        return new ArrayList<>(savedLocations.keySet());
    }

    public void clearSavedLocations() {
        savedLocations.clear();
    }
}