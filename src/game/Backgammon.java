package game;

import java.util.ArrayList;

import ai.QualityState;

/*
 * Play against Dad
 * todo: 
 * clean up all code
 * neural network scene for the learning GUI
 * 
 * Static networkhandler methods?
 * adpative learning rate?
 * 
 * comboBox in Vs
 * use removeRoll more
 * threaded learning
 * make accuracy actually useful
 * 
 * replay and target network
 * 
 * make loading players set networkHandler instance variables to allow for varibale network infrastructure
 * threaded backpropagation
 * 
 * for loop limits using nodesinlayer array instead of this crap
 * 
 * check normalization of values 
 * 
 * future of moves out of jail
 * 
 * allowing multiple moves out of jail while selected
 */
public class Backgammon {

	private int[] board;
	private boolean whiteTurn, whiteOff, blackOff;
	private DiceRoll thisRoll;
	
	public int turnNum;
	
	public static Backgammon mainGame;
	
	public Backgammon() {
		board = new int[]{-2, 0, 0, 0, 0, 5, 0, 3, 0, 0, 0, -5, 5, 0, 0, 0, -3,0, -5, 0, 0, 0, 0, 2, 0, 0, 0, 0};
		// Testing Board Setups
		//board = new int[]{1, 1, 1, 1, 1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -5, -1, -1, -1, -1, -1, 2, 2, 0, 0};
		
		if (DiceRoll.flipCoin()) {
			whiteTurn = true;
		}
		checkSwapTurn();
		mainGame = this;
	}
	
	
	public boolean movePiece(int start, int stop) {
		boolean moveMade = false;
		char moveType;
		if (start == 100 || start == 101) {
			moveMade = makeMoveJail(start, stop, board, whiteTurn, thisRoll);
			moveType = 'j';
		} else if (stop < 0 || stop > 23){
			moveMade = makeMoveOff(start, stop, board, whiteTurn, thisRoll);
			moveType = 'o';
		} else {
			moveMade = moveNormal(start, stop, board, whiteTurn, thisRoll);
			moveType = 'n';
		}
		if (moveMade) {
			removeRoll(moveType, start, stop);
			checkSwapTurn();
			new QualityState(board, turnNum);
		}
		return moveMade;
	}
	private void removeRoll(char moveType, int start, int stop) {
		switch (moveType) {
		case 'j':
			if (whiteTurn) {
				thisRoll.movesLeft.remove(Integer.valueOf(Math.abs(24 - stop)));
			} else {
				thisRoll.movesLeft.remove(Integer.valueOf(Math.abs(stop + 1)));
			} 
			break;
		case 'o':
			thisRoll.movesLeft.remove(Integer.valueOf(Math.abs(start - stop)));
			break;
		case 'n':
			thisRoll.movesLeft.remove(Integer.valueOf(Math.abs(stop - start)));
			break;
				
		}
	}
	
