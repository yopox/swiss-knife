package fr.yopox.swiss_knife

import fr.yopox.swiss_knife.Values.Companion.toBitSet
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.*

class LHTag : Tag() {
    override val name = "[Tag]"
    override val privateKey = sha256("1010".toBitSet())
    override val ID = "111".toBitSet()

    private val socket: Socket
    private val writer: ObjectOutputStream
    private val reader: ObjectInputStream

    init {
        socket = Socket("localhost", 9990)
        writer = ObjectOutputStream(socket.getOutputStream())
        reader = ObjectInputStream(socket.getInputStream())
    }

    override fun genNB(): BitSet = sha256("11011001".toBitSet())

    override fun f_x(private: BitSet, b: BitSet): BitSet = sha256(Values.join(sha256(Values.join(b, private)), private))

    override fun receive1(): Pair<BitSet, BitSet> {
        return reader.readObject() as Pair<BitSet, BitSet>
    }

    override fun send1(N_B: BitSet) {
        writer.writeObject(N_B)
    }

    override fun receive2(): Boolean {
        return reader.readObject() as Boolean
    }

    override fun send2(r_i: Boolean) {
        writer.writeObject(r_i)
    }

    override fun sync2() {
        reader.readObject()
    }

    override fun send3(value: Pair<BitSet, BitSet>) {
        writer.writeObject(value)
    }

    override fun receive3(): BitSet {
        val b = reader.readObject() as BitSet
        socket.close()
        return b
    }
}