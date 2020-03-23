package fr.yopox.swiss_knife

import java.security.MessageDigest
import java.util.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Reader thread.
 */
class T1 : Thread() {

    private var result = false

    override fun run() {
        val reader = LHReader((0..999999).random())
        result = reader.start()
    }

    fun result(): Boolean {
        return result
    }
}

/**
 * Tag thread.
 */
class T2 : Thread() {
    override fun run() {
        val tag = LHTag()
        tag.start()
    }
}

class LocalhostTest {

    init {
        // Wi-Fi speed
        Values.speed = 299792458
    }

    /**
     * Swiss-knife between two threads using a [Socket] on port 9990 to communicate
     * during the three phases.
     */
    @Test fun localhost() {
        val reader = T1()
        val tag = T2()

        reader.start()
        Thread.sleep(100)
        tag.start()

        reader.join()

        assertTrue { reader.result() }
    }

}

fun sha256(bs: BitSet): BitSet {
    val crypt = MessageDigest.getInstance("SHA-256")
    crypt.reset()
    return BitSet.valueOf(crypt.digest(bs.toByteArray()))
}