import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        if (config.getBoolean("enabled", false)) {
            server.pluginManager.registerEvents(EventHandlers(config), this)
            server.getPluginCommand("menu")?.setExecutor { sender, command, label, args ->
                if (sender is Player) {
                    sender.openInventory(StartMenuInventoryHolder(config).inventory)
                    true
                } else {
                    false
                }
            }
        }
    }
}

class EventHandlers(
    private val config: ConfigurationSection
) : Listener {

    @EventHandler
    fun onInventoryClick(ev: InventoryClickEvent) {
        val iv = ev.clickedInventory ?: return
        if (iv.holder is StartMenuInventoryHolder) {
            val clicker = ev.whoClicked as? Player ?: return

            val item = ev.currentItem ?: return
            ev.isCancelled = true

            when (item.type) {
                Material.BARRIER -> clicker.health = 0.0

                Material.FIREWORK_ROCKET -> {
                    val vec = clicker.velocity
                    vec.y += config.getDouble("liftoff.velocity", 5.0)
                    clicker.velocity = vec
                }

                Material.COMPASS ->
                    clicker.sendMessage(Component.text("Ping: ${clicker.ping}ms"))

                Material.COMMAND_BLOCK ->
                    clicker.performCommand(config.getString("command.run", "help")!!)

                else -> {}
            }

            iv.close()
        }
    }
}

class StartMenuInventoryHolder(config: ConfigurationSection) : InventoryHolder {
    private val inventory = Bukkit.createInventory(this, 1 * 9, Component.text("开始"))

    init {
        inventory.addItem(
            makeButton(Material.BARRIER, "重新部署"),
            makeButton(Material.FIREWORK_ROCKET, "快速起飞"),
            makeButton(Material.COMPASS, "查询延迟"),
            makeButton(Material.COMMAND_BLOCK, config.getString("command.label", "执行命令")!!)
        )
    }

    override fun getInventory(): Inventory = inventory
}

fun makeButton(mat: Material, label: String): ItemStack {
    val item = ItemStack(mat)
    val meta = item.itemMeta
    meta.customName(Component.text(label))
    item.itemMeta = meta
    return item
}