package de.t0bx.eindino.listener.lobby;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.vote.VotingHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

public class LobbyInventoryClickListener implements Listener {

    private final String prefix;
    private final MiniMessage mm;
    private final TeamHandler teamHandler;
    private final VotingHandler votingHandler;
    private final NamespacedKey teamKey;
    private final NamespacedKey voteKey;

    public LobbyInventoryClickListener() {
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.mm = MiniMessage.miniMessage();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.votingHandler = BedWarsPlugin.getInstance().getVotingHandler();
        this.teamKey = new NamespacedKey("bedwars", "teams");
        this.voteKey = new NamespacedKey("bedwars", "votes");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) return;

        if (event.getCurrentItem().getItemMeta() == null) return;

        if (event.getView().title().equals(this.mm.deserialize("<gray>» <red>Teamauswahl <gray>«"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getPersistentDataContainer().has(this.teamKey)) {
                String teamName = event.getCurrentItem().getPersistentDataContainer().get(this.teamKey, PersistentDataType.STRING);
                if (this.teamHandler.getTeam(teamName) == null) return;
                TeamData team = this.teamHandler.getTeam(teamName);
                if (team.isFull()) {
                    event.getView().close();
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Dieses Team ist bereits voll."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    return;
                }

                if (this.teamHandler.getPlayerTeam(player) != null) {
                    if (this.teamHandler.getPlayerTeam(player).getName().equalsIgnoreCase(teamName)) {
                        event.getView().close();
                        player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du bist bereits in diesem Team."));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        return;
                    }

                    this.teamHandler.removePlayerFromTeam(player);
                }

                event.getView().close();
                this.teamHandler.addPlayerToTeam(teamName, player);
                player.sendMessage(this.mm.deserialize(this.prefix + "Du bist nun im Team " + this.mm.serialize(team.getDisplayName()) + "<gray>."));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                this.teamHandler.updateScoreboardForAllPlayers();
            }
        }

        if (event.getView().title().equals(this.mm.deserialize("<gray>» <green>Voting <gray>«"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.GOLD_INGOT) {
                BedWarsPlugin.getInstance().getMapVotingInventory().openGoldVoting(player);
            }

            if (event.getCurrentItem().getType() == Material.PAPER) {
                BedWarsPlugin.getInstance().getMapVotingInventory().openMapVoting(player);
            }
        }

        if (event.getView().title().equals(this.mm.deserialize("<gray>» <green>Map-Voting <gray>«"))) {
            event.setCancelled(true);
            if (event.getCurrentItem().getPersistentDataContainer().has(this.voteKey)) {
                String mapName = event.getCurrentItem().getPersistentDataContainer().get(this.voteKey, PersistentDataType.STRING);
                if (this.votingHandler.hasVotedForMap(player, mapName)) {
                    event.getView().close();
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du stimmst bereits für diese Map."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    return;
                }

                if (this.votingHandler.hasVoted(player)) {
                    this.votingHandler.removeVote(player);
                }

                event.getView().close();
                this.votingHandler.addVote(player, mapName);
                player.sendMessage(this.mm.deserialize(this.prefix + "Du stimmst nun für die Map " + mapName + "."));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }

        if (event.getView().title().equals(this.mm.deserialize("<gray>» <green>Gold-Voting <gray>«"))) {
            if (event.getCurrentItem().getType() == Material.EMERALD) {
                if (this.votingHandler.getVotedForGold(player)) {
                    event.getView().close();
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du stimmst bereits für Gold in dieser Runde."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    return;
                }

                if (this.votingHandler.hasVotedForGold(player)) {
                    this.votingHandler.removeGoldVote(player);
                }

                event.getView().close();
                this.votingHandler.addGoldVote(player, true);
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du stimmst nun für Gold in dieser Runde."));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }

            if (event.getCurrentItem().getType() == Material.REDSTONE) {
                if (this.votingHandler.hasVotedForGold(player)) {
                    if (!this.votingHandler.getVotedForGold(player)) {
                        event.getView().close();
                        player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du stimmst bereits gegen Gold in dieser Runde."));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        return;
                    }
                }

                if (this.votingHandler.hasVotedForGold(player)) {
                    this.votingHandler.removeGoldVote(player);
                }

                event.getView().close();
                this.votingHandler.addGoldVote(player, false);
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du stimmst nun gegen Gold in dieser Runde."));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }
}
