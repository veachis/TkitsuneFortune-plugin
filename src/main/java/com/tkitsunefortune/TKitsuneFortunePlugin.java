package com.tkitsunefortune;

import com.tkitsunefortune.command.FortuneWheelCommand;
import com.tkitsunefortune.gui.AdminMenuListener;
import com.tkitsunefortune.gui.AdminMenuManager;
import com.tkitsunefortune.listener.WheelInteractionListener;
import com.tkitsunefortune.model.ModelLoader;
import com.tkitsunefortune.resourcepack.ResourcePackGenerator;
import com.tkitsunefortune.resourcepack.ResourcePackHostServer;
import com.tkitsunefortune.ticket.TicketManager;
import com.tkitsunefortune.util.LanguageManager;
import com.tkitsunefortune.wheel.SliceManager;
import com.tkitsunefortune.wheel.WheelManager;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TKitsuneFortunePlugin extends JavaPlugin {

    private NamespacedKey wheelEntityKey;
    private LanguageManager languageManager;
    private ModelLoader modelLoader;
    private ResourcePackGenerator resourcePackGenerator;
    private ResourcePackHostServer resourcePackHostServer;
    private TicketManager ticketManager;
    private SliceManager sliceManager;
    private WheelManager wheelManager;
    private AdminMenuManager adminMenuManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.wheelEntityKey = new NamespacedKey(this, "wheel_id");
        this.languageManager = new LanguageManager(this);
        this.modelLoader = new ModelLoader(this);
        this.resourcePackGenerator = new ResourcePackGenerator(this);
        this.resourcePackHostServer = new ResourcePackHostServer(this);
        this.ticketManager = new TicketManager(this);
        this.sliceManager = new SliceManager(this);
        this.wheelManager = new WheelManager(this);
        this.adminMenuManager = new AdminMenuManager(this);

        this.modelLoader.loadModels();
        File packZip = this.resourcePackGenerator.generateResourcePack();
        this.resourcePackHostServer.start(packZip);
        this.sliceManager.loadSlices();
        this.wheelManager.loadWheels();
        this.ticketManager.refreshAllOnlinePlayerTickets();
        this.resourcePackHostServer.sendToAllOnlinePlayers();

        PluginCommand cmd = getCommand("tkf");
        if (cmd != null) {
            FortuneWheelCommand executor = new FortuneWheelCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getServer().getPluginManager().registerEvents(new WheelInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new AdminMenuListener(this), this);
        getLogger().info("TkitsuneFortune v" + getDescription().getVersion() + " enabled (/tkf ready for Spigot & Paper 1.21.11)!");
    }

    @Override
    public void onDisable() {
        if (this.resourcePackHostServer != null) {
            this.resourcePackHostServer.stop();
        }
        if (this.wheelManager != null) {
            this.wheelManager.despawnAll();
        }
    }

    public void reloadPluginState() {
        reloadConfig();
        this.modelLoader.loadModels();
        File packZip = this.resourcePackGenerator.generateResourcePack();
        this.resourcePackHostServer.start(packZip);
        this.sliceManager.loadSlices();
        this.wheelManager.loadWheels();
        this.ticketManager.refreshAllOnlinePlayerTickets();
        this.resourcePackHostServer.sendToAllOnlinePlayers();
    }

    public NamespacedKey getWheelEntityKey() {
        return wheelEntityKey;
    }

    public ModelLoader getModelLoader() {
        return modelLoader;
    }

    public ResourcePackGenerator getResourcePackGenerator() {
        return resourcePackGenerator;
    }

    public ResourcePackHostServer getResourcePackHostServer() {
        return resourcePackHostServer;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public SliceManager getSliceManager() {
        return sliceManager;
    }

    public WheelManager getWheelManager() {
        return wheelManager;
    }

    public AdminMenuManager getAdminMenuManager() {
        return adminMenuManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public boolean isEnglish() {
        return languageManager != null && languageManager.isEnglish();
    }

    public String tr(String trText, String enText) {
        return isEnglish() ? enText : trText;
    }
}
