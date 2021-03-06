package fr.yopox.swiss_knife

import java.util.*
import kotlin.math.pow

class Values {

    companion object {
        // Protocol parameters
        var m = 8 // Rounds nb
        var T = 2 // Errors allowed
        var speed = 300 // Rapid phase speed in m/s

        // System constant
        val C_B = "1001010010010100".toBitSet()

        // Maximum reception length allowed
        val tMax = 10.0.pow(9)

        fun join(vararg bitsets: BitSet): BitSet {
            var b1 = BitSet()
            for (b in bitsets) {
                // New BitSet of the correct size
                val b2 = BitSet(b.size() + b1.size())

                // b copy
                b2.or(b)

                // b1 copy
                for (i in 0 until b1.size())
                    if (b1[i])
                        b2.set(i + b.size())
                b1 = b2
            }
            return b1
        }

        fun String.toBitSet(): BitSet {
            val b = BitSet(length)
            for (i in indices)
                if (get(i) == '0')
                    b.set(lastIndex - i + 1)
            return b
        }

        val BitSet.fullString: String
            get() {
                var str = ""
                for (bit in 0 until size()) {
                    str = if (get(bit)) "1$str" else "0$str"
                }
                return str
            }

        val BitSet.usefulString: String
            get() {
                return this.fullString.takeLast(m).reversed()
            }
    }

    // Slow phase
    var N_A = BitSet()
    var N_B = BitSet()
    var d = BitSet()
    var R0 = BitSet(m)
    var R1 = BitSet(m)

    // Rapid phase
    var c1 = BitSet(m)
    var c2 = BitSet(m)
    var Dt = Array(m) { 0.toLong() }

    fun computeR(a: BitSet, privateKey: BitSet) {

        R0 = BitSet(m)
        R1 = BitSet(m)

        // Z computation
        val Z0: BitSet = a.clone() as BitSet
        val Z1: BitSet = a.clone() as BitSet
        Z1.xor(privateKey)

        // R computation
        var j = d.nextSetBit(0)
        for (i in 0 until m) {
            if (Z0[j]) R0.set(i)
            if (Z1[j]) R1.set(i)
            j = d.nextSetBit(j + 1)
        }
    }

}