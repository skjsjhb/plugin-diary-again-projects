import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        val isEnabled = config.getBoolean("enabled", false)
        val content = config.getString("content", "(Announcement Here)") as String

        val eventHandlers = object : Listener {
            @EventHandler
            fun onPlayerJoin(ev: PlayerJoinEvent) {
                val msg = Component.text(content)
                ev.player.sendMessage(msg)
            }
        }

        if (isEnabled) {
            server.pluginManager.registerEvents(eventHandlers, this)
        }
    }
}
