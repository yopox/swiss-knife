package fr.yopox.swiss_knife

import fr.yopox.swiss_knife.Values.Companion.toBitSet
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class LHReader(seed: Int) : Reader(seed) {

    override val name = "[Reader]"
    override val hashSize = 256

    private val db = arrayOf(
            "001".toBitSet() to sha256("1101".toBitSet()),
            "111".toBitSet() to sha256("1010".toBitSet())
    )

    private val socket: Socket
    private val writer: ObjectOutputStream
    private val reader: ObjectInputStream

    init {
        val s = ServerSocket(9990)
        socket = s.accept()
        log("Socket accepted.")
        reader = ObjectInputStream(socket.getInputStream())
        writer = ObjectOutputStream(socket.getOutputStream())
    }

    override fun dbSearch(tB: BitSet, cpI: BitSet, nA: BitSet, nB: BitSet): Pair<BitSet, BitSet>? {
        for ((id, private) in db)
            if (f_x(private, Values.join(cpI, id, nA, nB)) == tB)
                return id to private
        return null
    }

    override fun tagNotFound() {

    }

    override fun genNA(): BitSet = sha256("11010001".toBitSet())

    override fun f_x(private: BitSet, b: BitSet) = sha256(Values.join(sha256(Values.join(b, private)), private))

    override fun accept(ID: BitSet) {
    }

    override fun reject(ID: BitSet) {
    }

    override fun send1(value: Pair<BitSet, BitSet>) {
        writer.writeObject(value)
    }

    override fun receive1(): BitSet {
        return reader.readObject() as BitSet
    }

    override fun sync2() {
        writer.writeObject(1)
    }

    override fun send2(value: Boolean) {
        writer.writeObject(value)
    }

    override fun receive2(): Boolean {
        return reader.readObject() as Boolean
    }

    override fun receive3(): Pair<BitSet, BitSet> {
        return reader.readObject() as Pair<BitSet, BitSet>
    }

    override fun send3(value: BitSet) {
        writer.writeObject(value)
        socket.close()
    }
}