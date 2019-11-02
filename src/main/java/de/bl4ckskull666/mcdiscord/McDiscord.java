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

    @Override
    public void onEnable() {
        _instance = this;
        File f = new File(this.getDataFolder(), "config.yml");
        _config = YamlConfiguration.loadConfiguration(f);
        if(!f.exists()) {
            _config.set("discord.token", "Check out https://discordapp.com/developers/applications/ to create a token.");
            _config.set("discord.invite", "Clear it to let the bot a key generate or add a invite code here.");
            _config.set("discord.channelId.chat", "Right click a channel and press ID copy");
            _config.set("discord.channelId.disconnect", "Right click a channel and press ID copy");
            _config.set("discord.channelId.connect", "Right click a channel and press ID copy");
            _config.set("discord.channelId.kick", "Right click a channel and press ID copy");
            _config.set("discord.channelId.switch", "Right click a channel and press ID copy");

            _config.set("inform.ingame", true);
            _config.set("inform.chat", true);
            _config.set("inform.connect", true);
            _config.set("inform.disconnect", true);
            _config.set("inform.kick", true);
            _config.set("inform.switch", false);

            //%s = Server , %p = Playername, %m = Message
            _config.set("messages.discord.chat", "[MC %s] %p: %m");
            _config.set("messages.discord.disconnect", "Player %p has left the server. Last server %s");
            _config.set("messages.discord.connect", "A Player %p  joined the server. Current server %s");
            _config.set("messages.discord.kick", "Player %p was kicked from the server %s. Reason %m");
            _config.set("messages.discord.switch", "Player %p changed the server to %s");

            // &c = Discord Invite Code, %p = Discord User Name, %m = Discord Message
            _config.set("messages.ingame.1.message", "&f[&9Discord&f]");
            _config.set("messages.ingame.1.hover-msg", "&eJoin our Discord: &c%c");
            _config.set("messages.ingame.1.click-msg", "http://discord.gg/%c");
            _config.set("messages.ingame.1.click-type", "open_url");
            _config.set("messages.ingame.2.message", "&2%p&e: &6%m");

            _config.set("admin.no-permission", "You @%p has no permission to use a discord command.");
            _config.set("admin.need-args", "@%p you have forget arguments for this command.");
            _config.set("admin.no-number", "@%p please set a number.");
            _config.set("admin.no-player", "@%p the wished player can't be found.");
            _config.set("admin.no-user", "@%p the wished user can't be found.");
            _config.set("admin.rejoin", "@%p player can now join again.");
            _config.set("admin.ban-header", "&l&o&c!!! BANNED !!!");
            _config.set("admin.kick-header", "&l&o&c!!! KICKED !!!");
            _config.set("admin.broadcast", "&f[&aBroadcast&f] &6%m");
            _config.set("admin.admin-added", "Added %p as Discord Admin of me.");
            _config.set("admin.admin-removed", "Removed %p as Discord Admin of me");

            _config.set("shutdown.start", "&cA countdown to shutdown the BungeeCord Server has been started. Shutdown in %t seconds.");
            _config.set("shutdown.900", "&cThe BungeeCord Server will be shutdown in 15 minutes.");
            _config.set("shutdown.600", "&cThe BungeeCord Server will be shutdown in 10 minutes.");
            _config.set("shutdown.300", "&cThe BungeeCord Server will be shutdown in 5 minutes.");
            _config.set("shutdown.60", "&cThe BungeeCord Server will be shutdown in 1 minute.");
            _config.set("shutdown.60", "&cThe BungeeCord Server will be shutdown in 1 minute.");
            _config.set("shutdown.30", "&cThe BungeeCord Server will be shutdown in 30 seconds.");
            _config.set("shutdown.10", "&cThe BungeeCord Server will be shutdown in 10 seconds.");
            _config.set("shutdown.3", "&cThe BungeeCord Server will be shutdown in 3 seconds.");
            _config.set("shutdown.2", "&cThe BungeeCord Server will be shutdown in 2 seconds.");
            _config.set("shutdown.1", "&cThe BungeeCord Server will be shutdown in 1 second.");
            _config.set("shutdown.end", "&cThe BungeeCord Server will be shutdown now.");
            _config.set("shutdown.kick", "&eWe will see us back. Good Bye.");

            _config.set("announce.header", "&cAnnounce");
            _config.set("announce.stay", 100);
            _config.set("announce.fade-in", 40);
            _config.set("announce.fade-out", 40);

            try {
                _config.save(f);
            } catch(Exception ex) {
                getLogger().log(Level.WARNING, "Error on save default configuration.", ex);
            } finally {
                this.getLogger().log(Level.INFO, "Configuration saved. please configurate it and restart proxy server.");
            }
            return;
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
}
