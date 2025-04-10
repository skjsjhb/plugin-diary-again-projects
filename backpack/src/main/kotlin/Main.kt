import org.bukkit.plugin.java.JavaPlugin
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.io.File

class Main : JavaPlugin() {
    private var db: DB? = null
    private var listener: BackpackListener? = null

    override fun onEnable() {
        saveDefaultConfig()
        if (config.getBoolean("enabled")) {
            db = DBMaker.fileDB(File(dataFolder, "backpacks.db")).make()
            val bpMap = db!!.hashMap("backpacks", Serializer.UUID, Serializer.BYTE_ARRAY).createOrOpen()

            listener = BackpackListener(config, bpMap)
            server.pluginManager.registerEvents(listener!!, this)
        }
    }

    override fun onDisable() {
        listener?.saveBackpacks()
        db?.close()
    }
}