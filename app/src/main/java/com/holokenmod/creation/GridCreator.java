package com.holokenmod.creation;

import com.holokenmod.Grid;
import com.holokenmod.GridCage;
import com.holokenmod.GridCell;
import com.holokenmod.GridSize;
import com.holokenmod.RandomSingleton;
import com.holokenmod.options.ApplicationPreferences;
import com.holokenmod.options.DigitSetting;
import com.holokenmod.options.GameVariant;
import com.holokenmod.options.GridCageOperation;
import com.holokenmod.options.SingleCageUsage;

import java.util.ArrayList;

public class GridCreator {
	
	private static final int SINGLE_CELL_CAGE = 0;
	
	// O = Origin (0,0) - must be the upper leftmost cell
	// X = Other cells used in cage
	private static final int[][][] CAGE_COORDS = new int[][][]{
			// O
			{{0, 0}},
			// O
			// X
			{{0, 0}, {0, 1}},
			// OX
			{{0, 0}, {1, 0}},
			// O
			// X
			// X
			{{0, 0}, {0, 1}, {0, 2}},
			// OXX
			{{0, 0}, {1, 0}, {2, 0}},
			// O
			// XX
			{{0, 0}, {0, 1}, {1, 1}},
			// O
			//XX
			{{0, 0}, {0, 1}, {-1, 1}},
			// OX
			//  X
			{{0, 0}, {1, 0}, {1, 1}},
			// OX
			// X
			{{0, 0}, {1, 0}, {0, 1}},
			// OX
			// XX
			//=== 9 ===
			{{0, 0}, {1, 0}, {0, 1}, {1, 1}},
			// OX
			// X
			// X
			{{0, 0}, {1, 0}, {0, 1}, {0, 2}},
			// OX
			//  X
			//  X
			//{{0,0},{1,0},{1,1},{1,2}},
			// O
			// X
			// XX
			//{{0,0},{0,1},{0,2},{1,2}},
			// O
			// X
			//XX
			//=== 11 ===
			{{0, 0}, {0, 1}, {0, 2}, {-1, 2}},
			// OXX
			// X
			{{0, 0}, {1, 0}, {2, 0}, {0, 1}},
			// OXX
			//   X
			{{0, 0}, {1, 0}, {2, 0}, {2, 1}},
			// O
			// XXX
			/*{{0,0},{0,1},{1,1},{2,1}},
			//  O
			//XXX
			{{0,0},{-2,1},{-1,1},{0,1}},
			// O
			// XX
			// X
			{{0,0},{0,1},{0,2},{1,1}},
			// O
			//XX
			// X
			{{0,0},{0,1},{0,2},{-1,1}},
			// OXX
			//  X
			{{0,0},{1,0},{2,0},{1,1}},
			// O
			//XXX
			{{0,0},{-1,1},{0,1},{1,1}},
			// OXXX
			{{0,0},{1,0},{2,0},{3,0}},
			// O
			// X
			// X
			// X
			{{0,0},{0,1},{0,2},{0,3}},
			// O
			// XX
			//  X
			{{0,0},{0,1},{1,1},{1,2}},
			// O
			//XX
			//X
			{{0,0},{0,1},{-1,1},{-1,2}},
			// OX
			//  XX
			{{0,0},{1,0},{1,1},{2,1}},
			// OX
			//XX
			{{0,0},{1,0},{0,1},{-1,1}}*/
	};
	
	private final GridSize gridSize;
	private Grid grid;
	
	public GridCreator(final GridSize gridSize) {
		this.gridSize = gridSize;
	}
	
	private int createSingleCages() {
		final int singles = (int) (Math.sqrt(grid.getGridSize().getSurfaceArea()) / 2);
		
		final boolean[] RowUsed = new boolean[grid.getGridSize().getHeight()];
		final boolean[] ColUsed = new boolean[grid.getGridSize().getWidth()];
		final boolean[] ValUsed = new boolean[grid.getGridSize().getAmountOfNumbers()];
		
		for (int i = 0; i < singles; i++) {
			GridCell cell;
			int cellIndex;
			do {
				cell = grid.getCell(RandomSingleton.getInstance()
						.nextInt(grid.getGridSize().getSurfaceArea()));
				
				cellIndex = cell.getValue();
				
				if (ApplicationPreferences.getInstance()
						.getDigitSetting() == DigitSetting.FIRST_DIGIT_ONE) {
					cellIndex--;
				}
				
			} while (RowUsed[cell.getRow()] || ColUsed[cell.getRow()] || ValUsed[cellIndex]);
			ColUsed[cell.getColumn()] = true;
			RowUsed[cell.getRow()] = true;
			ValUsed[cellIndex] = true;
			final GridCage cage = new GridCage(grid, SINGLE_CELL_CAGE);
			cage.addCell(cell);
			cage.setSingleCellArithmetic();
			cage.setCageId(i);
			grid.addCage(cage);
		}
		return singles;
	}
	
