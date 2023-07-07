package ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import game.Backgammon;
import game.DiceRoll;
import javafx.application.Platform;



public class Player implements Runnable{

	// Fields saved when loading Players
	String playerFile;
	String networkFile;
	NetworkHandler networkHandler;
	double epsilon;
	int generation = 0;
	
	// Fields that don't need to be saved
	Random random;
	static double learningRate = .01;
	static double trainingCycles = 10;
	static int maxReplay = 500;
	static final double EPSILON_LOWER_LIMIT = .05;

	static double runningAccuracyChange;

	private int whiteWins, blackWins;
	
	public Player() {
		this(new NetworkHandler(learningRate), "default_player.txt");
	}
	public Player(String playerFile) {
		this(new NetworkHandler(getNetworkFile(playerFile), learningRate), playerFile);
	}
	public Player(NetworkHandler networkHandler, String playerFile) {
		this.networkHandler = networkHandler;
		this.playerFile = playerFile;
		setNetworkFile();
		random = new Random();
		epsilon = .999;
		
	}
	
	public int[] getMove() {
		Backgammon g = Backgammon.mainGame;
		ArrayList<int[]> possibleMoves = Backgammon.getPossibleMoves(g.getBoard(), g.getWhiteTurn(), g.getThisRoll(), g.getWhiteOff(), g.getBlackOff());
		if (possibleMoves.size() == 0) {
			return null;
		}
		if (random.nextDouble() < epsilon) {
			// Explore
			return possibleMoves.get(random.nextInt(possibleMoves.size()));
		} else {
			// Exploit
			return getBestMove(true);
		}
	}	
	public int[] getBestMove(boolean training) {
		// Gathers all possible moves
		Backgammon g = Backgammon.mainGame;
		ArrayList<int[]> possibleMoves = Backgammon.getPossibleMoves(g.getBoard(), g.getWhiteTurn(), g.getThisRoll(), g.getWhiteOff(), g.getBlackOff());
		
		// Values for Simulating Moves
		DiceRoll thisRoll = Backgammon.mainGame.getThisRoll().clone();
		int[] board = Backgammon.mainGame.getBoard();
		boolean whiteTurn = Backgammon.mainGame.getWhiteTurn();
		
		// Keeps track of best moves
		double bestScore = whiteTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		int[] bestMove = null;
		
		for (int[] move : possibleMoves) {
			double expectedValue = getBestMoveAux(move, board, whiteTurn, thisRoll, training);
			// If move is better than best, save it and continue
			if (whiteTurn) {
				if (expectedValue < bestScore) {
					bestMove = move;
					bestScore = expectedValue;
				} 
			} else {
				if (expectedValue > bestScore) {
					bestMove = move;
					bestScore = expectedValue;
				}
			}
		}
		System.out.println("T: " + (whiteTurn ? "White" : "Black") + ", Best Move: " + Arrays.toString(bestMove) + ", Expected Value: " + bestScore);
		return bestMove;
	}
	private double getBestMoveAux(int[] move, int[] board, boolean whiteTurn, DiceRoll thisRoll, boolean training) {
		// Make Move
		int[] afterMove = Backgammon.movePieceSimulation(move[0], move[1], board.clone(), whiteTurn, thisRoll);
		// Update Values
		boolean blackOff = Backgammon.getIfBlackOff(afterMove);
		boolean whiteOff = Backgammon.getIfWhiteOff(afterMove);
		DiceRoll newRoll = thisRoll.clone().removeRoll(move[0], move[1]);
		// Check if Moves After
		ArrayList<int[]> possibleMoves = Backgammon.getPossibleMoves(afterMove, whiteTurn, newRoll, whiteOff, blackOff);
		if (possibleMoves.isEmpty() || training) {
			// Base Case
			double[] doubleArr = convertIntToDouble(afterMove);
			return networkHandler.forwardPropagate(doubleArr)[0];
		} else {
			// Main Case
			double bestScore = whiteTurn ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			
			for (int[] nextMove : possibleMoves) {
				double expectedValue = getBestMoveAux(nextMove, board, whiteTurn, newRoll, training);
				// If move is better than best, save it and continue
				if (whiteTurn) {
					if (expectedValue < bestScore) {
						bestScore = expectedValue;
					} 
				} else {
					if (expectedValue > bestScore) {
						bestScore = expectedValue;
					}
				}
			}
			return bestScore;
		}
	}
	
