package io.github.steaf23.bingoreloaded.player;

import io.github.steaf23.bingoreloaded.cards.BingoCard;
import io.github.steaf23.bingoreloaded.cards.LockoutBingoCard;
import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.data.TeamData;
import io.github.steaf23.bingoreloaded.event.*;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.gui.base.FilterType;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.gui.base.PaginatedSelectionMenu;
import io.github.steaf23.bingoreloaded.settings.PlayerKit;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.util.Message;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.*;
public class TeamManager
{
    private final BingoSession session;
    private final Set<BingoTeam> activeTeams;
    private final Scoreboard teams;
    private final TeamData teamData;

    // Contains all players that will join a team automatically when the game starts
    private final Set<UUID> automaticTeamPlayers;
    private final Map<UUID, String> autoVirtualPlayers;
    private int maxTeamSize;

    public TeamManager(Scoreboard teamBoard, BingoSession session) {
        this.session = session;
        this.activeTeams = new HashSet<>();
        this.teams = teamBoard;
        this.teamData = new TeamData();
        this.maxTeamSize = session.settingsBuilder.view().maxTeamSize();
        this.automaticTeamPlayers = new HashSet<>();
        this.autoVirtualPlayers = new HashMap<>();
        createTeams();
    }

    @Nullable
    public BingoParticipant getBingoParticipant(@NonNull UUID participantId) {
        for (BingoParticipant participant : getParticipants()) {
            if (participant.getId().equals(participantId)) {
                return participant;
            }
        }
        return null;
    }

    @Nullable
    public VirtualBingoPlayer getVirtualPlayerFromName(String playerName) {
        for (BingoParticipant participant : getParticipants()) {
            if (!(participant instanceof VirtualBingoPlayer virtualPlayer)) {
                continue;
            }

            if (virtualPlayer.getName().equals(playerName)) {
                return virtualPlayer;
            }
        }
        return null;
    }

    @Nullable
    public BingoParticipant getBingoParticipant(@NonNull Player player) {
        return getBingoParticipant(player.getUniqueId());
    }

    public void openTeamSelector(MenuManager menuManager, Player player) {
        List<MenuItem> optionItems = new ArrayList<>();
        optionItems.add(new MenuItem(Material.NETHER_STAR, "" + ChatColor.BOLD + ChatColor.ITALIC + BingoTranslation.TEAM_AUTO.translate())
                .setCompareKey("item_auto"));
        optionItems.add(new MenuItem(Material.TNT, "" + ChatColor.BOLD + ChatColor.ITALIC + BingoTranslation.OPTIONS_LEAVE.translate())
                .setGlowing(true).setCompareKey("item_leave"));

        var allTeams = teamData.getTeams();
        for (String teamId : allTeams.keySet()) {
            boolean playersTeam = false;
            TeamData.TeamTemplate teamTemplate = allTeams.get(teamId);

            boolean teamIsFull = false;
            List<String> description = new ArrayList<>();
            for (BingoTeam team : activeTeams) {
                if (!team.getIdentifier().equals(teamId))
                    continue;

                for (BingoParticipant participant : team.getMembers()) {
                    description.add("" + ChatColor.GRAY + ChatColor.BOLD + " ┗ " + ChatColor.RESET + ChatColor.WHITE + participant.getDisplayName());
                    if (participant.getId().equals(player.getUniqueId()))
                    {
                        playersTeam = true;
                    }
                }

                if (maxTeamSize == team.getMembers().size()) {
                    teamIsFull = true;
                }
            }

            description.add(" ");
            if (teamIsFull) {
                description.add(ChatColor.RED + BingoTranslation.FULL_TEAM_DESC.translate());
            } else {
                description.add(ChatColor.GREEN + BingoTranslation.JOIN_TEAM_DESC.translate());
            }

            optionItems.add(MenuItem.createColoredLeather(teamTemplate.color(), Material.LEATHER_CHESTPLATE)
                    .setName("" + teamTemplate.color() + ChatColor.BOLD + teamTemplate.name())
                    .setDescription(description.toArray(new String[]{})).setCompareKey(teamId)
                    .setGlowing(playersTeam));
        }

        PaginatedSelectionMenu teamPicker = new PaginatedSelectionMenu(menuManager, BingoTranslation.OPTIONS_TEAM.translate(), optionItems, FilterType.DISPLAY_NAME)
        {
            @Override
            public void onOptionClickedDelegate(InventoryClickEvent event, MenuItem clickedOption, HumanEntity player) {
                if (clickedOption.getCompareKey().equals("item_auto")) {
                    addPlayerToAutoTeam((Player) player);
                    openTeamSelector(menuManager, (Player) player);
                    return;
                }
                else if (clickedOption.getCompareKey().equals("item_leave")) {
                    removeMemberFromTeam((Player) player);
                    openTeamSelector(menuManager, (Player) player);
                    return;
                }

                addPlayerToTeam((Player) player, clickedOption.getCompareKey());
                openTeamSelector(menuManager, (Player) player);
            }

            @Override
            public boolean openOnce() {
                return true;
            }
        };
        teamPicker.open(player);
    }

