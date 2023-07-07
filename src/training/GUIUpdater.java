package training;

public class GUIUpdater implements Runnable {
	
	private int type;
	
	public GUIUpdater(int type) {
		this.type = type;
	}
	
	public void run() {
		if (type == 1) {
			TrainingGUI.currentGUI.resetImage();
			if (TrainingGUI.showDice) TrainingGUI.currentGUI.updateDice();
		} else if (type == 2) {
			TrainingGUI.currentGUI.resetBoard();
		}
	}
}
