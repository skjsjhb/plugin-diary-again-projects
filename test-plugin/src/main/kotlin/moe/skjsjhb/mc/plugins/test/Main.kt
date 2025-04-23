package moe.skjsjhb.mc.plugins.test

import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

open class Main : JavaPlugin() {
    override fun onEnable() {
        val listener = object : Listener {
            @EventHandler
            fun onPlayerJoin(ev: PlayerJoinEvent) {
                ev.player.sendMessage(
                    Component.text(
                        sayHelloLoudly(ev.player.name)
                    )
                )
            }

            @EventHandler
            fun onPlayerQuit(ev: PlayerQuitEvent) {
                server.broadcast(
                    Component.text(
                        "Bye, ${ev.player.name}"
                    )
                )
            }
        }

        server.pluginManager.registerEvents(listener, this)
    }
}

fun sayHelloLoudly(name: String): String {
    return "Hello, ${name.uppercase()}!!!"
}