    public boolean addPlayerToTeam(@NonNull Player player, String teamName) {
        //TODO: maybe combine both functions into 1?
        if (teamName.equals("auto"))
            return addPlayerToAutoTeam(player);

        automaticTeamPlayers.remove(player.getUniqueId());

        BingoTeam bingoTeam = activateTeamFromName(teamName);

        if (bingoTeam.hasPlayer(player)) {
            return false;
        }
        if (bingoTeam == null) {
            return false;
        }
        if (bingoTeam.getMembers().size() == maxTeamSize) {
            return false;
        }

        if (getBingoParticipant(player) != null)
            removeMemberFromTeam(getBingoParticipant(player));

        new TranslatedMessage(BingoTranslation.JOIN).color(ChatColor.GREEN)
                .arg(bingoTeam.getColoredName().asLegacyString())
                .send(player);

        BingoPlayer bingoPlayer = new BingoPlayer(player, bingoTeam, session);
        bingoTeam.addMember(bingoPlayer);

        var joinEvent = new ParticipantJoinedTeamEvent(bingoPlayer, bingoTeam, session);
        Bukkit.getPluginManager().callEvent(joinEvent);
        return true;
    }

    private boolean addPlayerToAutoTeam(Player player) {
        BingoParticipant participant = getBingoParticipant(player);
        if (participant != null)
            removeMemberFromTeam(participant);


        if (automaticTeamPlayers.contains(player.getUniqueId())) {
            return true;
        }

        automaticTeamPlayers.add(player.getUniqueId());
        new TranslatedMessage(BingoTranslation.JOIN_AUTO).color(ChatColor.GREEN).send(player);

        var joinEvent = new ParticipantJoinedTeamEvent(participant, session);
        Bukkit.getPluginManager().callEvent(joinEvent);
        return true;
    }

