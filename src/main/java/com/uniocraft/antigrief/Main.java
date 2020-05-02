package com.uniocraft.antigrief;

import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    public static Permission permission = null;
    public static Plugin plugin;
    static FileConfiguration fc;
    static SQLManager sql;
    private Rcon rcon;

    public void onEnable() {
        saveDefaultConfig();
        fc = this.getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if ((permissionProvider != null)) {
            permission = permissionProvider.getProvider();
        }
        plugin = this;
        sql = new SQLManager(this);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::connectRcon);
    }

    public void onDisable() {
        sql.onDisable();
    }

    public void connectRcon() {
        try {
            rcon = new Rcon(getConfig().getString("rcon.host"), getConfig().getInt("rcon.port"), getConfig().getString("rcon.pass").getBytes());
        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 1) {
            if (cmd.getName().equalsIgnoreCase("bcmd")) {
                if (sender.equals(Bukkit.getConsoleSender())) {
                    String cmdd = StringUtils.join(args, ' ', 0, args.length);

                    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                        connectRcon();
                        try {
                            if (rcon != null && !rcon.getSocket().isClosed()) {

                                rcon.command(cmdd);
                            } else {
                                connectRcon();
                                rcon.command(cmdd);
                            }
                        } catch (IOException e) {
                            connectRcon();
                            try {
                                rcon.command(cmdd);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (event.getPlayer().isOp() || (permission.playerInGroup(event.getPlayer(), "Kurucu")) || (event.getPlayer().hasPermission("*")) || (event.getPlayer().hasPermission("zpermissions.*"))) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                if (!sql.checkPlayerIP(event.getPlayer().getName(), event.getAddress().getHostAddress())) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        event.getPlayer().setOp(false);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "perm player " + event.getPlayer().getName() + " purge");
                    });
                }
            });
        }

        if (event.getPlayer().getName().equalsIgnoreCase("UnioDex")) {
            if (!sql.checkPlayerIP(event.getPlayer().getName(), event.getAddress().getHostAddress())) {
                event.setKickMessage("\n§cBu isimle oyuna giriş yapamazsınız.");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "\n§cBu isimle oyuna giriş yapamazsınız.");
            }
        }

        if (event.getPlayer().getName().toLowerCase().contains("unioguvenlik")) {
            if (!sql.checkPlayerIP("UnioDex", event.getAddress().getHostAddress())) {
                event.setKickMessage("\n§cBu isimle oyuna giriş yapamazsınız.");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "\n§cBu isimle oyuna giriş yapamazsınız.");
            }
        }

        List<String> li = getConfig().getStringList("players");

        if (li.contains(event.getPlayer().getName())) {
            if (!sql.checkPlayerIP(event.getPlayer().getName(), event.getAddress().getHostAddress())) {
                event.setKickMessage("\n§cBu isimle oyuna giriş yapamazsınız.");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "\n§cBu isimle oyuna giriş yapamazsınız.");
            }
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().isOp() || (permission.playerInGroup(event.getPlayer(), "Kurucu")) || (event.getPlayer().hasPermission("*")) || (event.getPlayer().hasPermission("zpermissions.*"))) {
            if (!sql.checkPlayerIP(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress())) {
                event.getPlayer().setOp(false);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "perm player " + event.getPlayer().getName() + " purge");
            }
        }
    }
}
