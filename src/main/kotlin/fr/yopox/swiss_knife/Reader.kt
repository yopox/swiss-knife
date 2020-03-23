package fr.yopox.swiss_knife

import fr.yopox.swiss_knife.Values.Companion.prettyPrint
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

abstract class Reader(seed: Int) {

    data class Status(val accepted: Boolean, val ID: BitSet? = null, val errors: Int = 0)

    abstract val name: String
    abstract val hashSize: Int
    private val random = Random(seed)
    var values = Values()
    var status: Status = Status(false)

    fun start(): Boolean {
        values = Values()
        slowPhase()
        rapidPhase()
        endPhase()
        return status.accepted
    }

    private fun slowPhase() {
        log("Starting slow phase.")

        // N_A computation
        values.N_A = genNA()
        log("N_A : ${Values.bitSetToStr(values.N_A)}")

        // m random positions
        var s: Set<Int> = setOf()
        while (s.size < Values.m)
            s = s.plus(random.nextInt(hashSize))
        values.d = BitSet(hashSize)
        s.forEach { values.d.set(it) }
        log("d : ${Values.bitSetToStr(values.d)}")

        // Send N_A and d
        log("Sending (N_A, d)")
        send1(Pair(values.N_A, values.d))

        // Receive N_B
        values.N_B = receive1()
        log("Received ${Values.bitSetToStr(values.N_B)}")
    }

    private inline fun <R> measure(block: () -> R): Pair<R, Long> {
        val start = System.nanoTime()
        val result = block()
        return result to (System.nanoTime() - start)
    }

    private fun rapidPhase() {
        log("Starting rapid phase.")

        for (i in 0 until Values.m) {

            sync2()

            // Send cI
            val cI = random.nextFloat() >= 0.5
            if (cI) values.c1.set(i)
            send2(cI)

            sync2()

            // Receive r'I
            val (rpI, dt) = measure { receive2() }
            if (rpI) values.c2.set(i)
            values.Dt[i] = dt
            log("Bit $i : ${dt}ns\t${"%.1f".format(dt / 10.0.pow(9) * Values.speed)}m")
        }
    }

    abstract fun sync2()

    private fun endPhase() {
        log("Starting end phase.")

        // Receive tB & c'I
        val (tB, cpI) = receive3()


        // Check ID
        val (ID, privateKey) = dbSearch(tB, cpI, values.N_A, values.N_B) ?: return tagNotFound()

        // R computation
        val a = f_x(privateKey, Values.join(arrayOf(Values.C_B, values.N_B)))
        values.computeR(a, privateKey)
        log("R0 :\t${prettyPrint(values.R0)}")
        log("R1 :\t${prettyPrint(values.R1)}")
        log("C1 :\t${prettyPrint(values.c1)} (bits sent by the reader)")
        log("C'i:\t${prettyPrint(cpI)} (bits received by the tag)")
        log("C2 :\t${prettyPrint(values.c2)} (bits sent by the tag)")
        // Errors computation
        var errors = 0

        for (i in 0 until Values.m) {
            // First class errors : Wrong bit received
            if (cpI[i] != values.c1[i]) {
                log("Bit $i : wrong bit received")
                errors += 1
            } else {
                // Second class errors : Wrong response
                when (values.c1[i]) {
                    true -> if (values.c2[i] != values.R1[i]) {
                        log("Bit $i : wrong bit sent")
                        errors += 1
                    }
                    false -> if (values.c2[i] != values.R0[i]) {
                        log("Bit $i : wrong bit sent")
                        errors += 1
                    }
                }

                // Third class errors : Response too late
                if (values.Dt[i] >= Values.tMax) {
                    log("Bit $i : too late")
                    errors += 1
                }
            }
        }

        // Accept or reject
        if (errors < Values.T) {
            // Tag accepted
            log("Tag accepted with $errors errors.")
            accept(ID)
            status = Status(true, ID, errors)
        } else {
            // Tag rejected
            log("Tag rejected with $errors errors.")
            reject(ID)
            status = Status(false, ID, errors)
        }

        // Send tA
        val tA = f_x(privateKey, values.N_B)
        send3(tA)

    }

    open fun log(s: String) = println("$name $s")

    abstract fun genNA(): BitSet
    abstract fun dbSearch(tB: BitSet, cpI: BitSet, nA: BitSet, nB: BitSet): Pair<BitSet, BitSet>?
    abstract fun tagNotFound()
    abstract fun f_x(private: BitSet, b: BitSet): BitSet

    abstract fun accept(ID: BitSet)
    abstract fun reject(ID: BitSet)

    abstract fun send1(value: Pair<BitSet, BitSet>)
    abstract fun receive1(): BitSet

    abstract fun send2(value: Boolean)
    abstract fun receive2(): Boolean

    abstract fun receive3(): Pair<BitSet, BitSet>
    abstract fun send3(value: BitSet)

}