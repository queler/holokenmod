package com.holokenmod

import android.content.Context
import android.content.SharedPreferences
import com.holokenmod.Utils.convertTimetoStr
import com.holokenmod.grid.Grid
import java.util.*

class StatisticsManager(context: Context, private val grid: Grid) {
    private val stats: SharedPreferences

    init {
        stats = context.getSharedPreferences("stats", Context.MODE_PRIVATE)
    }

    fun storeStatisticsAfterNewGame() {
        val gamestat = stats
            .getInt("playedgames" + grid.gridSize, 0)
        val editor = stats.edit()
        editor.putInt("playedgames" + grid.gridSize, gamestat + 1)
        editor.commit()
    }

    fun storeStatisticsAfterFinishedGame(): Optional<String> {
        val gridsize = grid.gridSize

        // assess hint penalty - gridsize^2/2 seconds for each cell
        val penalty = grid.countCheated().toLong() * 500 * gridsize.surfaceArea
        grid.playTime = grid.playTime + penalty
        val solvetime = grid.playTime
        var solveStr = convertTimetoStr(solvetime)
        val hintedstat = stats.getInt("hintedgames$gridsize", 0)
        val solvedstat = stats.getInt("solvedgames$gridsize", 0)
        val timestat = stats.getLong("solvedtime$gridsize", 0)
        val totaltimestat = stats.getLong("totaltime$gridsize", 0)
        val editor = stats.edit()
        if (penalty != 0L) {
            editor.putInt("hintedgames$gridsize", hintedstat + 1)
            solveStr += "^"
        } else {
            editor.putInt("solvedgames$gridsize", solvedstat + 1)
        }
        editor.putLong("totaltime$gridsize", totaltimestat + solvetime)
        val recordTime: Optional<String> = if (timestat == 0L || timestat > solvetime) {
            editor.putLong("solvedtime$gridsize", solvetime)
            Optional.of<String>(solveStr)
        } else {
            Optional.empty<String>()
        }
        editor.commit()
        return recordTime
    }

    fun storeStreak(isSolved: Boolean) {
        val solved_streak = stats.getInt("solvedstreak", 0)
        val longest_streak = stats.getInt("longeststreak", 0)
        val editor = stats.edit()
        if (isSolved) {
            editor.putInt("solvedstreak", solved_streak + 1)
            if (solved_streak == longest_streak) {
                editor.putInt("longeststreak", solved_streak + 1)
            }
        } else {
            editor.putInt("solvedstreak", 0)
        }
        editor.commit()
    }
}