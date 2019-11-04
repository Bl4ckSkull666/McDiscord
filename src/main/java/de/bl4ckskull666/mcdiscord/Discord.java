package de.bl4ckskull666.mcdiscord;

import de.bl4ckskull666.mcdiscord.utils.Rnd;
import de.bl4ckskull666.mcdiscord.utils.ServerUtils;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class Discord extends ListenerAdapter {
    static JDA MCD;
    private String _channelId;
    private HashMap<String, Calendar> _cooldown = new HashMap<>();

    public Discord(String TOKEN, String channelId) throws LoginException, InterruptedException {
        _channelId = channelId;
        MCD = new JDABuilder(AccountType.BOT).setToken(TOKEN).setStatus(OnlineStatus.ONLINE).buildAsync();
        MCD.addEventListener(this);
        MCD.getPresence().setPresence(OnlineStatus.ONLINE, Game.playing("Minecraft"));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(!e.getChannel().getId().equals(_channelId) || e.getAuthor().isBot())
            return;

        FileConfiguration conf = McDiscord.getConfig();
        if(e.getMessage().getContentStripped().startsWith("!")) {
            String[] a = e.getMessage().getContentStripped().split(" ");
            String cmd = "unknown";
            if(conf.isString("command-name." + a[0].toLowerCase().substring(1)))
                cmd = conf.getString("command-name." + a[0].toLowerCase().substring(1));

            switch(cmd.toLowerCase()) {
                case "ban":
                    if(!allowCommand(e.getAuthor(), "ban")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    Bungeecords.BanPlayer(e.getAuthor(), a);
                    break;
                case "unban":
                    if(!allowCommand(e.getAuthor(), "unban")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    Bungeecords.UnBanPlayer(e.getAuthor(), a);
                    break;
                case "kick":
                    if(!allowCommand(e.getAuthor(), "kick")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    Bungeecords.KickPlayer(e.getAuthor(), a);
                    break;
                case "broadcast":
                    if(!allowCommand(e.getAuthor(), "broadcast")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    Bungeecords.BroadcastMessage(e.getAuthor(), a);
                    break;
                case "announce":
                    if(!allowCommand(e.getAuthor(), "announce")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    Bungeecords.AnnounceMessage(e.getAuthor(), a);
                    break;
                case "shutdown":
                    if(!allowCommand(e.getAuthor(), "shutdown")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    Bungeecords.ShutdownProxy(e.getAuthor(), a);
                    break;
                case "admin":
                    if(!allowCommand(e.getAuthor(), "admin")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    CommandAdmin(e.getAuthor(), a);
                    break;
                case "serverinfo":
                    if(!allowCommand(e.getAuthor(), "serverinfo")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    if(_cooldown.containsKey("serverinfo")) {
                        Calendar lastRun = _cooldown.get("serverinfo");
                        int cdTime = McDiscord.getConfig().getInt("cooldowns.serverinfo", 300);
                        if(((Calendar.getInstance().getTimeInMillis() - lastRun.getTimeInMillis()) / 1000) < cdTime) {
                            sendTimeMessageToDiscord(_channelId, McDiscord.getConfig().getString("user.in-cooldown", "Please wait few minutes before you run the command."), e.getAuthor().getName(), "", "" );
                            break;
                        }
                        _cooldown.remove("serverinfo");
                    }

                    ServerInfo();
                    _cooldown.put("serverinfo", Calendar.getInstance());
                    break;
                case "players":
                    if(!allowCommand(e.getAuthor(), "players")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    if(_cooldown.containsKey("players")) {
                        Calendar lastRun = _cooldown.get("players");
                        int cdTime = McDiscord.getConfig().getInt("cooldowns.players", 300);
                        if(((Calendar.getInstance().getTimeInMillis() - lastRun.getTimeInMillis()) / 1000) < cdTime) {
                            sendTimeMessageToDiscord(_channelId, McDiscord.getConfig().getString("user.in-cooldown", "Please wait few minutes before you run the command."), e.getAuthor().getName(), "", "" );
                            break;
                        }
                        _cooldown.remove("players");
                    }

                    OnlinePlayers(e.getAuthor(), a);
                    _cooldown.put("players", Calendar.getInstance());
                    break;
                case "plugins":
                    if(!allowCommand(e.getAuthor(), "plugins")) {
                        sendTimeMessageToDiscord(_channelId, conf.getString("user.no-permission", "You don't has permission to use this command."), e.getAuthor().getName(), "", "");
                        break;
                    }
                    getPluginList();
                    break;
                default:
                    EmbedBuilder eb = new EmbedBuilder();
                    if(allowCommand(e.getAuthor(), "serverinfo"))
                        eb.addField("!" + getCommandByRealName("serverinfo"), conf.getString("commands.serverinfo", "Display some informations about the Proxy Server."), false);
                    if(allowCommand(e.getAuthor(), "playerlist"))
                        eb.addField("!" + getCommandByRealName("players") + "  (server)", conf.getString("commands.playerlist", "Display a list of Online players. (server) is optional."), false);
                    if(allowCommand(e.getAuthor(), "plugins"))
                        eb.addField("!" + getCommandByRealName("plugins"), conf.getString("commands.pluginlist", "Display a list of all installed BungeeCord Plugins."), false);
                    if(allowCommand(e.getAuthor(), "ban"))
                        eb.addField("!" + getCommandByRealName("ban") + " (player) (reason)", conf.getString("commands.ban", "Ban (player) with the (reason). Ban is removed on next ProxyServer restart."), false);
                    if(allowCommand(e.getAuthor(), "unban"))
                        eb.addField("!" + getCommandByRealName("unban") + " (player)", conf.getString("commands.unban", "Unban a (player) who is banned with !ban."), false);
                    if(allowCommand(e.getAuthor(), "kick"))
                        eb.addField("!" + getCommandByRealName("kick") + " (player) (reason)", conf.getString("commands.kick", "Kick the (player) with (reason)"), false);
                    if(allowCommand(e.getAuthor(), "broadcast"))
                        eb.addField("!" + getCommandByRealName("broadcast") + " (message)", conf.getString("commands.broadcast", "broadcast the (message) in the Ingame chat."), false);
                    if(allowCommand(e.getAuthor(), "announce"))
                        eb.addField("!" + getCommandByRealName("announce") + " (message)", conf.getString("commands.announce", "Display a Screen Message on all online players."), false);
                    if(allowCommand(e.getAuthor(), "shutdown"))
                        eb.addField("!" + getCommandByRealName("shutdown") + " (seconds)", conf.getString("commands.shutdown", "Shutdown the ProxyServer in (seconds) with countdown."), false);
                    if(allowCommand(e.getAuthor(), "admin"))
                        eb.addField("!" + getCommandByRealName("admin") + " (discord user id)", conf.getString("commands.admin", "Toggle Discord Bot Admin by Discord UserId"), false);

                    if(conf.isString("serverinfo.name")) {
                        if(conf.isString("serverinfo.url")) {
                            eb.setTitle(conf.getString("serverinfo.name"), conf.getString("serverinfo.url"));
                        } else {
                            eb.setTitle(conf.getString("serverinfo.name"));
                        }
                    } else {
                        eb.setTitle(ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMotd());
                    }

                    if(!conf.getString("commandlist.image", "").isEmpty())
                        eb.setImage(conf.getString("commandlist.image"));

                    if(!conf.getString("commandlist.thumbnail", "").isEmpty())
                        eb.setThumbnail(conf.getString("commandlist.thumbnail"));

                    if(!conf.getString("commandlist.motd", "").isEmpty())
                        eb.setDescription(conf.getString("commandlist.motd"));

                    if(conf.isConfigurationSection("commandlist.color")) {
                        int rgbR = Math.min(255, conf.getInt("commandlist.color.r", 0));
                        int rgbG = Math.min(255, conf.getInt("commandlist.color.g", 255));
                        int rgbB = Math.min(255, conf.getInt("commandlist.color.b", 255));
                        eb.setColor(new Color(rgbR, rgbG, rgbB));
                    }

                    eb.setAuthor(MCD.getSelfUser().getName(), !conf.getString("commandlist.url", "").isEmpty()?conf.getString("commandlist.url"):null, MCD.getSelfUser().getAvatarUrl());
                    MCD.getTextChannelById(_channelId).sendMessage(eb.build()).queue();

            }
            e.getMessage().delete().queue();
            return;
        }
        McDiscord.sendMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
    }

    public void updateGame() {
        int maxPlayers = ServerUtils.getMaxPlayers();
        MCD.getPresence().setGame(Game.playing("Minecraft (" + (ProxyServer.getInstance().getOnlineCount() + "/" + maxPlayers) + ")"));
    }

    public void sendMessageToDiscord(String channelId, String txt, String name, String message, String server) {
        MCD.getTextChannelById(channelId).sendMessage(ReplaceAll(txt, name, message, server)).queue();
    }

    public void sendTimeMessageToDiscord(String channelId, String txt, String name, String message, String server) {
        try {
            Consumer<Message> callback = (response) -> ProxyServer.getInstance().getScheduler().schedule(McDiscord.getInstance(), new EveryTask.AutoDeleteMessage(channelId, response.getId()), 10, TimeUnit.SECONDS);
            TextChannel tch = MCD.getTextChannelById(channelId);
            tch.sendMessage(ReplaceAll(txt, name, message, server)).queue(callback);
        } catch(Exception ex) {
            McDiscord.getInstance().getLogger().log(Level.WARNING, "Error in sendTimeMessageToDiscord:", ex);
        }
    }

    public String getInviteCode() {
        TextChannel tch = MCD.getTextChannelById(_channelId);
        if(tch != null) {
            try {
                for (Invite inv : tch.getInvites().submit().get()) {
                    if (inv.getUses() < inv.getMaxUses())
                        return inv.getCode();
                }
            } catch(Exception ex) {}

            //No Invite found, create new one.
            InviteAction invAct = tch.createInvite();
            if (invAct != null) {
                invAct = invAct.setTemporary(false);
                invAct = invAct.setUnique(true);
                invAct = invAct.setMaxAge(24l, TimeUnit.HOURS);
                Invite inv = invAct.complete();
                if (inv != null)
                    return inv.getCode();
            }
        }
        return "No Invite Code :(";
    }

    private void CommandAdmin(User u, String[] a) {
        if(!McDiscord.isAdmin(u.getId())) {
            sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-permission", "You @%p has no permission to use a discord command."), u.getName(), "", "");
            return;
        }

        if(a.length < 2) {
            sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.need-args", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        User user = MCD.getUserById(a[1]);
        if(user == null) {
            sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.no-user", "@%p you have forget arguments for this command."), u.getName(), "", "");
            return;
        }

        if(McDiscord.toggleAdmin(user.getId())) {
            sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.admin-added", "@%p you have forget arguments for this command."), user.getName(), "", "");
        } else {
            sendTimeMessageToDiscord(McDiscord.getConfig().getString("discord.channelId.chat", ""), McDiscord.getConfig().getString("admin.admin-removed", "@%p you have forget arguments for this command."), user.getName(), "", "");
        }
    }

    private void getPluginList() {
        FileConfiguration conf = McDiscord.getConfig();
        EmbedBuilder eb = new EmbedBuilder();

        if(conf.isString("pluginlist.name")) {
            if(conf.isString("pluginlist.url")) {
                eb.setTitle(conf.getString("pluginlist.name"), conf.getString("pluginlist.url"));
            } else {
                eb.setTitle(conf.getString("pluginlist.name"));
            }
        } else {
            eb.setTitle(ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMotd());
        }

        if(!conf.getString("pluginlist.image", "").isEmpty())
            eb.setImage(conf.getString("pluginlist.image"));

        if(!conf.getString("pluginlist.thumbnail", "").isEmpty())
            eb.setThumbnail(conf.getString("pluginlist.thumbnail"));

        if(!conf.getString("pluginlist.motd", "").isEmpty())
            eb.setDescription(conf.getString("pluginlist.motd"));

        if(conf.isConfigurationSection("pluginlist.color")) {
            int rgbR = Math.min(255, conf.getInt("pluginlist.color.r", 0));
            int rgbG = Math.min(255, conf.getInt("pluginlist.color.g", 255));
            int rgbB = Math.min(255, conf.getInt("pluginlist.color.b", 255));
            eb.setColor(new Color(rgbR, rgbG, rgbB));
        }

        for(Plugin p: ProxyServer.getInstance().getPluginManager().getPlugins()) {
            eb.addField(ReplaceAll(conf.getString("pluginlist.header", "%name by %author"), p), ReplaceAll(conf.getString("pluginlist.field", "%version | %description"), p), conf.getBoolean("embeds.pluginlist.plugin"));
        }

        eb.setAuthor(MCD.getSelfUser().getName(), !conf.getString("pluginlist.url", "").isEmpty()?conf.getString("pluginlist.url"):null, MCD.getSelfUser().getAvatarUrl());
        MCD.getTextChannelById(_channelId).sendMessage(eb.build()).queue();
    }

    private void ServerInfo() {
        FileConfiguration conf = McDiscord.getConfig();
        EmbedBuilder eb = new EmbedBuilder();
        int pluginAmount = ProxyServer.getInstance().getPluginManager().getPlugins().size();
        String onlineSince = McDiscord.OnlineSince();
        int servers = ProxyServer.getInstance().getServers().size();
        int pO = ProxyServer.getInstance().getOnlineCount();
        int pT = ServerUtils.getMaxPlayers();

        if(conf.isString("serverinfo.name")) {
            if(conf.isString("serverinfo.url")) {
                eb.setTitle(conf.getString("serverinfo.name"), conf.getString("serverinfo.url"));
            } else {
                eb.setTitle(conf.getString("serverinfo.name"));
            }
        } else {
            eb.setTitle(ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMotd());
        }

        for(String type: conf.getConfigurationSection("embeds.serverinfo").getKeys(false)) {
            switch(type.toLowerCase()) {
                case "players":
                    eb.addField(conf.getString("serverinfo.header.players", "Players"), ReplaceAll(conf.getString("serverinfo.players", "%p / %m"), String.valueOf(pO), String.valueOf(pT), ""), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "uptime":
                    eb.addField(conf.getString("serverinfo.header.uptime", "Uptime"), onlineSince, conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "servers":
                    String viewServer = String.valueOf(servers);
                    int online = ServerUtils.getOnlineServerCount();
                    if (online > -1)
                        viewServer = String.valueOf(online) + " / " + viewServer;
                    eb.addField(conf.getString("serverinfo.header.servers", "Servers"), viewServer, conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "serverlist":
                    List<String> onlineServers = ServerUtils.getOnlineServers();
                    if (onlineServers == null) {
                        McDiscord.getInstance().getLogger().info("Need Plugin ServerPing from Author Bl4ckSkull666 ( https://www.Bl4ckSkull666.de )");
                        break;
                    }
                    String serverlist = "";
                    for (String s : onlineServers) {
                        if (!serverlist.isEmpty())
                            serverlist += McDiscord.getConfig().getString("serverinfo.server-seperator", ", ");

                        serverlist += ServerUtils.getServerName(s);
                    }
                    eb.addField(conf.getString("serverinfo.header.serverlist", "Online Servers"), serverlist, conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "memory":
                    DecimalFormat df = new DecimalFormat("#.##");
                    Runtime rt = Runtime.getRuntime();
                    int using = (int) Rnd.bytesToMegabytes(rt.maxMemory() - rt.freeMemory());
                    int free = (int) Rnd.bytesToMegabytes(rt.freeMemory());
                    int max = (int) Rnd.bytesToMegabytes(rt.maxMemory());
                    String pc_in_use = df.format((Float.parseFloat("100") / Float.parseFloat(String.valueOf(max)) * Float.parseFloat(String.valueOf(using))));
                    String var = conf.getString("serverinfo.memory", "%use / %free / %max MB (%per %)");
                    var = var.replace("%use", String.valueOf(using));
                    var = var.replace("%free", String.valueOf(free));
                    var = var.replace("%max", String.valueOf(max));
                    var = var.replace("%per", pc_in_use);
                    eb.addField(conf.getString("serverinfo.header.memory", "Memory"), var, conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "client":
                    eb.addField(conf.getString("serverinfo.header.client", "Client Version"), conf.getString("serverinfo.client", "Version x.xx"), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "webseite":
                    eb.addField(conf.getString("serverinfo.header.website", "Website"), conf.getString("serverinfo.website", "https://www.spigotmc.org"), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "ip":
                    eb.addField(conf.getString("serverinfo.header.ip", "Server IP"), ServerUtils.getServerAddress(), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "address":
                    eb.addField(conf.getString("serverinfo.header.address", "Server IP"), conf.getString("serverinfo.address"), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "version":
                    eb.addField(conf.getString("serverinfo.header.version", "Proxy Version"), ProxyServer.getInstance().getVersion(), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "plugins":
                    eb.addField(conf.getString("serverinfo.header.plugins", "Plugins"), String.valueOf(pluginAmount), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                case "pings":
                    eb.addField(conf.getString("serverinfo.header.pings", "Pings (last 15 mins./Total)"), String.valueOf(Bungeecords.getLastPings()) + " / " + String.valueOf(Bungeecords.getTotalPing()), conf.getBoolean("embeds.serverinfo." + type));
                    break;
                default:
                    //do nothing
            }
        }

        if(!conf.getString("serverinfo.image", "").isEmpty())
            eb.setImage(conf.getString("serverinfo.image"));

        if(!conf.getString("serverinfo.thumbnail", "").isEmpty())
            eb.setThumbnail(conf.getString("serverinfo.thumbnail"));

        if(!conf.getString("serverinfo.motd", "").isEmpty())
            eb.setDescription(conf.getString("serverinfo.motd"));

        if(conf.isConfigurationSection("serverinfo.color")) {
            int rgbR = Math.min(255, conf.getInt("serverinfo.color.r", 0));
            int rgbG = Math.min(255, conf.getInt("serverinfo.color.g", 255));
            int rgbB = Math.min(255, conf.getInt("serverinfo.color.b", 255));
            eb.setColor(new Color(rgbR, rgbG, rgbB));
        }

        eb.setAuthor(MCD.getSelfUser().getName(), !conf.getString("serverinfo.url", "").isEmpty()?conf.getString("serverinfo.url"):null, MCD.getSelfUser().getAvatarUrl());
        MCD.getTextChannelById(_channelId).sendMessage(eb.build()).queue();
    }

    private void OnlinePlayers(User u, String[] a) {
        HashMap<String, List<ProxiedPlayer>> tmp = new HashMap<>();
        FileConfiguration conf = McDiscord.getConfig();
        EmbedBuilder eb = new EmbedBuilder();
        ServerInfo si = null;

        if(a.length > 1) {
            //display users on server
            for(Map.Entry<String, ServerInfo> me: ProxyServer.getInstance().getServers().entrySet()) {
                if(me.getKey().equalsIgnoreCase(a[1])) {
                    si = me.getValue();
                    break;
                }
            }

            if(si == null) {
                sendTimeMessageToDiscord(_channelId, conf.getString("playerlist.no-server", "Can't find the wished server %m"), u.getName(), a[1], "");
                return;
            }

            List<ProxiedPlayer> pList = new ArrayList<>();
            pList.addAll(si.getPlayers());
            tmp.put(si.getName(), pList);
        } else if(conf.getString("playerlist.display", "all").equalsIgnoreCase("group")) {
            for(ProxiedPlayer pp: ProxyServer.getInstance().getPlayers()) {
                for(String grp: pp.getGroups()) {
                    if (grp == null || grp.isEmpty())
                        grp = "default";

                    if (!tmp.containsKey(grp))
                        tmp.put(grp, new ArrayList<>());

                    tmp.get(grp).add(pp);
                    if(conf.getBoolean("playerlist.single-group", true))
                        break;
                }
            }

        } else if(conf.getString("playerlist.display", "all").equalsIgnoreCase("server")) {
            for(Map.Entry<String, ServerInfo> meSrv: ProxyServer.getInstance().getServers().entrySet()) {
                if(meSrv.getValue() == null)
                    continue;

                List<ProxiedPlayer> tmpPlayers = new ArrayList<>();
                tmpPlayers.addAll(meSrv.getValue().getPlayers());
                tmp.put(meSrv.getKey(), tmpPlayers);
            }
        } else {
            if(ProxyServer.getInstance().getOnlineCount() > 0) {
                List<ProxiedPlayer> tmpPlayers = new ArrayList<>();
                tmpPlayers.addAll(ProxyServer.getInstance().getPlayers());
                tmp.put("all", tmpPlayers);
            }
        }

        if(conf.isString("playerlist.name")) {
            if(conf.isString("playerlist.url")) {
                eb.setTitle(conf.getString("playerlist.name"), conf.getString("playerlist.url"));
            } else {
                eb.setTitle(conf.getString("playerlist.name"));
            }
        } else {
            eb.setTitle(ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMotd());
        }

        if(tmp.isEmpty()) {
            eb.addField(conf.getString("playerlist.header.no-players"),  conf.getString("playerlist.no-players", "There are currently no players on the server(s)... :sob: :sob: "), conf.getBoolean("embeds.playerlist.no-players"));
        } else {
            for(Map.Entry<String, List<ProxiedPlayer>> me: tmp.entrySet()) {
                String header = "";
                boolean inline = false;
                int limit = 0;
                if(conf.getString("playerlist.display", "all").equalsIgnoreCase("group")) {
                    header = conf.getString("playerlist.header.group", "Online Players in Group %group").replace("%group", ServerUtils.getGroupName(me.getKey()));
                    inline = conf.getBoolean("embeds.playerlist.group", true);
                    limit = conf.getInt("playerlist.max.group", 25);
                } else if(si != null || conf.getString("playerlist.display", "all").equalsIgnoreCase("server")) {
                    header = conf.getString("playerlist.header.server", "All Players on Server %srv").replace("%srv", ServerUtils.getServerName(me.getKey()));
                    inline = conf.getBoolean("embeds.playerlist.server", true);
                    limit = conf.getInt("playerlist.max.server", 25);
                } else {
                    header = conf.getString("playerlist.header.all", "Here are all online players");
                    inline = conf.getBoolean("embeds.playerlist.all", true);
                    limit = conf.getInt("playerlist.max.all", 100);
                }

                int i = 0;
                String playerList = "";
                for(ProxiedPlayer p: me.getValue()) {
                    if(!playerList.isEmpty())
                        playerList += conf.getString("playerlist.seperator", ", ");

                    playerList += p.getName();
                    i++;
                    if(i >= limit) {
                        playerList += conf.getString("playerlist.more-online", "... and %count more online.").replace("%count", String.valueOf(me.getValue().size()-i));
                        break;
                    }
                }
                eb.addField(header, playerList, inline);
            }
        }

        if(!conf.getString("playerlist.image", "").isEmpty())
            eb.setImage(conf.getString("playerlist.image"));

        if(!conf.getString("playerlist.thumbnail", "").isEmpty())
            eb.setThumbnail(conf.getString("playerlist.thumbnail"));

        if(!conf.getString("playerlist.motd", "").isEmpty())
            eb.setDescription(conf.getString("playerlist.motd"));

        if(conf.isConfigurationSection("playerlist.color")) {
            int rgbR = Math.min(255, conf.getInt("playerlist.color.r", 0));
            int rgbG = Math.min(255, conf.getInt("playerlist.color.g", 255));
            int rgbB = Math.min(255, conf.getInt("playerlist.color.b", 255));
            eb.setColor(new Color(rgbR, rgbG, rgbB));
        }

        eb.setAuthor(MCD.getSelfUser().getName(), !conf.getString("playerlist.url", "").isEmpty()?conf.getString("playerlist.url"):null, MCD.getSelfUser().getAvatarUrl());
        MCD.getTextChannelById(_channelId).sendMessage(eb.build()).queue();
    }

    private String ReplaceAll(String txt, String name, String msg, String server) {
        txt = txt.replace("%p", name);
        txt = txt.replace("%m", msg);
        return txt.replace("%s", server);
    }

    private String ReplaceAll(String str, Plugin p) {
        String auth = p.getDescription().getAuthor() != null && !p.getDescription().getAuthor().isEmpty()?p.getDescription().getAuthor():"Unknown Author";
        String name = p.getDescription().getName() != null && !p.getDescription().getName().isEmpty()?p.getDescription().getName():p.getDescription().getMain() != null && !p.getDescription().getMain().isEmpty()?p.getDescription().getMain():"Unknown Name";
        String desc = p.getDescription().getDescription() != null && !p.getDescription().getDescription().isEmpty()?p.getDescription().getDescription():"Unknown Description";
        String version = p.getDescription().getVersion() != null && !p.getDescription().getVersion().isEmpty()?p.getDescription().getVersion():"Unknown Version";

        str = str.replace("%author", auth);
        str = str.replace("%name", name);
        str = str.replace("%description", desc);
        str = str.replace("%version", version);
        return str;
    }

    private boolean allowCommand(User u, String cmd) {
        if(McDiscord.getConfig().getBoolean("only-admin." + cmd, false)) {
            if(McDiscord.isAdmin(u.getId()))
                return true;
            return false;
        }
        return true;
    }

    private String getCommandByRealName(String name) {
        for(String k : McDiscord.getConfig().getConfigurationSection("command-name").getKeys(false)) {
            if(McDiscord.getConfig().getString("command-name." + k).equalsIgnoreCase(name))
                return k;
        }
        return name;
    }

    public static JDA getInstance() {
        return MCD;
    }
}
