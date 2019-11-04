package de.bl4ckskull666.mcdiscord;

import de.bl4ckskull666.mcdiscord.utils.Metrics;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class McDiscord extends Plugin implements Listener {
    private static Discord _discord;
    private static McDiscord _instance;
    private static FileConfiguration _config;
    private static List<String> _admins = new ArrayList<>();
    private static HashMap<UUID, TextComponent[]> _tempBans = new HashMap<>();
    private static Calendar _started;

    @Override
    public void onEnable() {
        _instance = this;
        File f = new File(this.getDataFolder(), "config.yml");
        _config = YamlConfiguration.loadConfiguration(f);
        boolean freshConfig = !f.exists();
        HashMap<String, Object> defCon = new HashMap<>();

        defCon.put("discord.token", "Check out https://discordapp.com/developers/applications/ to create a token.");
        defCon.put("discord.invite", "Clear it to let the bot a key generate or add a invite code here.");
        defCon.put("discord.channelId.chat", "Right click a channel and press ID copy");
        defCon.put("discord.channelId.disconnect", "Right click a channel and press ID copy");
        defCon.put("discord.channelId.connect", "Right click a channel and press ID copy");
        defCon.put("discord.channelId.kick", "Right click a channel and press ID copy");
        defCon.put("discord.channelId.switch", "Right click a channel and press ID copy");

        defCon.put("inform.ingame", true);
        defCon.put("inform.chat", true);
        defCon.put("inform.connect", true);
        defCon.put("inform.disconnect", true);
        defCon.put("inform.kick", true);
        defCon.put("inform.switch", false);

        //%s = Server , %p = Playername, %m = Message
        defCon.put("messages.discord.chat", "[MC %s] %p: %m");
        defCon.put("messages.discord.disconnect", "Player %p has left the server. Last server %s");
        defCon.put("messages.discord.connect", "A Player %p  joined the server. Current server %s");
        defCon.put("messages.discord.kick", "Player %p was kicked from the server %s. Reason %m");
        defCon.put("messages.discord.switch", "Player %p changed the server to %s");

        // &c = Discord Invite Code, %p = Discord User Name, %m = Discord Message
        defCon.put("messages.ingame.1.message", "&f[&9Discord&f]");
        defCon.put("messages.ingame.1.hover-msg", "&eJoin our Discord: &c%c");
        defCon.put("messages.ingame.1.click-msg", "http://discord.gg/%c");
        defCon.put("messages.ingame.1.click-type", "open_url");
        defCon.put("messages.ingame.2.message", "&2%p&e: &6%m");

        defCon.put("admin.need-args", "@%p you have forget arguments for this command.");
        defCon.put("admin.no-number", "@%p please set a number.");
        defCon.put("admin.no-player", "@%p the wished player can't be found.");
        defCon.put("admin.no-user", "@%p the wished user can't be found.");
        defCon.put("admin.rejoin", "@%p player can now join again.");
        defCon.put("admin.ban-header", "&l&o&c!!! BANNED !!!");
        defCon.put("admin.kick-header", "&l&o&c!!! KICKED !!!");
        defCon.put("admin.broadcast", "&f[&aBroadcast&f] &6%m");
        defCon.put("admin.admin-added", "Added %p as Discord Admin of me.");
        defCon.put("admin.admin-removed", "Removed %p as Discord Admin of me");

        defCon.put("shutdown.start", "&cA countdown to shutdown the BungeeCord Server has been started. Shutdown in %t seconds.");
        defCon.put("shutdown.900", "&cThe BungeeCord Server will be shutdown in 15 minutes.");
        defCon.put("shutdown.600", "&cThe BungeeCord Server will be shutdown in 10 minutes.");
        defCon.put("shutdown.300", "&cThe BungeeCord Server will be shutdown in 5 minutes.");
        defCon.put("shutdown.60", "&cThe BungeeCord Server will be shutdown in 1 minute.");
        defCon.put("shutdown.60", "&cThe BungeeCord Server will be shutdown in 1 minute.");
        defCon.put("shutdown.30", "&cThe BungeeCord Server will be shutdown in 30 seconds.");
        defCon.put("shutdown.10", "&cThe BungeeCord Server will be shutdown in 10 seconds.");
        defCon.put("shutdown.3", "&cThe BungeeCord Server will be shutdown in 3 seconds.");
        defCon.put("shutdown.2", "&cThe BungeeCord Server will be shutdown in 2 seconds.");
        defCon.put("shutdown.1", "&cThe BungeeCord Server will be shutdown in 1 second.");
        defCon.put("shutdown.end", "&cThe BungeeCord Server will be shutdown now.");
        defCon.put("shutdown.kick", "&eWe will see us back. Good Bye.");

        defCon.put("serverinfo.name", "My Minecraft Network");
        defCon.put("serverinfo.url", "https://www.spigotmc.org");
        defCon.put("serverinfo.client", "Version 1.14");
        defCon.put("serverinfo.memory", "%use / %free / %max MB (%per %)");
        defCon.put("serverinfo.color.r", "255");
        defCon.put("serverinfo.color.g", "255");
        defCon.put("serverinfo.color.b", "255");
        defCon.put("serverinfo.image", "");
        defCon.put("serverinfo.thumbnail", "");
        defCon.put("serverinfo.motd", "");
        defCon.put("serverinfo.website", "");
        defCon.put("serverinfo.ip", "");
        defCon.put("serverinfo.address", "");

        defCon.put("serverinfo.header.players", "Players");
        defCon.put("serverinfo.header.uptime", "Uptime");
        defCon.put("serverinfo.header.servers", "Servers");
        defCon.put("serverinfo.header.serverlist", "Online Servers");
        defCon.put("serverinfo.header.memory", "Memory");
        defCon.put("serverinfo.header.client", "Client Version");
        defCon.put("serverinfo.header.website", "Website");
        defCon.put("serverinfo.header.ip", "Server IP");
        defCon.put("serverinfo.header.address", "Server Address");
        defCon.put("serverinfo.header.version", "Proxy Version");
        defCon.put("serverinfo.header.plugins", "Plugins");
        defCon.put("serverinfo.header.pings", "Pings (last 15 mins./Total)");

        defCon.put("playerlist.name", "My Minecraft Network");
        defCon.put("playerlist.url", "https://www.spigotmc.org");
        defCon.put("playerlist.no-players", "There are currently no players on the server(s)... :sob: :sob:");
        defCon.put("playerlist.seperator", ", ");
        defCon.put("playerlist.color.r", "0");
        defCon.put("playerlist.color.g", "255");
        defCon.put("playerlist.color.b", "255");
        defCon.put("playerlist.image", "");
        defCon.put("playerlist.thumbnail", "");
        defCon.put("playerlist.motd", "");
        defCon.put("playerlist.more-online", "... and %count more online.");

        defCon.put("playerlist.header.all", "Here are all online players ( max. 50 )");
        defCon.put("playerlist.header.group", "Online Players in Group %group");
        defCon.put("playerlist.header.server", "All Players on Server %srv");
        defCon.put("playerlist.header.no-players", "All Offline!");

        defCon.put("playerlist.max.all", 100);
        defCon.put("playerlist.max.group", 25);
        defCon.put("playerlist.max.server", 25);

        defCon.put("playerlist.display", "all");
        defCon.put("playerlist.single-group", true);

        defCon.put("pluginlist.name", "My Minecraft Network");
        defCon.put("pluginlist.url", "https://www.spigotmc.org");
        defCon.put("pluginlist.color.r", "0");
        defCon.put("pluginlist.color.g", "255");
        defCon.put("pluginlist.color.b", "255");
        defCon.put("pluginlist.image", "");
        defCon.put("pluginlist.thumbnail", "");
        defCon.put("pluginlist.motd", "");
        defCon.put("pluginlist.header", "%name by %author");
        defCon.put("pluginlist.field", "%version | %description");

        defCon.put("commandlist.name", "My Minecraft Network");
        defCon.put("commandlist.url", "https://www.spigotmc.org");
        defCon.put("commandlist.color.r", "0");
        defCon.put("commandlist.color.g", "255");
        defCon.put("commandlist.color.b", "255");
        defCon.put("commandlist.image", "");
        defCon.put("commandlist.thumbnail", "");
        defCon.put("commandlist.motd", "");

        defCon.put("announce.header", "&cAnnounce");
        defCon.put("announce.stay", 100);
        defCon.put("announce.fade-in", 40);
        defCon.put("announce.fade-out", 40);

        defCon.put("embeds.serverinfo.players", true);
        defCon.put("embeds.serverinfo.uptime", true);
        defCon.put("embeds.serverinfo.servers", true);
        defCon.put("embeds.serverinfo.serverlist", false);
        defCon.put("embeds.serverinfo.memory", true);
        defCon.put("embeds.serverinfo.client", true);
        defCon.put("embeds.serverinfo.website", true);
        defCon.put("embeds.serverinfo.address", true);
        defCon.put("embeds.serverinfo.ip", true);
        defCon.put("embeds.serverinfo.plugins", true);
        defCon.put("embeds.serverinfo.pings", true);
        defCon.put("embeds.serverinfo.version", false);

        defCon.put("embeds.playerlist.no-players", false);
        defCon.put("embeds.playerlist.all", false);
        defCon.put("embeds.playerlist.group", false);
        defCon.put("embeds.playerlist.server", false);

        defCon.put("servers.lobby", "Lobby");
        defCon.put("servers.city", "City Life");
        defCon.put("servers.pvp", "Fight Town");

        defCon.put("groups.default", "Citizen");
        defCon.put("groups.vip", "VIP");
        defCon.put("groups.admin", "Admin");
        defCon.put("groups.owner", "Owner");

        defCon.put("user.in-cooldown", "Please wait few minutes before you run the command.");
        defCon.put("user.no-permission", "You don't have permission to use this command.");
        defCon.put("user.time-seperator", ", ");
        defCon.put("user.weeks", " w");
        defCon.put("user.week", " w");
        defCon.put("user.days", " d");
        defCon.put("user.day", " d");
        defCon.put("user.hours", " h");
        defCon.put("user.hour", " h");
        defCon.put("user.minutes", " m");
        defCon.put("user.minute", " m");
        defCon.put("user.seconds", " s");
        defCon.put("user.second", " s");

        defCon.put("commands.serverinfo", "Display some informations about the Proxy Server.");
        defCon.put("commands.playerlist", "Display a list of Online players. (server) is optional.");
        defCon.put("commands.pluginlist", "Display a list of all installed BungeeCord Plugins.");
        defCon.put("commands.ban", "Ban (player) with the (reason). Ban is removed on next ProxyServer restart.");
        defCon.put("commands.unban", "Unban a (player) who is banned with !ban.");
        defCon.put("commands.kick", "Kick the (player) with (reason)");
        defCon.put("commands.broadcast", "broadcast the (message) in the Ingame chat.");
        defCon.put("commands.announce", "Display a Screen Message on all online players.");
        defCon.put("commands.shutdown", "Shutdown the ProxyServer in (seconds) with countdown.");
        defCon.put("commands.admin", "Toggle Discord Bot Admin by Discord UserId");

        defCon.put("command-name.mcban", "ban");
        defCon.put("command-name.mcunban", "unban");
        defCon.put("command-name.mckick", "kick");
        defCon.put("command-name.mcbroadcast", "broadcast");
        defCon.put("command-name.mcannounce", "announce");
        defCon.put("command-name.mcadmin", "admin");
        defCon.put("command-name.mcinfo", "serverinfo");
        defCon.put("command-name.mcplayers", "players");
        defCon.put("command-name.mcplugins", "plugins");
        defCon.put("command-name.mcshutdown", "shutdown");

        defCon.put("only-admin.serverlist", false);
        defCon.put("only-admin.playerlist", false);
        defCon.put("only-admin.pluginlist", false);
        defCon.put("only-admin.ban", true);
        defCon.put("only-admin.unban", true);
        defCon.put("only-admin.kick", true);
        defCon.put("only-admin.broadcast", true);
        defCon.put("only-admin.announce", true);
        defCon.put("only-admin.shutdown", true);
        defCon.put("only-admin.admin", true);

        boolean save = false;
        for(Map.Entry<String, Object> me: defCon.entrySet()) {
            if(_config.isSet(me.getKey()))
                continue;

            getLogger().info("Added configuration path " + me.getKey());
            _config.set(me.getKey(), me.getValue());
            save = true;
        }

        if(save) {
            try {
                _config.save(f);
            } catch(Exception ex) {
                getLogger().log(Level.WARNING, "Error on save default configuration.", ex);
            } finally {
                if(freshConfig) {
                    getLogger().log(Level.INFO, "Configuration saved. please configurate it and restart proxy server.");
                    return;
                }
            }
        }

        try {
            _discord = new Discord(_config.getString("discord.token", ""), _config.getString("discord.channelId.chat"));
        } catch (LoginException ex) {
            getLogger().log(Level.WARNING, "Error on authentification with discord.", ex);
        } catch (InterruptedException ex) {
            getLogger().log(Level.WARNING, "Error with discord while wrong informations.", ex);
        } finally {
            ProxyServer.getInstance().getPluginManager().registerListener(this, new Bungeecords());
            loadAdmins();
            ProxyServer.getInstance().getScheduler().schedule(this, new EveryTask.UpdateDiscordGame(), 2, 2, TimeUnit.MINUTES);
            Metrics metrics = new Metrics(this);
            _started = Calendar.getInstance();
        }
    }

    private void loadAdmins() {
        File f = new File(this.getDataFolder(), "admins.yml");
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        if(!f.exists()) {
            List<String> tmp = new ArrayList<>();
            tmp.add("239145240630394880");
            fc.set("admins", tmp);

            try {
                fc.save(f);
            } catch(Exception ex) {
                getLogger().log(Level.WARNING, "Can't save default admins.yml file", ex);
                return;
            }
        }

        _admins.addAll(fc.getStringList("admins"));
    }

    private static void saveAdmins() {
        File f = new File(_instance.getDataFolder(), "admins.yml");
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        fc.set("admins", _admins);
        try {
            fc.save(f);
        } catch(Exception ex) {
            _instance.getLogger().warning("Can't save admin list.");
        }
    }

    public static FileConfiguration getConfig() {
        return _config;
    }

    public static McDiscord getInstance() {
        return _instance;
    }

    public static Discord getDiscord() {
        return _discord;
    }

    public static boolean isAdmin(String id) {
        return _admins.contains(id);
    }

    public static boolean toggleAdmin(String id) {
        if(isAdmin(id)) {
            _admins.remove(id);
        } else {
            _admins.add(id);
        }

        saveAdmins();
        return isAdmin(id);
    }

    public static HashMap<UUID, TextComponent[]> getBans() {
        return _tempBans;
    }

    private static String ReplaceAll(String txt, String name, String msg, String code) {
        txt = txt.replace("%p", name);
        txt = txt.replace("%m", msg);
        txt = txt.replace("%c", (code.isEmpty()?_discord.getInviteCode():code));
        return ChatColor.translateAlternateColorCodes('&', txt);
    }

    public static void sendMessage(String name, String msg) {
        HashMap<Integer, TextComponent> tcMain = new HashMap<>();
        int iLine = 1;
        TextComponent tcLine = new TextComponent("");
        for(String k: getConfig().getConfigurationSection("messages.ingame").getKeys(false)) {
            if(tcLine == null)
                tcLine = new TextComponent("");

            ConfigurationSection cs = getConfig().getConfigurationSection("messages.ingame." + k);
            if(!cs.isString("message"))
                continue;

            TextComponent tcNext = new TextComponent(ReplaceAll(cs.getString("message"), name, msg, getConfig().getString("discord.invite", "")));
            if(cs.isString("hover-msg")) {
                HoverEvent hoverev = new HoverEvent(
                        getHoverAction(cs.getString("hover-type", "text")),
                        new ComponentBuilder(ReplaceAll(cs.getString("hover-msg"), name, msg, getConfig().getString("discord.invite", ""))).create()
                );
                tcNext.setHoverEvent(hoverev);
            }

            if(cs.isString("click-msg")) {
                ClickEvent clickev = new ClickEvent(
                        getClickAction(cs.getString("click-type", "open_url")),
                        ReplaceAll(cs.getString("click-msg"), name, msg, getConfig().getString("discord.invite", ""))
                );
                tcNext.setClickEvent(clickev);
            }

            tcLine.addExtra(tcNext);

            // End of Message?!
            if(cs.getBoolean("break", false)) {
                tcMain.put(iLine, tcLine);
                tcLine = null;
                iLine++;
            }
        }

        if(tcLine != null) {
            tcMain.put(iLine, tcLine);
            iLine++;
        }

        List<Integer> it = new ArrayList<>();
        it.addAll(tcMain.keySet());
        Collections.sort(it);

        for(ProxiedPlayer pp: ProxyServer.getInstance().getPlayers()) {
            for (Integer i: it)
                pp.sendMessage(tcMain.get(i));
        }
    }

    private static HoverEvent.Action getHoverAction(String str) {
        if(HoverEvent.Action.valueOf("SHOW_" + str.toUpperCase()) != null)
            return HoverEvent.Action.valueOf("SHOW_" + str.toUpperCase());
        return HoverEvent.Action.SHOW_TEXT;
    }

    private static ClickEvent.Action getClickAction(String str) {
        if(ClickEvent.Action.valueOf(str.toUpperCase()) != null)
            return ClickEvent.Action.valueOf(str.toUpperCase());
        return ClickEvent.Action.RUN_COMMAND;
    }

    public static ProxiedPlayer getPlayer(Connection c) {
        for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if(p.getPendingConnection().getAddress() == c.getAddress())
                return p;
        }
        return null;
    }

    public static String OnlineSince() {
        String timeString = "";
        boolean isBefore = false;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = (int)(Calendar.getInstance().getTimeInMillis() - _started.getTimeInMillis()) / 1000;

        if(seconds > (60*60*24*7)) {
            weeks = (int) Math.floor(seconds / (60 * 60 * 24 * 7));
            seconds -= (60 * 60 * 24 * 7) * weeks;
            if (weeks > 0) {
                if (weeks == 1)
                    timeString += "0" + weeks + McDiscord.getConfig().getString("user.week", " w");
                else
                    timeString += (weeks < 10?"0":"") + weeks + McDiscord.getConfig().getString("user.weeks", " w");
                isBefore = true;
            }
        }

        if(seconds > (60*60*24)) {
            days = (int)Math.floor(seconds/(60*60*24));
            seconds -= (60*60*24)*days;
            if(days > 0) {
                if(isBefore)
                    timeString += McDiscord.getConfig().getString("user.time-seperator", ", ");

                if(days == 1)
                    timeString += "0" + days + McDiscord.getConfig().getString("user.day", " d");
                else
                    timeString += (days < 10?"0":"") + days + McDiscord.getConfig().getString("user.days", " d");
                isBefore = true;
            }
        }

        if(seconds > (60*60)) {
            hours = (int)Math.floor(seconds/(60*60));
            seconds -= (60*60)*hours;
            if(hours > 0) {
                if(isBefore)
                    timeString += McDiscord.getConfig().getString("user.time-seperator", ", ");

                if(hours == 1)
                    timeString += "0" + hours + McDiscord.getConfig().getString("user.hour", " h");
                else
                    timeString += (hours < 10?"0":"") + hours + McDiscord.getConfig().getString("user.hours", " h");
                isBefore = true;
            }
        }

        if(seconds > 60) {
            minutes = (int)Math.floor(seconds/60);
            seconds -= 60*minutes;
            if(minutes > 0) {
                if(isBefore)
                    timeString += McDiscord.getConfig().getString("user.time-seperator", ", ");

                if(minutes == 1)
                    timeString += "0" + minutes + McDiscord.getConfig().getString("user.minute", " m");
                else
                    timeString += (minutes < 10?"0":"") + minutes + McDiscord.getConfig().getString("user.minutes", " m");
                isBefore = true;
            }
        }

        if(seconds > 0) {
            if(isBefore)
                timeString += McDiscord.getConfig().getString("user.time-seperator", ", ");

            if(seconds == 1)
                timeString += "0" + seconds + McDiscord.getConfig().getString("user.second", " m");
            else
                timeString += (seconds < 10?"0":"") + seconds + McDiscord.getConfig().getString("user.seconds", " m");
        }
        return timeString;
    }
}
