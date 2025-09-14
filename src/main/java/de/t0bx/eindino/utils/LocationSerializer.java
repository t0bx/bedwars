package de.t0bx.eindino.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerializer {
    public static String toString(Location location) {
        return location.getWorld().getName()
                + ";" + location.getX()
                + ";" + location.getY()
                + ";" + location.getZ()
                + ";" + location.getYaw()
                + ";" + location.getPitch();
    }

    public static Location locFromString(String str){
        if(str == null || str.isEmpty()) return null;

        String[] strar = str.split(";");
        Location newLoc = new Location(Bukkit.getWorld(strar[0]), Double.parseDouble(strar[1]),
                Double.parseDouble(strar[2]),
                Double.parseDouble(strar[3]),
                Float.parseFloat(strar[4]),
                Float.parseFloat(strar[5]));
        return newLoc.clone();
    }
}
