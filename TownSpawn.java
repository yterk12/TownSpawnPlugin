package me.seninpaketin;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TownSpawn extends JavaPlugin {
    private Map<UUID, String> playerTowns = new HashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        playerTowns = new HashMap<>();
        getLogger().info("TownSpawn plugin etkin!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TownSpawn plugin kapandı!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular için geçerlidir.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (command.getName().equalsIgnoreCase("settownspawn")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Kullanım: /settownspawn <isim>");
                return true;
            }
            String townName = args[0].toLowerCase();

            if (config.contains("townspawns." + townName)) {
                player.sendMessage(ChatColor.RED + "Bu isimde zaten bir spawn var!");
                return true;
            }

            if (playerTowns.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Zaten bir spawn noktan var. Önce /removetownspawn ile kaldır.");
                return true;
            }

            if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                player.sendMessage(ChatColor.RED + "Spawn noktası sadece ana dünyada oluşturulabilir.");
                return true;
            }

            Location loc = player.getLocation();
            config.set("townspawns." + townName + ".world", loc.getWorld().getName());
            config.set("townspawns." + townName + ".x", loc.getX());
            config.set("townspawns." + townName + ".y", loc.getY());
            config.set("townspawns." + townName + ".z", loc.getZ());
            saveConfig();

            playerTowns.put(uuid, townName);
            player.sendMessage(ChatColor.GREEN + townName + " adlı kasaba spawn noktan ayarlandı.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("townspawn")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Kullanım: /townspawn <isim>");
                return true;
            }
            String townName = args[0].toLowerCase();

            if (!config.contains("townspawns." + townName)) {
                player.sendMessage(ChatColor.RED + "Böyle bir kasaba spawn noktası bulunamadı.");
                return true;
            }

            String worldName = config.getString("townspawns." + townName + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(ChatColor.RED + "Dünya yüklenemedi.");
                return true;
            }

            double x = config.getDouble("townspawns." + townName + ".x");
            double y = config.getDouble("townspawns." + townName + ".y");
            double z = config.getDouble("townspawns." + townName + ".z");

            Location loc = new Location(world, x, y, z);
            player.teleport(loc);
            player.sendMessage(ChatColor.GREEN + townName + " kasaba spawn noktasına ışınlandın.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("townspawnlist")) {
            Set<String> towns = config.getConfigurationSection("townspawns").getKeys(false);
            if (towns.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Hiç kasaba spawn noktası yok.");
                return true;
            }
            player.sendMessage(ChatColor.AQUA + "Kasaba spawnları:");
            for (String t : towns) {
                player.sendMessage("- " + t);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("removetownspawn")) {
            if (!playerTowns.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Senin bir kasaba spawn noktan yok.");
                return true;
            }
            String townName = playerTowns.get(uuid);
            config.set("townspawns." + townName, null);
            saveConfig();
            playerTowns.remove(uuid);
            player.sendMessage(ChatColor.GREEN + townName + " adlı kasaba spawn noktan silindi.");
            return true;
        }

        return false;
    }
}
