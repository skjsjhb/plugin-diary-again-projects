import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        println("This is the Welcome Broadcast plugin.")
        server.pluginManager.registerEvents(EventHandlers, this)
    }
}

object EventHandlers : Listener {
    @EventHandler
    fun onPlayerJoin(ev: PlayerJoinEvent) {
        val msg = Component.text("Welcome ${ev.player.name} to our server!")
        ev.player.server.broadcast(msg)
    }
}