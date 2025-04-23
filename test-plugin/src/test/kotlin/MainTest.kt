import moe.skjsjhb.mc.plugins.test.Main
import org.mockbukkit.mockbukkit.MockBukkit
import kotlin.test.Test
import kotlin.test.assertEquals

val server = MockBukkit.mock()
val plugin = MockBukkit.loadSimple(Main::class.java)

class MainTest {
    @Test
    fun testPlayerJoin() {
        val p = server.addPlayer("ThatRarityEG")
        assertEquals("Hello, THATRARITYEG!!!", p.nextMessage())
    }

    @Test
    fun testPlayerQuit() {
        val p = server.addPlayer("HIM")
        p.disconnect()
        p.nextMessage()
        assertEquals("Bye, HIM", p.nextMessage())
    }
}