package com.example;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BungeeCordCheck extends JavaPlugin implements PluginMessageListener {

    private final Map<String, Player> requestMap = new HashMap<>();

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        getLogger().info("BungeeCordCheck 플러그인 활성화 완료");
    }

    public void requestPlayerServer(Player sender, String targetName) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeUTF("PlayerServer");
            out.writeUTF(targetName);
            sender.sendPluginMessage(this, "BungeeCord", byteOut.toByteArray());

            requestMap.put(targetName.toLowerCase(), sender); // 소문자로 통일

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestSelfServer(Player sender) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeUTF("GetServer");
            sender.sendPluginMessage(this, "BungeeCord", byteOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();

            if (subchannel.equals("PlayerServer")) {
                String targetPlayer = in.readUTF();
                String serverName = in.readUTF();

                Player requester = requestMap.remove(targetPlayer.toLowerCase());

                if (requester != null && requester.isOnline()) {
                    requester.sendMessage("§e[서버 확인] §f" + targetPlayer + " 님은 현재 §a" + serverName + "§f 서버에 접속 중입니다.");
                } else {
                    Bukkit.getLogger().warning("요청자를 찾을 수 없습니다: " + targetPlayer);
                }

                Bukkit.getLogger().info("[Bungee] " + targetPlayer + " is on server: " + serverName);
            }

            if (subchannel.equals("GetServer")) {
                String serverName = in.readUTF();
                player.sendMessage("§e[서버 확인] 당신은 현재 §a" + serverName + "§f 서버에 접속 중입니다.");
                Bukkit.getLogger().info("[Bungee] " + player.getName() + " is on server: " + serverName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("checkserver")) {
            if (args.length != 1) {
                player.sendMessage("§c사용법: /checkserver <플레이어명>");
                return true;
            }

            requestPlayerServer(player, args[0]);
            player.sendMessage(args[0] + " 의 서버 정보를 요청했습니다.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("whereami")) {
            requestSelfServer(player);
            return true;
        }

        return false;
    }
}
