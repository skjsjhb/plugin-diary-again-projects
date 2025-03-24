import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

object PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        e.player.kick(Component.text("试吃结束"))
    }
}

class Main : JavaPlugin() {
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(PlayerJoinListener, this);
    }
}