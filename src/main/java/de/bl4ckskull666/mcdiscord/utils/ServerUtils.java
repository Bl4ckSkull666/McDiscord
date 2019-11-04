package de.bl4ckskull666.mcdiscord.utils;

import codecrafter47.bungeetablistplus.BungeePlugin;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.util.PingTask;
import de.bl4ckskull666.mcdiscord.McDiscord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import de.bl4ckskull666.serverping.SP;

import java.util.ArrayList;
import java.util.List;

public class ServerUtils {
    public static int getOnlineServerCount() {
        int online = 0;
        Plugin p = ProxyServer.getInstance().getPluginManager().getPlugin("ServerPing");
        if (p == null || !(p instanceof SP))
            return -1;

        for (String s : ProxyServer.getInstance().getServers().keySet()) {
            if (SP.getStatus(s))
                online++;
        }
        return online;
    }

    public static List<String> getOnlineServers() {
        List<String> online = new ArrayList<>();
        Plugin p = ProxyServer.getInstance().getPluginManager().getPlugin("BungeeTabListPlus");
        if(p != null && p instanceof BungeePlugin) {
            for (String s : ProxyServer.getInstance().getServers().keySet()) {
                PingTask pt = BungeeTabListPlus.getInstance().getServerState(s);
                if(pt.isOnline())
                    online.add(s);
            }
            return online;
        }

        Plugin p2 = ProxyServer.getInstance().getPluginManager().getPlugin("ServerPing");
        if (p2 != null || p2 instanceof SP) {
            for (String s : ProxyServer.getInstance().getServers().keySet()) {
                if (SP.getStatus(s))
                    online.add(s);
            }
            return online;
        }
        return online;
    }

    public static int getMaxPlayers() {
        int maxPlayers = 0;
        for(ListenerInfo linfo: ProxyServer.getInstance().getConfig().getListeners()) {
            if(linfo.getMaxPlayers() > maxPlayers) {
                maxPlayers = linfo.getMaxPlayers();
            }
        }

        if(maxPlayers == 0) {
            maxPlayers = ProxyServer.getInstance().getConfig().getPlayerLimit();
        }
        return maxPlayers;
    }

    public static String getServerName(String srv) {
        return McDiscord.getConfig().getString("servers." + srv.toLowerCase(), srv);
    }

    public static String getGroupName(String grp) {
        return McDiscord.getConfig().getString("groups." + grp.toLowerCase(), grp);
    }

    public static String getServerAddress() {
        return ProxyServer.getInstance().getConfig().getListeners().iterator().next().getHost().getAddress().getHostAddress() + ":" + ProxyServer.getInstance().getConfig().getListeners().iterator().next().getHost().getPort();
    }
}
