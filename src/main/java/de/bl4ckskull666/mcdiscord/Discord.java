package de.bl4ckskull666.mcdiscord;

import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class Discord extends ListenerAdapter {
    static JDA MCD;
    private String _channelId;
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

        if(McDiscord.isAdmin(e.getAuthor().getId()) && e.getMessage().getContentStripped().startsWith("!")) {
            String[] a = e.getMessage().getContentStripped().split(" ");
            switch(a[0].toLowerCase()) {
                case "!ban":
                    Bungeecords.BanPlayer(e.getAuthor(), a);
                    break;
                case "!unban":
                    Bungeecords.UnBanPlayer(e.getAuthor(), a);
                    break;
                case "!kick":
                    Bungeecords.KickPlayer(e.getAuthor(), a);
                    break;
                case "!broadcast":
                    Bungeecords.BroadcastMessage(e.getAuthor(), a);
                    break;
                case "!announce":
                    Bungeecords.AnnounceMessage(e.getAuthor(), a);
                    break;
                case "!shutdown":
                    Bungeecords.ShutdownProxy(e.getAuthor(), a);
                    break;
                case "!admin":
                    CommandAdmin(e.getAuthor(), a);
                    break;
                default:
                    String txt = "*Available Commands:*\r\n" +
                            "!ban (playername) (reason)\r\n" +
                            "!unban (playername)\r\n" +
                            "!kick (playername) (reason)\r\n" +
                            "!broadcast (message)\r\n" +
                            "!announce (message)\r\n" +
                            "!shutdown (seconds)\r\n" +
                            "!admin (discord user id)";
                    sendTimeMessageToDiscord(_channelId, txt, "", "", "");
            }
            e.getMessage().delete().queue();
            return;
        }

        McDiscord.sendMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
    }

    public void updateGame() {
        int maxPlayers = 0;
        for(ListenerInfo linfo: ProxyServer.getInstance().getConfig().getListeners()) {
            if(linfo.getMaxPlayers() > maxPlayers) {
                maxPlayers = linfo.getMaxPlayers();
            }
        }

        if(maxPlayers == 0) {
            maxPlayers = ProxyServer.getInstance().getConfig().getPlayerLimit();
        }
        MCD.getPresence().setGame(Game.playing("Minecraft (" + (ProxyServer.getInstance().getOnlineCount() + "/" + maxPlayers) + ")"));
    }

    public void sendMessageToDiscord(String channelId, String txt, String name, String message, String server) {
        MCD.getTextChannelById(channelId).sendMessage(ReplaceAll(txt, name, message, server)).queue();
    }

    public void sendTimeMessageToDiscord(String channelId, String txt, String name, String message, String server) {
        try {
            Consumer<Message> callback = (response) -> ProxyServer.getInstance().getScheduler().schedule(McDiscord.getInstance(), new EveryTask.AutoDeleteMessage(channelId, response.getId()), 30, TimeUnit.SECONDS);
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

    private String ReplaceAll(String txt, String name, String msg, String server) {
        txt = txt.replace("%p", name);
        txt = txt.replace("%m", msg);
        return txt.replace("%s", server);
    }

    public static JDA getInstance() {
        return MCD;
    }
}
