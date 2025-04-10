import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.*

private class BackpackInventoryHolder(size: Int, title: String) : InventoryHolder {
    private val inv = Bukkit.createInventory(this, size, Component.text(title))

    override fun getInventory() = inv
}

class BackpackListener(
    config: ConfigurationSection,
    private val bpMap: MutableMap<UUID, ByteArray>
) : Listener {
    private val backpacks = HashMap<UUID, BackpackInventoryHolder>()
    private val size = config.getInt("size", 9)
    private val title = config.getString("title", "旅行背包")!!

    init {
        Bukkit.getPluginCommand("backpack")?.setExecutor { sender, command, label, args ->
            if (sender is Player) {
                val inv = backpacks.computeIfAbsent(sender.uniqueId) {
                    val holder = BackpackInventoryHolder(size, title)

                    val dat = bpMap[sender.uniqueId]

                    if (dat != null) {
                        holder.inventory.storageContents =
                            ItemStack.deserializeItemsFromBytes(dat).take(size).toTypedArray()
                    }

                    holder
                }

                sender.openInventory(inv.inventory)
            }
            true
        }
    }

    @EventHandler
    fun onPlayerLeave(ev: PlayerQuitEvent) {
        val holder = backpacks[ev.player.uniqueId] ?: return
        bpMap[ev.player.uniqueId] = ItemStack.serializeItemsAsBytes(holder.inventory.storageContents)
        backpacks.remove(ev.player.uniqueId)
    }

    fun saveBackpacks() {
        backpacks.forEach { (uuid, holder) ->
            bpMap[uuid] = ItemStack.serializeItemsAsBytes(holder.inventory.storageContents)
        }
    }
}