import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(EventHandlers(config), this)

        server.getPluginCommand("chunk-heat")?.setExecutor { sender, command, label, args ->
            if (sender.isOp) {
                when (args.firstOrNull()) {
                    "enable" -> config.set("enabled", true)
                    "disable" -> config.set("enabled", false)
                    else -> sender.sendMessage(Component.text("/chunk-heat: 指定的操作无效"))
                }
            } else {
                sender.sendMessage(Component.text("/chunk-heat: 权限不够"))
            }

            true
        }
    }
}

class EventHandlers(private val config: ConfigurationSection) : Listener {
    private val sessionId = UUID.randomUUID().toString()

    private val heatKey = NamespacedKey.fromString("chunk_heat:heat")!!
    private val lastOverheatTimeKey = NamespacedKey.fromString("chunk_heat:last_overheat")!!
    private val sessionIdKey = NamespacedKey.fromString("chunk_heat:session")!!

    private val entities = config.getStringList("mobs").toSet()

    @EventHandler
    fun onMobSpawn(ev: EntitySpawnEvent) {
        if (!config.getBoolean("enabled")) return

        if (!entities.contains(ev.entity.type.key.toString())) return

        val chunk = ev.entity.chunk

        val storedSessionId = chunk.persistentDataContainer.get(sessionIdKey, PersistentDataType.STRING) ?: ""
        if (storedSessionId != sessionId) {
            chunk.persistentDataContainer.remove(heatKey)
            chunk.persistentDataContainer.remove(lastOverheatTimeKey)
            chunk.persistentDataContainer.set(sessionIdKey, PersistentDataType.STRING, sessionId)
        }

        val prevHeat = chunk.persistentDataContainer.get(heatKey, PersistentDataType.INTEGER) ?: 0
        val lastOverheatTime = chunk.persistentDataContainer.get(lastOverheatTimeKey, PersistentDataType.INTEGER)

        if (lastOverheatTime != null && Bukkit.getCurrentTick() < lastOverheatTime + config.getInt("cooldown")) {
            ev.isCancelled = true
            return
        }

        val newHeat = prevHeat + 1
        if (newHeat >= config.getInt("overheat")) {
            chunk.persistentDataContainer.set(
                lastOverheatTimeKey,
                PersistentDataType.INTEGER,
                Bukkit.getCurrentTick()
            )
            chunk.persistentDataContainer.set(heatKey, PersistentDataType.INTEGER, 0)
        } else {
            chunk.persistentDataContainer.set(heatKey, PersistentDataType.INTEGER, newHeat)
        }
    }
}