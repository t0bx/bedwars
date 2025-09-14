package de.t0bx.eindino.vote;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.map.MapHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class VotingHandler {
    private final MapHandler mapHandler;
    private final ConcurrentHashMap<Player, String> mapVotes;
    private final ConcurrentHashMap<Player, Boolean> goldVotes;

    @Getter
    @Setter
    private boolean isForceMap;

    @Getter
    @Setter
    private String forceMapName;

    public VotingHandler() {
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
        this.mapVotes = new ConcurrentHashMap<>();
        this.goldVotes = new ConcurrentHashMap<>();
        this.isForceMap = false;
        this.forceMapName = null;
    }

    public void forceMap(String forceMapName) {
        this.setForceMapName(forceMapName);
        if (!this.isForceMap) {
            this.setForceMap(true);
        }
    }

    public void addVote(Player player, String mapName) {
        if (this.mapHandler.getMap(mapName) != null) {
            if (!this.mapVotes.containsKey(player)) {
                this.mapVotes.put(player, mapName);
            }
        }
    }

    public void addGoldVote(Player player, boolean gold) {
        if (!this.goldVotes.containsKey(player)) {
            this.goldVotes.put(player, gold);
        }
    }

    public void removeVote(Player player) {
        this.mapVotes.remove(player);
    }

    public void removeGoldVote(Player player) {
        this.goldVotes.remove(player);
    }

    public boolean hasVotedForGold(Player player) {
        return this.goldVotes.containsKey(player);
    }

    public boolean getVotedForGold(Player player) {
        return this.goldVotes.getOrDefault(player, false);
    }

    public int getVotesForGold(boolean isGoldActive) {
        int votes = 0;
        for (Map.Entry<Player, Boolean> entry : this.goldVotes.entrySet()) {
            if (entry.getValue() == isGoldActive) {
                votes++;
            }
        }
        return votes;
    }

    public void clearAllVotes() {
        this.mapVotes.clear();
    }

    public boolean hasVoted(Player player) {
        return this.mapVotes.containsKey(player);
    }

    public boolean hasVotedForMap(Player player, String mapName) {
        for (Map.Entry<Player, String> entry : this.mapVotes.entrySet()) {
            if (entry.getKey().equals(player) && entry.getValue().equals(mapName)) {
                return true;
            }
        }
        return false;
    }

    public boolean getVotedGold() {
        if (this.goldVotes.isEmpty()) {
            return false;
        }

        int trueVotes = 0;
        int falseVotes = 0;

        for (boolean vote : this.goldVotes.values()) {
            if (vote) {
                trueVotes++;
            } else {
                falseVotes++;
            }
        }

        return trueVotes >= falseVotes;
    }

    public int getVotesFromMap(String mapName) {
        int votes = 0;
        if (this.mapHandler.getMap(mapName) != null) {
            for (Map.Entry<Player, String> entry : this.mapVotes.entrySet()) {
                if (entry.getValue().equals(mapName)) {
                    votes++;
                }
            }
        }
        return votes;
    }

    public String getVotedMap() {
        if (this.mapHandler.getMapNames().isEmpty()) {
            return "NO MAP";
        } else {
            if (!this.isForceMap) {
                if (this.mapVotes.isEmpty()) {
                    if (this.mapHandler.getMapNames().size() == 1) {
                        return this.mapHandler.getMapNames().getFirst();
                    }

                    int randomNumber = new Random().nextInt(0, this.mapHandler.getMapNames().size());
                    return this.mapHandler.getMapNames().get(randomNumber);
                } else {
                    Map<String, Integer> voteCount = new HashMap<>();

                    for (String mapName : this.mapVotes.values()) {
                        voteCount.put(mapName, voteCount.getOrDefault(mapName, 0) + 1);
                    }

                    String mostVotedMap = null;
                    int maxVotes = 0;

                    for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
                        if (entry.getValue() > maxVotes) {
                            maxVotes = entry.getValue();
                            mostVotedMap = entry.getKey();
                        }
                    }

                    return mostVotedMap;
                }
            } else {
                return this.getForceMapName();
            }
        }
    }
}
