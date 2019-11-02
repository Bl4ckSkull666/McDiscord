package de.bl4ckskull666.mcdiscord;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class EveryTask  {
    public static class AutoDeleteMessage implements Runnable {
        private final String _cId;
        private final String _msgId;

        public AutoDeleteMessage(String channelId, String msgId) {
            _cId = channelId;
            _msgId = msgId;
        }

        @Override
        public void run() {
            Discord.getInstance().getTextChannelById(_cId).deleteMessageById(_msgId).queue();
        }
    }

    public static class DoShutDown implements Runnable {
        private int _remaining;

        public DoShutDown(int remain) {
            _remaining = remain;
            ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("shutdown.start").replace("%t", String.valueOf(remain)))));
        }

        @Override
        public void run() {
            if(_remaining > 0) {
                if(McDiscord.getConfig().isString("shutdown." + _remaining)) {
                    ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("shutdown." + _remaining))));
                }
            } else if(_remaining == 0) {
                ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("shutdown.end"))));
                TextComponent kickMsg = new TextComponent(ChatColor.translateAlternateColorCodes('&', McDiscord.getConfig().getString("shutdown.kick")));
                for(ProxiedPlayer pp: ProxyServer.getInstance().getPlayers()) {
                    pp.disconnect(kickMsg);
                }
                ProxyServer.getInstance().stop();
            }
            _remaining--;
        }
    }

    public static class UpdateDiscordGame implements Runnable {
        @Override
        public void run() {
            McDiscord.getDiscord().updateGame();
        }
    }
}
