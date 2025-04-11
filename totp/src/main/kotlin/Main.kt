import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    private var listener: TOTPListener? = null

    override fun onEnable() {
        saveDefaultConfig()
        if (config.getBoolean("enabled")) {
            server.pluginManager.registerEvents(TOTPListener(this), this)
        }
    }

    override fun onDisable() {
        listener?.saveSecrets()
    }
}