    public void addAutoPlayersToTeams() {
        record TeamCount(BingoTeam team, int count) {}

        int totalPlayers = teams.getTeams().size() * maxTeamSize;
        int availablePlayers = totalPlayers - getTotalParticipantCount();
        if (automaticTeamPlayers.size() > availablePlayers) {
            Message.error("Could not fit every player into a team, consider changing the team size or adding more teams");
            return;
        }

        // 1. create list sorted by how many players are missing from each team using a bit of insertion sorting...
        List<TeamCount> counts = new ArrayList<>();
        for (BingoTeam team : activeTeams) {
            TeamCount newCount = new TeamCount(team, team.getMembers().size());
            if (counts.size() == 0) {
                counts.add(newCount);
                continue;
            }
            int idx = 0;
            for (TeamCount tCount : counts) {
                if (newCount.count <= tCount.count) {
                    counts.add(idx, newCount);
                    break;
                }
                idx++;
            }
            if (idx >= counts.size()) {
                counts.add(newCount);
            }
        }

        // 2. fill this list 1 by 1 using players from the list of queued players.
        // To actually implement this, we need to take the team with the least amount of players,
        //      add a player to it, and then insert it back into the list.
        //      when the team with the least amount of players has the same amount as the biggest team, all teams have been filled.

        HashSet<UUID> autoPlayersCopy = new HashSet<>(automaticTeamPlayers);
        // Since we need to remove players from this list as we are iterating, use a direct reference to the iterator.
        for (UUID playerId : autoPlayersCopy) {
            TeamCount lowest = counts.size() > 0 ? counts.get(0) : null;
            // If our lowest count is the same as the highest count, all incomplete teams have been filled
            if (counts.size() == 0 || lowest.count == maxTeamSize) {
                // If there are still players left in the queue, create a new team
                if (automaticTeamPlayers.size() > 0) {
                    BingoTeam newTeam = activateAnyTeam();
                    if (newTeam == null) {
                        Message.error("Could not fit every player into a team, since there is not enough room!");
                        break;
                    }
                    counts.add(0, new TeamCount(newTeam, 0));
                    lowest = counts.get(0);
                }
            }

            counts.remove(0);
            // After this point in the iteration, lowest will reference the team that will get inserted into counts at the end of the iteration.

            // Create a Substitute player when the uuid is invalid for some reason.
            boolean ok = false;
            if (autoVirtualPlayers.containsKey(playerId)) {
                ok = addVirtualPlayerToTeam(autoVirtualPlayers.get(playerId), lowest.team.getIdentifier());
                autoVirtualPlayers.remove(playerId);
            } else if (Bukkit.getPlayer(playerId) != null) {
                Player player = Bukkit.getPlayer(playerId);
                ok = addPlayerToTeam(player, lowest.team.getIdentifier());
            }
            if (ok) {
                lowest = new TeamCount(lowest.team, lowest.count + 1);
                automaticTeamPlayers.remove(playerId);
            }

            // Insert the lowest back into the team counts
            int idx = 0;
            for (TeamCount tCount : counts) {
                if (lowest.count <= tCount.count) {
                    counts.add(idx, lowest);
                    break;
                }
                idx++;
            }
            if (idx >= counts.size()) {
                counts.add(lowest);
            }
        }
        automaticTeamPlayers.clear();
        autoVirtualPlayers.clear();
    }

    private boolean addVirtualPlayerToAutoTeam(String playerName) {
        BingoParticipant participant = getVirtualPlayerFromName(playerName); // This will exclude automatic team players!
        for (UUID id : autoVirtualPlayers.keySet()) {
            if (autoVirtualPlayers.get(id).equals(playerName)) {
                autoVirtualPlayers.remove(id);
            }
        }
        if (participant != null)
            removeMemberFromTeam(participant);

        // This is a little disgusting since we are only creating this local virtual player to look just recreate it later anyway.
        //      At that point the actual virtual player will be created using a new random UUID.
        VirtualBingoPlayer player = new VirtualBingoPlayer(UUID.randomUUID(), playerName, null, session);
        autoVirtualPlayers.put(player.getId(), player.getName());
        automaticTeamPlayers.add(player.getId());

        var joinEvent = new ParticipantJoinedTeamEvent(player, session);
        Bukkit.getPluginManager().callEvent(joinEvent);
        return true;
    }

    public boolean addVirtualPlayerToTeam(String playerName, String teamName) {
        if (teamName.equals("auto"))
            return addVirtualPlayerToAutoTeam(playerName);

        BingoTeam bingoTeam = activateTeamFromName(teamName);

        if (bingoTeam == null) {
            return false;
        }
        if (bingoTeam.getMembers().size() == maxTeamSize) {
            Message.error("Team " + bingoTeam.getIdentifier() + " has reached it's capacity of " + maxTeamSize + " players!");
            return false;
        }


        VirtualBingoPlayer existingPlayer = getVirtualPlayerFromName(playerName);
        if (existingPlayer != null)
            removeMemberFromTeam(existingPlayer);

        VirtualBingoPlayer bingoPlayer = new VirtualBingoPlayer(UUID.randomUUID(), playerName, bingoTeam, session);
        bingoTeam.addMember(bingoPlayer);

        var joinEvent = new ParticipantJoinedTeamEvent(bingoPlayer, bingoTeam, session);
        Bukkit.getPluginManager().callEvent(joinEvent);
        return true;
    }

