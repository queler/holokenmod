package com.holokenmod.creation;

import com.holokenmod.RandomSingleton;
import com.holokenmod.Randomizer;
import com.holokenmod.backtrack.hybrid.MathDokuCage2BackTrack;
import com.holokenmod.creation.cage.GridSingleCageCreator;
import com.holokenmod.grid.Grid;
import com.holokenmod.grid.GridCage;
import com.holokenmod.options.DigitSetting;
import com.holokenmod.options.GameVariant;
import com.srlee.dlx.DLX;
import com.srlee.dlx.MathDokuDLX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class GridCalculator {
	private final Logger LOGGER = LoggerFactory.getLogger(GridCalculator.class);
	
	private final GameVariant variant;
	private final Randomizer randomizer;
	private final PossibleDigitsShuffler shuffler;
	
	public GridCalculator(final GameVariant variant) {
		this(RandomSingleton.getInstance(), new RandomPossibleDigitsShuffler(), variant);
	}
	
	public GridCalculator(final Randomizer randomizer, final PossibleDigitsShuffler shuffler, final GameVariant variant) {
		this.randomizer = randomizer;
		this.shuffler = shuffler;
		this.variant = variant;
	}
	
	public Grid calculate() {
		final boolean debug = false;
		
		int dlxNumber = 0;
		int backTrack2Number = 0;
		int num_attempts = 0;
		
		long sumBacktrack2Duration = 0;
		long sumDLXDuration = 0;
		
		boolean useDLX = variant.getGridSize().isSquare() &&
				(variant.getOptions().getDigitSetting() == DigitSetting.FIRST_DIGIT_ZERO
				|| variant.getOptions().getDigitSetting() == DigitSetting.FIRST_DIGIT_ONE);
		
		Grid grid;
		
		do {
			grid = new GridCreator(randomizer, shuffler, variant).createRandomizedGridWithCages();
		
			num_attempts++;
			
			if (useDLX) {
				long dlxMillis = System.currentTimeMillis();
				final MathDokuDLX mdd = new MathDokuDLX(grid);
				// Stop solving as soon as we find multiple solutions
				dlxNumber = mdd.Solve(DLX.SolveType.MULTIPLE);
				long dlxDuration = System.currentTimeMillis() - dlxMillis;
				sumDLXDuration += dlxDuration;
				
				LOGGER.info("DLX Num Solns = " + dlxNumber + " in " + dlxDuration + " ms");
				
				if (dlxNumber == 0) {
					LOGGER.debug(grid.toString());
				}
			}
			
			if (!useDLX || debug) {
				long backtrack2Millis = System.currentTimeMillis();
				final MathDokuCage2BackTrack backTrack2 = new MathDokuCage2BackTrack(grid, true);
				backTrack2Number = backTrack2.solve();
				long backtrack2Duration = System.currentTimeMillis() - backtrack2Millis;
				sumBacktrack2Duration += backtrack2Duration;
				
				grid.clearUserValues();
				
				LOGGER.info("Backtrack2 Num Solns = " + backTrack2Number + " in " + backtrack2Duration + " ms");
				
				if (backTrack2Number != dlxNumber) {
					LOGGER.debug("difference: backtrack2 " + backTrack2Number + " - dlx " + dlxNumber + ":" + grid);
					
					//System.exit(0);
				}
				
				if (backTrack2Number == 1) {
					grid.clearUserValues();
				}
				
				if (backTrack2Number == 0) {
					LOGGER.debug("backtrack2 found no solution: " + grid);
					
					for(GridCage cage : grid.getCages()) {
						LOGGER.debug("backtrack2 cage "
								+ cage.getId());
						
						for(int[] possibleNums : new GridSingleCageCreator(grid, cage).getPossibleNums()) {
							LOGGER.debug("backtrack2     " + Arrays.toString(possibleNums));
						}
					}
					
					System.exit(0);
				}
			}
		} while ((useDLX && dlxNumber != 1) || (!useDLX && backTrack2Number != 1));
		
		long averageBacktrack2 = sumBacktrack2Duration / num_attempts;
		long averageDLX = sumDLXDuration / num_attempts;
		
		LOGGER.debug("DLX Num Attempts = " + num_attempts + " in " + sumDLXDuration + " ms" + " (average " + averageDLX + " ms)");
		LOGGER.debug("MathDoku", "Backtrack 2 Num Attempts = " + num_attempts + " in " + sumBacktrack2Duration + " ms" + " (average " + averageBacktrack2 + " ms)");
		
		grid.clearUserValues();
		
		return grid;
	}
}