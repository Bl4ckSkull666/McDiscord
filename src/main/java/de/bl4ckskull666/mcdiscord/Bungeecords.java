package de.bl4ckskull666.mcdiscord;

import net.dv8tion.jda.core.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bungeecords implements Listener {
    private static HashMap<UUID, String> _users = new HashMap<>();
    private static List<UUID> _connected = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if(!McDiscord.getConfig().getBoolean("inform.chat", false))
            return;

        if(e.isCancelled() || e.getMessage().isEmpty())
            return;

        ProxiedPlayer ps = McDiscord.getPlayer(e.getSender());
        if(ps == null)
            return;

        if(e.isCommand() || e.getMessage().startsWith("/"))
            return;

        McDiscord.getDiscord().sendMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("messages.discord.chat", "[MC %s] %p: %m"), ps.getName(), e.getMessage(), ps.getServer().getInfo().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        if(!McDiscord.getConfig().getBoolean("inform.disconnect", false))
            return;

        _connected.remove(e.getPlayer().getUniqueId());
        McDiscord.getDiscord().sendMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.disconnect", ""), McDiscord.getConfig().getString("messages.discord.disconnect", "Player &p has left the Minecraft Server. Last Server %s"), e.getPlayer().getName(), "", e.getPlayer().getServer().getInfo().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent e) {
        if(!McDiscord.getConfig().getBoolean("inform.kick", false))
            return;

        _connected.remove(e.getPlayer().getUniqueId());
        McDiscord.getDiscord().sendMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.kick", ""), McDiscord.getConfig().getString("messages.discord.kick", "Player %p was kicked from the server %s. Reason %m"), e.getPlayer().getName(), e.getKickReasonComponent().toString(), e.getPlayer().getServer().getInfo().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent e) {
        if(McDiscord.getBans().containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().disconnect(McDiscord.getBans().get(e.getPlayer().getUniqueId()));
            return;
        }

        String server = e.getTarget() == null?"":e.getTarget().getName();
        if(_connected.contains(e.getPlayer().getUniqueId())) {
            if(!McDiscord.getConfig().getBoolean("inform.switch", false))
                return;

            McDiscord.getDiscord().sendMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.switch", ""), McDiscord.getConfig().getString("messages.discord.switch", "Player %p changed the server to %s"), e.getPlayer().getName(),  "", server);
            return;
        }

        _connected.add(e.getPlayer().getUniqueId());
        if(!McDiscord.getConfig().getBoolean("inform.connect", false))
            return;

        McDiscord.getDiscord().sendMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.connect", ""), McDiscord.getConfig().getString("messages.discord.connect", "A Player %p  joined the server. Current server %s"), e.getPlayer().getName(), "", server);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerSwitch(ServerSwitchEvent e) {
        if(!McDiscord.getConfig().getBoolean("inform.switch", false))
            return;

        McDiscord.getDiscord().sendMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.switch", ""), McDiscord.getConfig().getString("messages.discord.switch", "Player %p changed the server to %s"), e.getPlayer().getName(),  "", e.getPlayer().getServer().getInfo().getName());
    }

    public static void BanPlayer(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 3) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(a[1]);
        if(pp == null) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-player", "@%p the wished player can't be found."), u.getName(), "", "");
            return;
        }

        String strBuild = "";
        for(int i = 2; i < a.length; i++) {
            if(!strBuild.isEmpty())
                strBuild += " ";

            strBuild += a[i];
        }

        TextComponent[] tcs = new TextComponent[2];
        tcs[0] = new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("admin.ban-header", "&l&o&c!!! BANNED !!!") + "\n"));
        tcs[1] = new TextComponent(ChatColor.translateAlternateColorCodes('&', strBuild));
        McDiscord.getBans().put(pp.getUniqueId(), tcs);
        pp.disconnect(tcs);
    }

    public static void UnBanPlayer(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 2) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        UUID uuid = null;
        for(Map.Entry<UUID, String> me: _users.entrySet()) {
            if(me.getValue().equalsIgnoreCase(a[1])) {
                uuid = me.getKey();
                break;
            }
        }

        if(uuid == null) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-user", "@%p the wished player can't be found."), u.getName(), "", "");
            return;
        }

        McDiscord.getBans().remove(uuid);
        McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("admin.rejoin", "@%p player can now join again.")), u.getName(), "", "");
    }

    public static void KickPlayer(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 3) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(a[1]);
        if(pp == null) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-player", "@%p the wished player can't be found."), u.getName(), "", "");
            return;
        }

        String strBuild = "";
        for(int i = 2; i < a.length; i++) {
            if(!strBuild.isEmpty())
                strBuild += " ";

            strBuild += a[i];
        }

        TextComponent[] tcs = new TextComponent[2];
        tcs[0] = new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("admin.kick-header", "&l&o&c!!! KICKED !!!") + "\n"));
        tcs[1] = new TextComponent(ChatColor.translateAlternateColorCodes('&', strBuild));
        pp.disconnect(tcs);
    }

    public static void BroadcastMessage(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 2) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        String strBuild = "";
        for(int i = 1; i < a.length; i++) {
            if(!strBuild.isEmpty())
                strBuild += " ";

            strBuild += a[i];
        }

        ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("admin.broadcast", "&f[&aBroadcast&f] &6%m").replace("%m", strBuild))));
    }

    public static void AnnounceMessage(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 2) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        String strBuild = "";
        for(int i = 1; i < a.length; i++) {
            if(!strBuild.isEmpty())
                strBuild += " ";

            strBuild += a[i];
        }

        Title myTitle = ProxyServer.getInstance().createTitle();
        myTitle.title(new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("announce.header", "&9Announce"))));
        myTitle.subTitle(new TextComponent(ChatColor.translateAlternateColorCodes('&', strBuild)));
        myTitle.stay(McDiscord.getConfig().getInt("announce.stay", 100));
        myTitle.fadeIn(McDiscord.getConfig().getInt("announce.fade-in", 40));
        myTitle.fadeOut(McDiscord.getConfig().getInt("announce.fade-out", 40));

        for(ProxiedPlayer pp: ProxyServer.getInstance().getPlayers()) {
            myTitle.send(pp);
        }
    }

    public static void ShutdownProxy(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 2) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(a[1]);
        } catch(NumberFormatException ex) {
            McDiscord.getDiscord().sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-number", "@%p please set a number."), u.getName(), "", "");
            return;
        }

        ProxyServer.getInstance().getScheduler().schedule(McDiscord.getInstance(), new EveryTask.DoShutDown(seconds), 0, 1, TimeUnit.SECONDS);
    }
}