    public void removeMemberFromTeam(Player player) {
        // TODO: this does not work for virtual players!
        // Make sure to call event even if player was part of auto team
        if (automaticTeamPlayers.contains(player.getUniqueId()))
        {
            automaticTeamPlayers.remove(player.getUniqueId());
            autoVirtualPlayers.remove(player.getUniqueId());

            var leaveEvent = new ParticipantLeftTeamEvent(null, session);
            Bukkit.getPluginManager().callEvent(leaveEvent);
            return;
        }

        BingoParticipant participant = getBingoParticipant(player);
        if (participant != null) {
            removeMemberFromTeam(getBingoParticipant(player));
        } else {
            return;
        }
    }

    public boolean removeMemberFromTeam(BingoParticipant player) {
        automaticTeamPlayers.remove(player.getId());
        autoVirtualPlayers.remove(player.getId());

        if (getParticipants().contains(player)) {
            player.getTeam().removeMember(player);
        }
        else {
            return false;
        }

        var leaveEvent = new ParticipantLeftTeamEvent(player, player.getTeam(), session);
        Bukkit.getPluginManager().callEvent(leaveEvent);
        return true;
    }

    public void removeEmptyTeams() {
        activeTeams.removeIf(team -> team.getMembers().size() == 0);
    }

    public void initializeCards(BingoCard masterCard) {
        if (masterCard instanceof LockoutBingoCard lockoutCard) {
            lockoutCard.teamCount = activeTeams.size();
        }
        activeTeams.forEach((t) -> {
            t.outOfTheGame = false;
            t.card = masterCard.copy();
        });

    }

    public Set<BingoTeam> getActiveTeams() {
        return activeTeams;
    }


    /**
     * Gets all BingoPlayers regardless if they are currently in the world.
     *
     * @return Set of BingoPlayers that have joined a team.
     */
    public Set<BingoParticipant> getParticipants() {
        Set<BingoParticipant> players = new HashSet<>();
        for (BingoTeam activeTeam : activeTeams) {
            players.addAll(activeTeam.getMembers());
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            boolean found = false;
            for (BingoTeam t : activeTeams) {
                for (var player : t.getMembers()) {
                    if (player.getId().equals(p.getUniqueId())) {
                        players.add(player);
                        found = true;
                    }
                }
                if (found)
                    break;
            }
        }
        return players;
    }

    public void updateActivePlayers() {
        for (BingoTeam team : activeTeams) {
            for (BingoParticipant participant : team.getMembers()) {
                if (participant.sessionPlayer().isEmpty() && !participant.alwaysActive()) {
                    removeMemberFromTeam(participant);
                }
            }
        }

        removeEmptyTeams();
    }

    public Set<BingoParticipant> getParticipantsOfTeam(BingoTeam team) {
        return team.getMembers();
    }

    public BingoTeam getLeadingTeam() {
        Optional<BingoTeam> leadingTeam = activeTeams.stream().max(
                Comparator.comparingInt(t -> t.card.getCompleteCount(t))
        );
        return leadingTeam.orElse(null);
    }

    public BingoTeam getLosingTeam() {
        Optional<BingoTeam> losingTeam = activeTeams.stream().min(
                Comparator.comparingInt(t -> t.card.getCompleteCount(t))
        );
        return losingTeam.orElse(null);
    }

    public BingoTeam activateTeam(Team team) {
        BingoTeam bTeam;
        bTeam = activeTeams.stream().filter(
                        (t) -> t.team.getName().equals(team.getName()))
                .findFirst().orElse(null);

        if (bTeam == null) {
            TeamData.TeamTemplate template = teamData.getTeam(team.getName());
            bTeam = new BingoTeam(team, null, template.color(), team.getName(), template.name());

            activeTeams.add(bTeam);
        }
        return bTeam;
    }

