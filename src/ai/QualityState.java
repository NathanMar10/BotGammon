package ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/*
 * QualityState class is customized by the user to work in the Neural Network.
 * The Neural Network relies on the getInput and getOutput methods of this class.
 * Methods that must be called before the NN back propagates, call them in setupForLearning().
 */
public class QualityState {

	// Stores Past States
	public static LinkedList<QualityState> stateQueue = new LinkedList<QualityState>();
	public static ArrayList<QualityState> stateList = new ArrayList<QualityState>();
	// Variables to Affect Future Ratings
	private static double decrementPower = .93;
	private static int futureMoves = 1;
	private static int avgRating, maxRating, minRating;
	static {
		double[] futureCoeffs = calcCoeffs();
		for (int i = 0; i < futureMoves; i++) {
			avgRating += futureCoeffs[i] * 11.5;
			maxRating += futureCoeffs[i] * 24;
			minRating += futureCoeffs[i] * -1;
		}
		
	}
	
	// State of Game Variables
	private int[] allPositions;
	// Rating Values
	private double futureRating, currentRating;


	
	public QualityState(int[] board, int moveNumber) {
		allPositions = board.clone();
		currentRating = calcAveragePosition();
		stateList.add(this);
		stateQueue.add(this);
	}

	public double getCurrentRating() {
		return currentRating;
	}
	
	private double calcAveragePosition() {
		int sum = 0;
		int weightedSum = 0;
		for (int i = 0; i < 28; i++) {
			sum += Math.abs(allPositions[i]);
			if (i < 24) {
				weightedSum += Math.abs(allPositions[i]) * i;
			}
		}
		weightedSum += (allPositions[24] + allPositions[27]) * 24;
		weightedSum += (allPositions[25] + allPositions[26]) * -1;
		
		return (double)weightedSum / sum;
	}
	
	private static void calcFutureRatings() {
		double[] coeffs = calcCoeffs();
		for (int i = 0; i < stateList.size(); i++) {
			stateList.get(i).futureRating = 0;
			for (int j = 0; j < futureMoves; j++) {
				if (i + j < stateList.size()) {
					stateList.get(i).futureRating += 
							coeffs[j] * stateList.get(i + j).currentRating;
				} else {
					stateList.get(i).futureRating += 
							coeffs[j] * stateList.get(i).currentRating;
				}
			}
			stateList.get(i).futureRating *= .5 / avgRating;
			stateList.get(i).futureRating *= stateList.get(i).futureRating / .5;
		}
	}
	private static double[] calcCoeffs() {
		double[] coeffs = new double[futureMoves];
		for (int i = 0; i < futureMoves; i++) {
			coeffs[i] = Math.pow(decrementPower, i);
		}
		return coeffs;
	}
	public static void clearList() {
		stateList.clear();
	}
	
	public static double[][][] getStatesAsArray() {
		double[][][] allValues = new double[2][][];
		allValues[0] = new double[stateQueue.size()][];
		allValues[1] = new double[stateQueue.size()][];
		for (int j = 0; j < stateQueue.size(); j++) {
			allValues[0][j] = stateQueue.get(j).getAllPositionsDouble();
			allValues[1][j] = new double[] {stateQueue.get(j).futureRating};
		}
		//System.out.println(Arrays.deepToString(allValues));
		return allValues;
		
	}
	
	public double[] getAllPositionsDouble() {
		double[] allPosDouble = new double[allPositions.length];
		for (int i = 0; i < allPositions.length; i++) {
			allPosDouble[i] = (double)allPositions[i];
		}
		return allPosDouble;
	}
	// Provided : Setup according to your problem
	static void setupForLearning() {
		calcFutureRatings();
	}
	int[] getInputs() {
		return allPositions;
	}
	double getOutput() {
		return futureRating;
	}
	
	@Override
	public String toString() {
		return Double.toString(futureRating);
	}
}