	private double[] convertIntToDouble(int[] intArr) {
		double[] doubleArr = new double[intArr.length];
		for (int i = 0; i < intArr.length; i++) {
			doubleArr[i] = intArr[i];
		}
		return doubleArr;
	}
	
	private void setNetworkFile() {
		networkFile = getNetworkFile(playerFile);
	}
	private static String getNetworkFile(String playerFile) {
		String networkFile = playerFile.substring(0, playerFile.length() - 4);
		networkFile = networkFile + "_network.txt";
		return networkFile;
	}

	void updateEpsilon() {
		epsilon = Math.pow(epsilon, 1.01);
		epsilon = epsilon < EPSILON_LOWER_LIMIT ? EPSILON_LOWER_LIMIT : epsilon;
	}
	
	public void learn() {
		QualityState.setupForLearning();
		while(QualityState.stateQueue.size() > maxReplay) {
			QualityState.stateQueue.remove();
		}
		//System.out.println(QualityState.stateList);
		double[][][] allValues = QualityState.getStatesAsArray();
		double accuracy = 0;
		double accuracy1 = 0;
		

		//System.out.println(QualityState.stateQueue.size());
		if (QualityState.stateQueue.size() == maxReplay) {
			for (int i = 0; i < trainingCycles; i++) {
				accuracy = networkHandler.backPropagate(allValues[0], allValues[1]);
				//System.out.println(accuracy);
				if (i == 0) {
					
					accuracy1 = accuracy;
				}
			}
			generation++;
		}

		//System.out.println(Arrays.deepToString(networkHandler.totalWeightDerivatives));
		//System.out.println(Arrays.deepToString(networkHandler.totalBiasDerivatives));
		//System.out.println(Arrays.deepToString(networkHandler.network.values));
		QualityState.clearList();
		
		Player.savePlayer(this);
		updateEpsilon();
		System.out.println("Initial Error: " + accuracy1);
		System.out.println("Final Error:   " + accuracy); 
		System.out.println("Error Change: " + (accuracy1 - accuracy));
		runningAccuracyChange += accuracy1 - accuracy;
		System.out.println("Running Error Change: " + runningAccuracyChange);
		System.out.println("Generation: " + generation);
		System.out.println("W: " + whiteWins + ", B: " + blackWins);
		System.out.println("Epsilon: " + epsilon);
		System.out.println("----------");
	}
	
	public void run() {

			Backgammon game = Backgammon.mainGame;
			int counter = 0;
			while(game.isGameOver() == 0) {
				int[] move = getMove();
				if (move == null) {
					game.switchTurnIfNoMoves();
				} else {
					synchronized(game) {
						game.movePiece(move[0], move[1]);
					}
				}
				if (counter > training.TrainingGUI.graphicUpdateRate) {
					Platform.runLater(new training.GUIUpdater(1));
					counter = 0;
				}
				counter++;
				try {Thread.sleep((long)training.TrainingGUI.gameDelay);} catch (InterruptedException e) {}
			} 
			
			if (game.isGameOver() == -1) {
				blackWins++;
			} else if (game.isGameOver() == 1) {
				whiteWins++;
			}
		learn();
		Platform.runLater(new training.GUIUpdater(2));
		
	}
	
	public String toString() {
		StringBuffer building = new StringBuffer();
		building.append("Epsilon: " + epsilon);
		building.append("\nGeneration: " + generation);
		return building.toString();
	}
	
	public static boolean savePlayer(Player player) {
		try {
			File saveFile = new File(player.playerFile);
			saveFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
			writer.write(player.toString());
			writer.close();
			
			Network.saveNetwork(player.networkHandler.getNetwork(), player.networkFile);
			} catch (Exception e) {
				System.err.println("Error Writing Player to File");
				e.printStackTrace();
				return false;
			}
		return true;
	}
	
	public static Player loadPlayer(String playerFile) {
		Player player = new Player(playerFile);
		try {
			File file = new File(playerFile);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			// Epsilon
			String currentLine = reader.readLine();
			currentLine = currentLine.substring(9);
			player.epsilon = Double.parseDouble(currentLine);
			// Generation
			currentLine = reader.readLine();
			currentLine = currentLine.substring(12);
			player.generation = Integer.parseInt(currentLine);
			reader.close();
			// Network
			player.networkHandler.setNetwork(Network.readNetwork(player.networkFile));
		} catch (Exception e) {
			System.err.println("Error Reading Player File");
			e.printStackTrace();
		}
		return player;
	}
		
}