    public int getCompleteCount(BingoTeam team) {
        return team.card.getCompleteCount(team);
    }

    @Nullable
    public BingoTeam activateTeamFromName(String teamName) {
        Team team = teams.getTeam(teamName);
        if (team == null) {
            return null;
        }

        if (session.isRunning() && activeTeams.stream().noneMatch(t -> t.getIdentifier().equals(teamName))) {
            return null;
        }

        return activateTeam(team);
    }

    private void createTeams() {
        var savedTeams = teamData.getTeams();
        for (String team : savedTeams.keySet()) {
            TeamData.TeamTemplate template = savedTeams.get(team);

            String name = team;
            Team t = teams.registerNewTeam(name);
            String prefix = "" + ChatColor.DARK_RED + "[" + template.color() + ChatColor.BOLD + template.name() + ChatColor.DARK_RED + "] ";
            t.setPrefix(prefix);
            // Add dummy entry to show the prefix on the board
            t.addEntry("" + team);
        }
        Message.log("Successfully created 16 teams");
    }

    /**
     * @return null if all teams are already active, else the team that was just activated
     */
    private BingoTeam activateAnyTeam() {
        for (String teamId : teamData.getTeams().keySet()) {
            boolean active = false;
            for (BingoTeam team : activeTeams) {
                if (team.getIdentifier().equals(teamId)) {
                    active = true;
                    break;
                }
            }

            if (!active) {
                return activateTeamFromName(teamId);
            }
        }

        return null;
    }

    /**
     * @return the total amount of players spread across all active teams, including players in the automatic team
     */
    public int getTotalParticipantCount() {
        return getParticipants().size() + automaticTeamPlayers.size() + autoVirtualPlayers.size();
    }

    public BingoSession getSession() {
        return session;
    }

    //== EventHandlers ==========================================
    public void handleSettingsUpdated(BingoSettingsUpdatedEvent event) {
        int newTeamSize = event.getNewSettings().maxTeamSize();
        if (newTeamSize == maxTeamSize)
            return;

        this.maxTeamSize = newTeamSize;
        if (!session.isRunning()) {
            getParticipants().forEach(p -> {
                removeMemberFromTeam(p);
                p.sessionPlayer().ifPresent(gamePlayer ->
                        new TranslatedMessage(BingoTranslation.TEAM_SIZE_CHANGED)
                        .color(ChatColor.RED)
                        .send(gamePlayer));
            });
        }
    }

    public void handlePlayerLeftSessionWorld(final PlayerLeftSessionWorldEvent event) {
        BingoParticipant participant = getBingoParticipant(event.getPlayer());
        if (participant == null)
            return;

        participant.getTeam().team.removeEntry(event.getPlayer().getName());
    }

    public void handlePlayerJoinedSessionWorld(final PlayerJoinedSessionWorldEvent event) {
        BingoParticipant participant = getBingoParticipant(event.getPlayer());
        if (participant == null) {
            if (!session.isRunning()) {
                addPlayerToAutoTeam(event.getPlayer());
            }
            return;
        }

        participant.getTeam().team.addEntry(event.getPlayer().getName());
    }

    /**
     * @param event
     * @param deathMatchTask the card item will show this task instead of the card if it is not null
     */
    public void handlePlayerShowCard(final PlayerInteractEvent event, @Nullable BingoTask<?> deathMatchTask) {
        BingoParticipant participant = getBingoParticipant(event.getPlayer());
        if (participant == null)
            return;
        if (PlayerKit.CARD_ITEM.isCompareKeyEqual(event.getItem())) {
            event.setCancelled(true);
            BingoTeam playerTeam = participant.getTeam();
            if (playerTeam == null) {
                return;
            }
            BingoCard card = playerTeam.card;

            // if the player is actually participating, show it
            if (card != null) {
                if (deathMatchTask != null) {
                    participant.showDeathMatchTask(deathMatchTask);
                    return;
                }
                card.showInventory(event.getPlayer());
            } else {
                new TranslatedMessage(BingoTranslation.NO_PLAYER_CARD).send(event.getPlayer());
            }
        }
    }
}
