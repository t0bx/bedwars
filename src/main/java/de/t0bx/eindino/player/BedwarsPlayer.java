package de.t0bx.eindino.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Setter
public class BedwarsPlayer {
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int wins;
    private int gamesPlayed;
    private int bedsDestroyed;
}
