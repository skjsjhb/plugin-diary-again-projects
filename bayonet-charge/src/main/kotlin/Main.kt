import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        if (config.getBoolean("enabled")) {
            server.pluginManager.registerEvents(BayonetChargeListener(config), this)
        }
    }
}
