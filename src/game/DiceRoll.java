package game;
import java.util.ArrayList;
import java.util.Random;

public class DiceRoll implements Cloneable{
	
	private static Random random = new Random();
	private int[] values;
	public ArrayList<Integer> movesLeft;
	
	public DiceRoll() {
		movesLeft = new ArrayList<Integer>();
		values = new int[2];
		values[0] = random.nextInt(6) + 1;
		values[1] = random.nextInt(6) + 1;
		if (!isDoubles()) {
			movesLeft.add(values[0]);
			movesLeft.add(values[1]);
		} else {
			for (int i = 0; i < 4; i++) {
				movesLeft.add(values[1]);
			}
		}
	}
	private boolean isDoubles() {
		if (values[0] == values[1]) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public boolean isEmpty() {
		return movesLeft.isEmpty();
	}
	public int getFirstDie() {
		return values[0];
	}	
	public int getSecondDie() {
		return values[1];
	}
	
	
	public static boolean flipCoin() {
		if (random.nextInt(2) == 0) {
			return true;
		} else {
			return false;
		}
	}

	public DiceRoll removeRoll(int start, int stop) {
		if (start == 100) {
			movesLeft.remove(Integer.valueOf(Math.abs(24 - stop)));
		} else if (start == 101) {
			movesLeft.remove(Integer.valueOf(Math.abs(stop + 1)));
		} else {
			movesLeft.remove(Integer.valueOf(Math.abs(stop - start)));
		}
		return this;
	}
	
	@Override
	public DiceRoll clone() {
		DiceRoll newRoll = new DiceRoll();
		newRoll.values = values;
		newRoll.movesLeft = (ArrayList<Integer>)movesLeft.clone();
		return newRoll;
	}
}
