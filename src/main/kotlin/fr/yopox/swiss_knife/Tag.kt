package fr.yopox.swiss_knife

import java.util.*

abstract class Tag {

    abstract val name: String
    private var values = Values()
    abstract val privateKey: BitSet
    abstract val ID: BitSet

    fun start() {
        values = Values()
        slowPhase()
        rapidPhase()
        endPhase()
    }

    private fun slowPhase() {
        log("Starting slow phase.")

        // Waits for N_A, d
        val pair = receive1()
        values.N_A = pair.first
        values.d = pair.second

        // N_B computation
        values.N_B = genNB()
        log("N_B :\t${Values.bitSetToStr(values.N_B)}")

        // a computation
        val a = f_x(privateKey, Values.join(arrayOf(Values.C_B, values.N_B)))
        log("a :\t${Values.bitSetToStr(a)}")

        // R computation
        values.computeR(a, privateKey)
        log("R0 :\t${Values.bitSetToStr(values.R0).takeLast(Values.m)}")
        log("R1 :\t${Values.bitSetToStr(values.R1).takeLast(Values.m)}")

        // Send N_B
        log("Sending N_B")
        send1(values.N_B)
    }

    private fun rapidPhase() {
        log("Starting rapid phase.")

        // TODO: 1 Fake exchange ([send2] is slower the first time it is called ???)

        for (i in 0 until Values.m) {

            sync2()

            // Receive c'I
            val cpI = receive2()
            if (cpI) values.c1.set(i)

            sync2()

            // Send rI
            if (cpI) send2(values.R1[i]) else send2(values.R0[i])
        }
    }

    abstract fun sync2()

    private fun endPhase() {
        log("Starting end phase.")

        // tB computation
        val tB = f_x(privateKey, Values.join(arrayOf(values.c1, ID, values.N_A, values.N_B)))
        send3(Pair(tB, values.c1))

        // Receive tA
        val tA = receive3()
        if (tA == f_x(privateKey, values.N_B)) {
            // Reader verified
            log("Reader verified.")
        } else {
            // Reader not verified
            log("Reader not verified.")
        }
    }

    open fun log(s: String) = println("$name $s")

    abstract fun genNB(): BitSet
    abstract fun f_x(private: BitSet, b: BitSet): BitSet

    abstract fun receive1(): Pair<BitSet, BitSet>
    abstract fun send1(N_B: BitSet)

    abstract fun receive2(): Boolean
    abstract fun send2(r_i: Boolean)

    abstract fun send3(value: Pair<BitSet, BitSet>)
    abstract fun receive3(): BitSet

}