	private void createCages() {
		final GridCageOperation operationSet = GameVariant.getInstance().getCageOperation();
		boolean restart;
		
		do {
			restart = false;
			
			int cageId = 0;
			
			if (ApplicationPreferences.getInstance()
					.getSingleCageUsage() == SingleCageUsage.FIXED_NUMBER) {
				cageId = createSingleCages();
			}
			
			for (final GridCell cell : grid.getCells()) {
				if (cell.CellInAnyCage()) {
					continue;
				}
				
				final ArrayList<Integer> possible_cages = getValidCages(grid, cell);
				
				final int cage_type;
				
				if (possible_cages.size() == 1) {
					// Only possible cage is a single
					if (ApplicationPreferences.getInstance()
							.getSingleCageUsage() != SingleCageUsage.DYNAMIC) {
						grid.ClearAllCages();
						restart = true;
						break;
					} else {
						cage_type = 0;
					}
				} else {
					cage_type = possible_cages.get(RandomSingleton.getInstance()
							.nextInt(possible_cages.size() - 1) + 1);
				}
				
				final GridCage cage = GridCage.createWithCells(grid, cell, cage_type, CAGE_COORDS[cage_type]);
				
				cage.setArithmetic(operationSet);
				cage.setCageId(cageId++);
				grid.addCage(cage);
			}
		} while (restart);
		
		for (final GridCage cage : grid.getCages()) {
			cage.setBorders();
		}
		grid.setCageTexts();
	}
	
	private ArrayList<Integer> getValidCages(final Grid grid, final GridCell origin) {
		final ArrayList<Integer> valid = new ArrayList<>();
		
		for (int cage_num = 0; cage_num < CAGE_COORDS.length; cage_num++) {
			final int[][] cage_coords = CAGE_COORDS[cage_num];
			
			boolean validCage = true;
			
			for (final int[] cage_coord : cage_coords) {
				final int col = origin.getColumn() + cage_coord[0];
				final int row = origin.getRow() + cage_coord[1];
				final GridCell c = grid.getCellAt(row, col);
				if (c == null || c.CellInAnyCage()) {
					validCage = false;
					break;
				}
			}
			
			if (validCage) {
				valid.add(cage_num);
			}
		}
		
		return valid;
	}
	
	/*
	 * Fills the grid with random numbers, per the rules:
	 *
	 * - 1 to <rowsize> on every row and column
	 * - No duplicates in any row or column.
	 */
	private void randomiseGrid() {
		GridRandomizer randomizer = new GridRandomizer(grid);
		
		randomizer.createGrid();
	}
	
	public Grid create() {
		final boolean debug = false;
		
		int dlxNumber;
		int backTrackNumber;
		int backTrack2Number;
		int num_attempts = 0;
		RandomSingleton.getInstance().discard();
		
		long sumBacktrackDuration = 0;
		long sumBacktrack2Duration = 0;
		long sumDLXDuration = 0;
		
		grid = new Grid(gridSize);
		
		grid.addAllCells();
		
		randomiseGrid();
		createCages();
		
		return grid;
		
		/*do {
			grid = new Grid(gridSize);
			
			grid.addAllCells();
			
			randomiseGrid();
			createCages();
			
			num_attempts++;
			
			long dlxMillis = System.currentTimeMillis();
			final MathDokuDLX mdd = new MathDokuDLX(grid);
			// Stop solving as soon as we find multiple solutions
			dlxNumber = mdd.Solve(DLX.SolveType.MULTIPLE);
			long dlxDuration = System.currentTimeMillis() - dlxMillis;
			sumDLXDuration += dlxDuration;

			Log.d("MathDoku", "DLX Num Solns = " + dlxNumber + " in " + dlxDuration + " ms");
			
			if (debug) {
				long backtrackMillis = System.currentTimeMillis();
				final MathDokuCageBackTrack backTrack = new MathDokuCageBackTrack(grid, true);
				backTrackNumber = backTrack.solve();
				long backtrackDuration = System.currentTimeMillis() - backtrackMillis;
				sumBacktrackDuration += backtrackDuration;
				
				grid.clearUserValues();
				
				long backtrack2Millis = System.currentTimeMillis();
				final MathDokuCage2BackTrack backTrack2 = new MathDokuCage2BackTrack(grid, true);
				backTrack2Number = backTrack2.solve();
				long backtrack2Duration = System.currentTimeMillis() - backtrack2Millis;
				sumBacktrack2Duration += backtrack2Duration;
				
				grid.clearUserValues();
				
				Log.d("Backtrack", "Backtrack Num Solns = " + backTrackNumber + " in " + backtrackDuration + " ms");
				Log.d("Backtrack2", "Backtrack2 Num Solns = " + backTrack2Number + " in " + backtrack2Duration + " ms");
				
				if (backTrack2Number != dlxNumber) {
					Log.d("backtrack2", "difference: backtrack2 " + backTrack2Number + " - dlx " + dlxNumber + ":" + grid);
					
					//System.exit(0);
				}
				
				if (backTrackNumber == 1) {
					grid.clearUserValues();
				}
			}
		} while (dlxNumber != 1);
		
		long averageBacktrack = sumBacktrackDuration / num_attempts;
		long averageBacktrack2 = sumBacktrack2Duration / num_attempts;
		long averageDLX = sumDLXDuration / num_attempts;
		
		Log.d("MathDoku", "DLX Num Attempts = " + num_attempts + " in " + sumDLXDuration + " ms" + " (average " + averageDLX + " ms)");
		Log.d("MathDoku", "Backtrack Num Attempts = " + num_attempts + " in " + sumBacktrackDuration + " ms" + " (average " + averageBacktrack + " ms)");
		Log.d("MathDoku", "Backtrack 2 Num Attempts = " + num_attempts + " in " + sumBacktrack2Duration + " ms" + " (average " + averageBacktrack2 + " ms)");
		
		grid.clearUserValues();
		
		return grid;*/
	}
}