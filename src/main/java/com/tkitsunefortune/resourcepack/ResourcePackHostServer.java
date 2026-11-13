package com.tkitsunefortune.resourcepack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.tkitsunefortune.TKitsuneFortunePlugin;
import com.tkitsunefortune.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.Executors;

public class ResourcePackHostServer {

    private final TKitsuneFortunePlugin plugin;
    private HttpServer httpServer;
    private byte[] cachedZipBytes;
    private byte[] cachedSha1Bytes;
    private String cachedSha1Hex;
    private UUID packUuid;

    public ResourcePackHostServer(TKitsuneFortunePlugin plugin) {
        this.plugin = plugin;
    }

    public void start(File zipFile) {
        stop();
        reloadPackCache(zipFile);

        if (!plugin.getConfig().getBoolean("settings.resource-pack.auto-host", true)) {
            return;
        }

        int port = plugin.getConfig().getInt("settings.resource-pack.port", 8765);
        try {
            httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            httpServer.createContext("/TkitsuneFortune-ResourcePack.zip", this::handleRequest);
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.start();
            plugin.getLogger().info("Resource Pack HTTP Server started on port " + port + " (SHA-1: " + cachedSha1Hex + ")");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not bind Resource Pack HTTP server on port " + port + ": " + e.getMessage());
        }
    }

    public void reloadPackCache(File zipFile) {
        if (zipFile == null || !zipFile.exists()) {
            return;
        }
        try {
            this.cachedZipBytes = Files.readAllBytes(zipFile.toPath());
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            this.cachedSha1Bytes = md.digest(this.cachedZipBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : this.cachedSha1Bytes) {
                sb.append(String.format("%02x", b));
            }
            this.cachedSha1Hex = sb.toString();
            this.packUuid = UUID.nameUUIDFromBytes(("tkitsunefortune-" + cachedSha1Hex).getBytes());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to compute SHA-1 for Resource Pack: " + e.getMessage());
        }
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        if (cachedZipBytes == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", "application/zip");
        exchange.sendResponseHeaders(200, cachedZipBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(cachedZipBytes);
        }
    }

    public void sendResourcePackToPlayer(Player player) {
        if (player == null || !player.isOnline() || cachedSha1Bytes == null) {
            return;
        }
        if (!plugin.getConfig().getBoolean("settings.resource-pack.auto-send", true)) {
            return;
        }

        int port = plugin.getConfig().getInt("settings.resource-pack.port", 8765);
        String configuredHost = plugin.getConfig().getString("settings.resource-pack.public-host", "auto");
        String host = "127.0.0.1";
        if (configuredHost != null && !configuredHost.isBlank() && !configuredHost.equalsIgnoreCase("auto")) {
            host = configuredHost.trim();
        } else if (player.getAddress() != null && player.getAddress().getAddress() != null) {
            if (player.getAddress().getAddress().isLoopbackAddress()) {
                host = "127.0.0.1";
            } else {
                String serverIp = Bukkit.getIp();
                if (serverIp != null && !serverIp.isBlank()) {
                    host = serverIp;
                }
            }
        }

        String url = "http://" + host + ":" + port + "/TkitsuneFortune-ResourcePack.zip?hash=" + cachedSha1Hex;
        String prompt = ColorUtil.colorize("&6&lTkitsuneFortune &e3D Şans Çarkı ve Özel Bilet Dokuları");
        boolean force = plugin.getConfig().getBoolean("settings.resource-pack.force", false);

        try {
            player.removeResourcePacks();
        } catch (Throwable ignored) {
        }
        try {
            player.setResourcePack(packUuid, url, cachedSha1Bytes, prompt, force);
        } catch (NoSuchMethodError err) {
            player.setResourcePack(url, cachedSha1Bytes, prompt, force);
        }
    }

    public void sendToAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendResourcePackToPlayer(player);
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
}

