package com.holokenmod.creation

import com.holokenmod.grid.GridSize
import com.holokenmod.options.GameOptionsVariant.Companion.createClassic
import com.holokenmod.options.GameVariant
import com.holokenmod.options.SingleCageUsage
import io.kotest.core.spec.style.FunSpec
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TestGridDifficultyCalculator : FunSpec({
    test("difficulty").config(invocations = 3) {
        val creator = GridCreator(
            GameVariant(
                GridSize(9, 9),
                createClassic()
            )
        )
        val grid = creator.createRandomizedGridWithCages()
        println(GridDifficultyCalculator(grid).calculate())
    }

    xtest("calculateValues") {
        val difficulties = Collections.synchronizedList(ArrayList<Double>())

        val options = createClassic()
        options.singleCageUsage = SingleCageUsage.NO_SINGLE_CAGES
        val creator = GridCalculator(
            GameVariant(
                GridSize(9, 9),
                options
            )
        )

        val pool = Executors.newFixedThreadPool(12)
        for (i in 0..999) {
            pool.submit {
                val grid = creator.calculate()
                difficulties.add(GridDifficultyCalculator(grid).calculate())
                print(".")
            }
        }
        try {
            pool.shutdown()
            pool.awaitTermination(1, TimeUnit.HOURS)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        difficulties.sort()
        println(difficulties.size)
        println("50: " + difficulties[49])
        println("333: " + difficulties[332])
        println("667: " + difficulties[666])
        println("950: " + difficulties[949])
    }
})