	private static boolean moveNormal(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (!isValidMove(start, stop, board, whiteTurn, thisRoll)) {
			return false;
		}
		int increment = 1;
		if (!whiteTurn) {
			increment = -1;
		}
		sendToJail(start, stop, increment, board, whiteTurn);
		board[start] -= increment;
		board[stop] += increment;
		
		return true;
	}
	private static boolean isValidMove(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (!thisRoll.movesLeft.contains(Math.abs(start - stop))) {
			return false;
		}
		if (stop < 0 || stop > 23) {
			return false;
		}
		if (whiteTurn) {
			if(board[start] > 0 && board[stop] >= -1 && start > stop && board[24] == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if (board[start] < 0 && board[stop] <= 1 && stop > start && board[25] == 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private static boolean makeMoveJail(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (!isValidJail(start, stop, board, whiteTurn, thisRoll)) {
			return false;
		}
		int increment = 1;
		if (!whiteTurn) {
			increment = -1;
		}
		sendToJail(start, stop, increment, board, whiteTurn);
		if (whiteTurn) {
			board[24]--;
		} else {
			board[25] --;
		} 
		board[stop] += increment;
		return true;
	}
	private static boolean isValidJail(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (start == 101 && !whiteTurn) {
			if (thisRoll.movesLeft.contains(stop + 1) && board[stop] <= 1) {
				return true;
			}
		} else if (start == 100 && whiteTurn) {
			if (thisRoll.movesLeft.contains(24 - stop) && board[stop] >= -1) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean makeMoveOff(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (!isValidOff(start, stop, board, whiteTurn, thisRoll)) {
			return false;
		}
		if (whiteTurn) {
			board[start] --;
		} else {
			board[start] ++;
		}
		
		if (whiteTurn) {
			board[26]++;
		} else {
			board[27]++;
		}
		return true;
	}
	private static boolean isValidOff(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (whiteTurn) {
			if (thisRoll.movesLeft.contains(start - stop) && board[start] > 0) {
				if (stop > -1) {
					return false;
				} else if (stop == -1) {
					return true;
				} else {
					for (int i = 5; i > start; i--) {
						if (board[i] > 0) {
							return false;
						}
					}
					return true;
				}
			}
		} else {
			if (thisRoll.movesLeft.contains(stop - start) && board[start] < 0) {
				if (stop < 24) {
					return false;
				} else if (stop == 24) {
					return true;
				} else {
					for (int i = 18; i < start; i++) {
						if (board[i] < 0) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private static void sendToJail (int start, int stop, int increment, int[] board, boolean whiteTurn) {
		if (Math.abs(board[stop]) == 1 && board[stop] != increment) {
			if (whiteTurn) {
				board[25] ++;
			} else {
				board[24] ++;
			}
			board[stop] += increment;
		}
	}
	
	// Method simulates moves for NN. DONT USE FOR GAMEPLAY.
	public static int[] movePieceSimulation(int start, int stop, int[] board, boolean whiteTurn, DiceRoll thisRoll) {
		if (start == 100 || start == 101) {
			makeMoveJail(start, stop, board, whiteTurn, thisRoll);
		} else if (stop < 0 || stop > 23){
			makeMoveOff(start, stop, board, whiteTurn, thisRoll);
		} else {
			moveNormal(start, stop, board, whiteTurn, thisRoll);
		}
		return board;
	}
	
	private void checkSwapTurn() {
		updateOffTags();
		if (thisRoll == null || thisRoll.isEmpty()) {
			whiteTurn = !whiteTurn;
			thisRoll = new DiceRoll();
			turnNum++;
		}
	}
	public void switchTurnIfNoMoves() {
		ArrayList<int[]> moveList = getPossibleMoves(board, whiteTurn, thisRoll, whiteOff, blackOff);
		if (moveList.isEmpty()) {
			whiteTurn = !whiteTurn;
			thisRoll = new DiceRoll();
			turnNum++;
		}
	}
	private void updateOffTags() {
		whiteOff = true;
		blackOff = true;
		for (int i = 0; i < 18; i++) {
			if (board[i] < 0) {
				blackOff = false;
				break;
			}
		}
		for (int i = 23; i > 5; i--) {
			if (board[i] > 0) {
				whiteOff = false;
				break;
			}
		}
		if (board[24] != 0) {
			whiteOff = false;
		}
		if (board[25] != 0) {
			blackOff = false;
		}
	}
	public static boolean getIfWhiteOff(int[] board) {
		for (int i = 23; i > 5; i--) {
			if (board[i] > 0) {
				return false;
			}
		}
		if (board[24] != 0) {
			return false;
		}
		return true;
	}
	public static boolean getIfBlackOff(int[] board) {
		for (int i = 0; i < 18; i++) {
			if (board[i] < 0) {
				return false;
			}
		}
		if (board[25] != 0) {
			return false;
		}
		return true;
	}
	
	public static ArrayList<int[]> getPossibleMoves(int[] board, boolean whiteTurn, DiceRoll thisRoll, boolean whiteOff, boolean blackOff) {
		ArrayList<int[]> moveList = new ArrayList<int[]>();
		if (!whiteTurn && board[25] > 0) {
			for (int i = 0; i < 6; i++) {
				if (isValidJail(101, i, board, whiteTurn, thisRoll)) {
					moveList.add(new int[] {101, i});
				}
			}
		} else if (whiteTurn && board[24] > 0) {
			for (int i = 23; i > 17; i--) {
				if (isValidJail(100, i, board, whiteTurn, thisRoll)) {
					moveList.add(new int[] {100, i});
				}
			}
		} else {
			for (int i = 0; i < 24; i++) {
				if (whiteTurn && board[i] > 0) {
					for (int move : thisRoll.movesLeft) { 
						if (isValidMove(i, i - move, board, whiteTurn, thisRoll)) {
							moveList.add(new int[] {i, i - move});
						}
					}
				} else if (!whiteTurn && board[i] < 0){
					for (int move : thisRoll.movesLeft) { 
						if (isValidMove(i, i + move, board, whiteTurn, thisRoll)) {
							moveList.add(new int[] {i, i + move});
						}
					}
				}
			}
		}
		if (whiteOff && whiteTurn) {
			for (int i = 5; i > -1; i--) {
				for (int move : thisRoll.movesLeft) {
					if (isValidOff(i, i - move, board, whiteTurn, thisRoll)) {
						moveList.add(new int[] {i, i - move});
					}
				}
			}
		} else if (blackOff && !whiteTurn) {
			for (int i = 18; i < 24; i++) {
				for (int move : thisRoll.movesLeft) {
					if (isValidOff(i, i + move, board, whiteTurn, thisRoll)) {
						moveList.add(new int[] {i, i + move});
					}
				}
			}
		}
		return moveList;
	}
	
	public int[] getBoard() {
		return board.clone();
	}
	public int getJail0() {
		return board[24];
	}
	public int getJail1() {
		return board[25];
	}
	public int getOffBoardWhite() {
		return board[26];
	}
	public int getOffBoardBlack() {
		return board[27];
	}
	public DiceRoll getThisRoll() {
		return thisRoll.clone();
	}
	public boolean getWhiteTurn() {
		return whiteTurn;
	}
	public boolean getWhiteOff() {
		return whiteOff;
	}
	public boolean getBlackOff() {
		return blackOff;
	}
	public int isGameOver() {
		boolean whiteWin = true;
		boolean blackWin = true;
		for (int i = 0; i < 26; i++) {
			if(board [i] > 0) {
				whiteWin = false;
			} else if (board[i] < 0) {
				blackWin = false;
			}
		}
		if (whiteWin) {
			return 1;
		} else if (blackWin) {
			return -1;
		} else {
			return 0;
		}
	}
}
