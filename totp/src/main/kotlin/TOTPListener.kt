import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Image
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class TOTPListener(
    plugin: Main
) : Listener {
    private val secretsFile = File(plugin.dataFolder, "secrets.yml")
    private val secretsData = YamlConfiguration.loadConfiguration(secretsFile)
    private val issuer = plugin.config.getString("issuer", "Minecraft Server")!!

    private val authenticatedPlayers = HashSet<UUID>()

    init {
        Bukkit.getPluginCommand("totp")?.setExecutor { sender, command, label, args ->
            if (sender is Player) {
                val code = args.firstOrNull()
                if (code == null) {
                    // Enable
                    if (secretsData.contains(sender.uniqueId.toString())) {
                        sender.sendMessage(Component.text("请提供 TOTP 验证码进行验证。"))
                    } else {
                        val secret = DefaultSecretGenerator().generate()
                        secretsData.set(sender.uniqueId.toString(), secret)
                        val image = createImageForSecret(secret, sender.name, issuer)
                        val item = createImageMapItem(image)
                        sender.inventory.addItem(item)
                        sender.sendMessage(Component.text("包含 TOTP 二维码的地图已加入你的物品栏，请使用验证器扫描其中的二维码。"))
                    }

                } else {
                    // Check
                    val secret = secretsData.getString(sender.uniqueId.toString())

                    if (secret == null) {
                        sender.sendMessage(Component.text("若要启用 TOTP，请使用不含参数的 /totp 命令。"))
                    } else {
                        val verifier = DefaultCodeVerifier(DefaultCodeGenerator(), SystemTimeProvider())
                        if (verifier.isValidCode(secret, code)) {
                            authenticatedPlayers.add(sender.uniqueId)
                            sender.sendMessage(Component.text("TOTP 认证成功。"))
                        } else {
                            sender.kick(Component.text("对不起，请重试。"))
                        }
                    }
                }
            }
            true
        }
    }

    @EventHandler
    fun onPlayerQuit(ev: PlayerQuitEvent) {
        authenticatedPlayers.remove(ev.player.uniqueId)
    }

    @EventHandler
    fun onPlayerCommand(ev: PlayerCommandPreprocessEvent) {
        val commandName = ev.message.drop(1).takeWhile { it != ' ' }

        if (!authenticatedPlayers.contains(ev.player.uniqueId) && commandName != "totp") {
            ev.isCancelled = true
            ev.player.sendMessage(Component.text("您必须经过 TOTP 验证，才能使用命令。"))
        }
    }

    fun saveSecrets() {
        secretsData.save(secretsFile)
    }
}

private fun createImageForSecret(secret: String, name: String, issuer: String): Image {
    val qrCode = QrData.Builder()
        .secret(secret)
        .label(name)
        .issuer(issuer)
        .build()

    val imgData = ZxingPngQrGenerator().generate(qrCode)
    val image =
        ImageIO.read(ByteArrayInputStream(imgData)).getScaledInstance(128, 128, Image.SCALE_FAST)

    return image
}

private fun createImageMapItem(img: Image): ItemStack {
    val item = ItemStack(Material.FILLED_MAP)
    item.editMeta {
        it.customName(Component.text("TOTP 验证码"))
        val mv = Bukkit.createMap(Bukkit.getWorlds().first())
        (it as MapMeta).run {
            mapView = mv
            mapView!!.renderers.forEach { mapView!!.removeRenderer(it) }
            mapView!!.addRenderer(
                object : MapRenderer() {
                    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
                        canvas.drawImage(0, 0, img)
                    }
                }
            )
        }
    }

    return item
}