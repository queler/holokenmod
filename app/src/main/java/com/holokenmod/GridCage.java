package com.holokenmod;

import androidx.annotation.NonNull;

import com.holokenmod.creation.GridCreator;
import com.holokenmod.options.ApplicationPreferences;
import com.holokenmod.options.DigitSetting;
import com.holokenmod.options.GameVariant;
import com.holokenmod.options.GridCageOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class GridCage {
	
	private final int mType;
	private final ArrayList<GridCell> mCells;
	private final Grid grid;
	
	private GridCageAction mAction;
	private int mResult;
	private boolean mUserMathCorrect;
	private boolean mSelected;
	private int mId;
	
	public GridCage(final Grid grid) {
		this(grid, -1);
	}
	
	public GridCage(final Grid grid, final int type) {
		this.grid = grid;
		mType = type;
		mUserMathCorrect = true;
		mSelected = false;
		mCells = new ArrayList<>();
	}
	
	public GridCage(Grid grid, GridCageAction action, int result) {
		this(grid);
		
		this.mAction = action;
		this.mResult = result;
	}
	
	public static GridCage createWithCells(Grid grid, GridCell firstCell, int cage_type) {
		GridCage cage = new GridCage(grid, cage_type);
		
		final int[][] cage_coords = GridCreator.CAGE_COORDS[cage_type];
		
		for (final int[] cage_coord : cage_coords) {
			final int col = firstCell.getColumn() + cage_coord[0];
			final int row = firstCell.getRow() + cage_coord[1];
			cage.addCell(grid.getCellAt(row, col));
		}
		
		return cage;
	}
	
	public static GridCage createWithCells(Grid grid, Collection<GridCell> cells) {
		GridCage cage = new GridCage(grid);
		
		for (GridCell cell : cells) {
			cage.addCell(grid.getCell(cell.getCellNumber()));
		}
		
		return cage;
	}
	
	@NonNull
	public String toString() {
		String retStr = "";
		retStr += "Cage id: " + this.mId + ", Type: " + this.mType;
		retStr += ", Action: ";
		switch (this.mAction) {
			case ACTION_NONE:
				retStr += "None";
				break;
			case ACTION_ADD:
				retStr += "Add";
				break;
			case ACTION_SUBTRACT:
				retStr += "Subtract";
				break;
			case ACTION_MULTIPLY:
				retStr += "Multiply";
				break;
			case ACTION_DIVIDE:
				retStr += "Divide";
				break;
		}
		
		retStr += ", ActionStr: " + this.mAction
				.getOperationDisplayName() + ", Result: " + this.mResult;
		retStr += ", cells: " + getCellNumbers();
		
		return retStr;
	}
	
	/*
	 * Generates the arithmetic for the cage, semi-randomly.
	 *
	 * - If a cage has 3 or more cells, it can only be an add or multiply.
	 * - else if the cells are evenly divisible, division is used, else
	 *   subtraction.
	 */
	public void setArithmetic(final GridCageOperation operationSet) {
		this.mAction = null;
		if (this.mCells.size() == 1) {
			setSingleCellArithmetic();
			return;
		}
		
		Optional<GridCageAction> action = decideMultipleOrAddOrOther(operationSet);
		
		action.ifPresent((a) -> this.mAction = a);
		
		//Log.d("generated", operationSet.toString());
        //Log.d("generated", mAction != null ? mAction.toString() : "null");
		
		if (this.mAction == GridCageAction.ACTION_ADD) {
			int total = 0;
			for (final GridCell cell : this.mCells) {
				total += cell.getValue();
			}
			this.mResult = total;
		}
		if (this.mAction == GridCageAction.ACTION_MULTIPLY) {
			int total = 1;
			for (final GridCell cell : this.mCells) {
				total *= cell.getValue();
			}
			this.mResult = total;
		}
		if (this.mAction != null) {
			return;
		}
		
		final int cell1Value = this.mCells.get(0).getValue();
		final int cell2Value = this.mCells.get(1).getValue();
		int higher = cell1Value;
		int lower = cell2Value;
		boolean canDivide = false;
		
		if (cell1Value < cell2Value) {
			higher = cell2Value;
			lower = cell1Value;
		}
        
        if (ApplicationPreferences.getInstance()
                .getDigitSetting() == DigitSetting.FIRST_DIGIT_ONE && higher % lower == 0 && operationSet != GridCageOperation.OPERATIONS_ADD_SUB) {
            canDivide = true;
        }
        
        if (ApplicationPreferences.getInstance()
                .getDigitSetting() == DigitSetting.FIRST_DIGIT_ZERO && lower > 0 && higher % lower == 0 && operationSet != GridCageOperation.OPERATIONS_ADD_SUB) {
            canDivide = true;
        }
		
		if (canDivide) {
			this.mResult = higher / lower;
			this.mAction = GridCageAction.ACTION_DIVIDE;
		} else {
			this.mResult = higher - lower;
			this.mAction = GridCageAction.ACTION_SUBTRACT;
		}
	}
	
	private Optional<GridCageAction> decideMultipleOrAddOrOther(GridCageOperation operationSet) {
		if (operationSet == GridCageOperation.OPERATIONS_MULT) {
			return Optional.of(GridCageAction.ACTION_MULTIPLY);
		}
		
		final double rand = RandomSingleton.getInstance().nextDouble();
		
		double addChance = 0.25;
		double multChance = 0.5;
		
		if (operationSet == GridCageOperation.OPERATIONS_ADD_SUB) {
			if (this.mCells.size() > 2) {
				addChance = 1.0;
			} else {
				addChance = 0.4;
			}
			multChance = 0.0;
		} else if (this.mCells
				.size() > 2 || operationSet == GridCageOperation.OPERATIONS_ADD_MULT) { // force + and x only
			addChance = 0.5;
			multChance = 1.0;
		}
		
		if (rand <= addChance) {
			return Optional.of(GridCageAction.ACTION_ADD);
		} else if (rand <= multChance) {
			return Optional.of(GridCageAction.ACTION_MULTIPLY);
		}
		
		return Optional.empty();
	}
	
	public void setSingleCellArithmetic() {
		this.mAction = GridCageAction.ACTION_NONE;
		this.mResult = this.mCells.get(0).getValue();
	}
	
	public void setCageId(final int id) {
		this.mId = id;
	}
	
	private boolean isAddMathsCorrect() {
		int total = 0;
		for (final GridCell cell : this.mCells) {
			total += cell.getUserValue();
		}
		return (total == this.mResult);
	}
	
	private boolean isMultiplyMathsCorrect() {
		int total = 1;
		for (final GridCell cell : this.mCells) {
			total *= cell.getUserValue();
		}
		return (total == this.mResult);
	}
	
	private boolean isDivideMathsCorrect() {
        if (this.mCells.size() != 2) {
            return false;
        }
        
        if (this.mCells.get(0).getUserValue() > this.mCells.get(1).getUserValue()) {
            return this.mCells.get(0).getUserValue() == (this.mCells.get(1)
                    .getUserValue() * this.mResult);
        } else {
            return this.mCells.get(1).getUserValue() == (this.mCells.get(0)
                    .getUserValue() * this.mResult);
        }
	}
	
	private boolean isSubtractMathsCorrect() {
        if (this.mCells.size() != 2) {
            return false;
        }
        
        if (this.mCells.get(0).getUserValue() > this.mCells.get(1).getUserValue()) {
            return (this.mCells.get(0).getUserValue() - this.mCells.get(1)
                    .getUserValue()) == this.mResult;
        } else {
            return (this.mCells.get(1).getUserValue() - this.mCells.get(0)
                    .getUserValue()) == this.mResult;
        }
	}
	
	public boolean isMathsCorrect() {
        if (this.mCells.size() == 1) {
            return this.mCells.get(0).isUserValueCorrect();
        }
		
		if (GameVariant.getInstance().showOperators()) {
			switch (this.mAction) {
				case ACTION_ADD:
					return isAddMathsCorrect();
				case ACTION_MULTIPLY:
					return isMultiplyMathsCorrect();
				case ACTION_DIVIDE:
					return isDivideMathsCorrect();
				case ACTION_SUBTRACT:
					return isSubtractMathsCorrect();
			}
		} else {
			return isAddMathsCorrect() || isMultiplyMathsCorrect() ||
					isDivideMathsCorrect() || isSubtractMathsCorrect();
			
		}
		throw new RuntimeException("isSolved() got to an unreachable point " +
				this.mAction + ": " + this.toString());
	}
	
	// Determine whether user entered values match the arithmetic.
	//
	// Only marks cells bad if all cells have a uservalue, and they dont
	// match the arithmetic hint.
	public void userValuesCorrect() {
		this.mUserMathCorrect = true;
        for (final GridCell cell : this.mCells) {
            if (!cell.isUserValueSet()) {
                this.setBorders();
                return;
            }
        }
		this.mUserMathCorrect = this.isMathsCorrect();
		this.setBorders();
	}
	
	public void setBorders() {
		for (final GridCell cell : this.mCells) {
			for (final Direction direction : Direction.values()) {
				cell.getCellBorders().setBorderType(direction, GridBorderType.BORDER_NONE);
			}
            if (this.grid.getCage(cell.getRow() - 1, cell.getColumn()) != this) {
                if (!this.mUserMathCorrect && GameVariant.getInstance().showBadMaths()) {
                    cell.getCellBorders()
                            .setBorderType(Direction.NORTH, GridBorderType.BORDER_WARN);
                } else if (this.mSelected) {
                    cell.getCellBorders()
                            .setBorderType(Direction.NORTH, GridBorderType.BORDER_CAGE_SELECTED);
                } else {
                    cell.getCellBorders()
                            .setBorderType(Direction.NORTH, GridBorderType.BORDER_SOLID);
                }
            }
            
            if (this.grid.getCage(cell.getRow(), cell.getColumn() + 1) != this) {
                if (!this.mUserMathCorrect && GameVariant.getInstance().showBadMaths()) {
                    cell.getCellBorders().setBorderType(Direction.EAST, GridBorderType.BORDER_WARN);
                } else if (this.mSelected) {
                    cell.getCellBorders()
                            .setBorderType(Direction.EAST, GridBorderType.BORDER_CAGE_SELECTED);
                } else {
                    cell.getCellBorders()
                            .setBorderType(Direction.EAST, GridBorderType.BORDER_SOLID);
                }
            }
            
            if (this.grid.getCage(cell.getRow() + 1, cell.getColumn()) != this) {
                if (!this.mUserMathCorrect && GameVariant.getInstance().showBadMaths()) {
                    cell.getCellBorders()
                            .setBorderType(Direction.SOUTH, GridBorderType.BORDER_WARN);
                } else if (this.mSelected) {
                    cell.getCellBorders()
                            .setBorderType(Direction.SOUTH, GridBorderType.BORDER_CAGE_SELECTED);
                } else {
                    cell.getCellBorders()
                            .setBorderType(Direction.SOUTH, GridBorderType.BORDER_SOLID);
                }
            }
            
            if (this.grid.getCage(cell.getRow(), cell.getColumn() - 1) != this) {
                if (!this.mUserMathCorrect && GameVariant.getInstance().showBadMaths()) {
                    cell.getCellBorders().setBorderType(Direction.WEST, GridBorderType.BORDER_WARN);
                } else if (this.mSelected) {
                    cell.getCellBorders()
                            .setBorderType(Direction.WEST, GridBorderType.BORDER_CAGE_SELECTED);
                } else {
                    cell.getCellBorders()
                            .setBorderType(Direction.WEST, GridBorderType.BORDER_SOLID);
                }
            }
		}
	}
	
	public int getId() {
		return mId;
	}
	
	public void addCell(final GridCell cell) {
		this.mCells.add(cell);
		cell.setCage(this);
	}
	
	public String getCellNumbers() {
		final StringBuilder numbers = new StringBuilder();
		
		for (final GridCell cell : this.mCells) {
			numbers.append(cell.getCellNumber()).append(",");
		}
		
		return numbers.toString();
	}
	
	public int getNumberOfCells() {
		return this.mCells.size();
	}
	
	public GridCell getCell(final int cellNumber) {
		return this.mCells.get(cellNumber);
	}
	
	public ArrayList<GridCell> getCells() {
		return this.mCells;
	}
    
    public void updateCageText() {
        if (GameVariant.getInstance().showOperators()) {
            setCagetext(this.mResult + this.mAction.getOperationDisplayName());
        } else {
            setCagetext(this.mResult + "");
        }
    }
    
    private void setCagetext(final String cageText) {
        this.mCells.get(0).setCagetext(cageText);
    }
	
	public void setSelected(final boolean mSelected) {
		this.mSelected = mSelected;
	}
	
	public int getResult() {
		return mResult;
	}
	
	public void setResult(final int mResult) {
		this.mResult = mResult;
	}
	
	public GridCageAction getAction() {
		return mAction;
	}
	
	public void setAction(final GridCageAction mAction) {
		this.mAction = mAction;
	}
	
	public int getType() {
		return mType;
	}
	
	public void addCellNumbers(int... cellNumbers) {
		if (cellNumbers == null) {
			return;
		}
		
		for(int cellNumber : cellNumbers) {
			addCell(grid.getCell(cellNumber));
		}
	}
}