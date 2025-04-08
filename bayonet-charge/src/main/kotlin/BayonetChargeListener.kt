import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class BayonetChargeListener(config: ConfigurationSection) : Listener {
    private val chargingItems = config.getStringList("items").toSet()
    private val maxDistance = config.getDouble("max-distance", 30.0)
    private val killMsg = config.getString("kill-msg", "")!!
    private val slownessDuration = config.getInt("slowness-duration", 100)
    private val slownessAmplifier = config.getInt("slowness-amplifier", 3)
    private val speedAmplifier = config.getInt("speed-amplifier", 2)
    private val barTitle = config.getString("title", "刺刀冲锋")!!

    private val chargingBar = HashMap<UUID, BossBar>()

    @EventHandler
    fun onBeginCharge(ev: PlayerInteractEvent) {
        if (ev.action != Action.RIGHT_CLICK_AIR) return
        if (!ev.player.isSprinting) return
        if (ev.player.hasPotionEffect(PotionEffectType.SLOWNESS)) return
        if (!chargingItems.contains(ev.item?.type?.key?.toString())) return
        if (chargingBar.containsKey(ev.player.uniqueId)) return

        ev.player.addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED,
                PotionEffect.INFINITE_DURATION,
                speedAmplifier
            )
        )

        val bossBar = BossBar.bossBar(
            Component.text(barTitle),
            1f,
            BossBar.Color.RED,
            BossBar.Overlay.PROGRESS
        )

        ev.player.showBossBar(bossBar)
        chargingBar[ev.player.uniqueId] = bossBar
    }

    @EventHandler
    fun onChargeUpdate(ev: PlayerMoveEvent) {
        val bb = chargingBar[ev.player.uniqueId] ?: return

        fun endCharge() {
            ev.player.removePotionEffect(PotionEffectType.SPEED)
            ev.player.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SLOWNESS,
                    (slownessDuration * (1 - bb.progress())).toInt(),
                    slownessAmplifier
                )
            )
            ev.player.hideBossBar(bb)
            chargingBar.remove(ev.player.uniqueId)
        }

        if (!ev.player.isSprinting) {
            endCharge()
            return
        }

        val entities = ev.player.getNearbyEntities(0.5, 0.5, 0.5)
        val target = entities.find { it is LivingEntity } as LivingEntity?
        if (target != null) {
            ev.player.attack(target)
            target.health = 0.0
            ev.player.sendMessage(Component.text(killMsg.replace("{name}", target.name)))
            endCharge()
            return
        }

        val dis = ev.from.distance(ev.to)
        val remainingBlocks = bb.progress() * maxDistance

        if (remainingBlocks <= dis) {
            endCharge()
            return
        }

        bb.progress(((remainingBlocks - dis) / maxDistance).toFloat())
    }
}