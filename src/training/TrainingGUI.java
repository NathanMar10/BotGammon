package training;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.image.*;
import javafx.stage.Stage;

import ai.Player;
import game.Backgammon;
import game.DiceRoll;


public class TrainingGUI extends Application {

	public static TrainingGUI currentGUI;
	
	public static final String playerFile = "theRun.txt";
	
	Player player = Player.loadPlayer(playerFile);;
	
	Backgammon game;
	PieceButton[] pieces;
	
	Scene gameScene;
	
	Stage diceStage;
	Stage gameStage;
	Stage startStage;
	
	GraphicsContext diceGraphics;
	VBox diceBox;
	Label welcomeLabel;

	static boolean showDice = false;
	public static long gameDelay, graphicUpdateRate;

	
	public void start(Stage stage) {
		currentGUI = this;
		
		String welcomeMessage = "BotGammon Training";
		String sliderLabel = "Useless Slider";

		{ // Code for Start Scene ----------------------------------------------
			
			BorderPane startPane = new BorderPane();

			// Text at the top of the screen
			welcomeLabel = new Label(welcomeMessage);

			{
				welcomeLabel.setFont(new Font("Segoe UI", 40));
				BorderPane.setAlignment(welcomeLabel, Pos.CENTER);
			}

			// Main start Button
			Button startButton = new Button("Start");
			{
				startButton.setFont(new Font("Segoe UI", 40));
				startButton.setMinWidth(400);
				startButton.setMinHeight(250);
				startButton.setOnAction(e -> {
					game = new Backgammon();
					gameStage.show();
					startStage.hide();
					if (showDice) diceStage.show();
					updateDice();
					resetImage();
					
					Thread thread = new Thread(player);
					thread.start();
				});
			}

			// Configuration Labels and Buttons
			Label sceneSizeLabel = new Label(sliderLabel);

			Slider sceneSizeSlider = new Slider();
			{
				sceneSizeSlider.setShowTickMarks(true);
				sceneSizeSlider.setShowTickLabels(true);
				sceneSizeSlider.setMin(0);
				sceneSizeSlider.setMax(300);
				sceneSizeSlider.setValue(100);
				sceneSizeSlider.setMajorTickUnit(50);
				sceneSizeSlider.setPrefWidth(400);
				sceneSizeSlider.setSnapToTicks(true);
			}

			// Configuration Row Setup
			HBox config = new HBox(20);
			{
				config.setAlignment(Pos.CENTER);
				config.setPadding(new Insets(10));
				config.getChildren().add(sceneSizeLabel);
				config.getChildren().add(sceneSizeSlider);
				BorderPane.setAlignment(config, Pos.CENTER);
			}

			// Adding Elements to the startPane
			{
				startPane.setTop(welcomeLabel);
				startPane.setBottom(config);
				startPane.setCenter(startButton);
			}

			// Creating the Starting Scene
			Scene startScene = new Scene(startPane, 600, 400);
			startStage = stage;
			startStage.setScene(startScene);
			
		} // End code for Start Scene ------------------------------------------
		
		
		
		{ // Code for Game Scene
			
			game = new Backgammon();
			
			// Drawing Game Board
			Group gameGroup = new Group();
			
			Canvas gameCanvas = new Canvas(750, 500);
			gameCanvas.getGraphicsContext2D().drawImage(new Image("zimages/board.png"), 0, 0, 750, 500);
			gameGroup.getChildren().add(gameCanvas);
			
			// Creating Game Pieces
			pieces = new PieceButton[30];
			for (int i = 0; i < 30; i++) {
				pieces[i] = new PieceButton();
				gameGroup.getChildren().add(pieces[i]);
			}
			
			// Training Sliders
			HBox sliderArea = new HBox();
			{
				VBox delayBox = new VBox(5);
				Slider delaySlider = new Slider();
				delaySlider.setShowTickMarks(true);
				delaySlider.setShowTickLabels(true);
				delaySlider.setMin(1);
				delaySlider.setMax(100);
				delaySlider.setValue(1);
				delaySlider.setMajorTickUnit(50);
				delaySlider.setPrefWidth(300);
				delaySlider.setSnapToTicks(false);
				gameDelay = (long)delaySlider.getValue();
				delaySlider.valueProperty().addListener(e -> {
					gameDelay = (long)delaySlider.getValue();
				});
				Label delayLabel = new Label("Game Delay");
				delayBox.setAlignment(Pos.CENTER);
				delayBox.getChildren().add(delayLabel);
				delayBox.getChildren().add(delaySlider);
				
				VBox updateBox = new VBox(5);
				Slider updateSlider = new Slider();
				updateSlider.setShowTickMarks(true);
				updateSlider.setShowTickLabels(true);
				updateSlider.setMin(0);
				updateSlider.setMax(10);
				updateSlider.setValue(1);
				updateSlider.setMajorTickUnit(1);
				updateSlider.setPrefWidth(300);
				updateSlider.setSnapToTicks(true);
				graphicUpdateRate = (long)updateSlider.getValue();
				updateSlider.valueProperty().addListener(e -> {
					graphicUpdateRate = (long)updateSlider.getValue();
				});
				Label updateLabel = new Label("Graphic Update Delay");
				updateBox.setAlignment(Pos.CENTER);
				updateBox.getChildren().add(updateLabel);
				updateBox.getChildren().add(updateSlider);
				
				sliderArea.getChildren().add(delayBox);
				sliderArea.getChildren().add(updateBox);
				sliderArea.setPrefSize(750, 50);
				sliderArea.setSpacing(100);
				sliderArea.setPadding(new Insets(10, 50, 0, 50));
			}
			
			
			
			// Finalizing Game Scene
			BorderPane gamePane = new BorderPane();
			gamePane.setCenter(gameGroup);
			gamePane.setBottom(sliderArea);
			gameScene = new Scene(gamePane, 750, 575);
			
			gameStage = new Stage();
			gameStage.setScene(gameScene);
			gameStage.setTitle("My Money is on Nate");
			
			gameStage.setX(1120);
			gameStage.setY(50);
			gameStage.setOnCloseRequest(e-> {
				Platform.exit();
			});
			
		} // End code for Game Scene
		
		
		
		{ // Code for Dice Scene
			
			diceStage = new Stage();
			diceStage.setX(1540);
			diceStage.setY(600);
			
			Canvas diceCanvas = new Canvas(300, 150);
			diceGraphics = diceCanvas.getGraphicsContext2D();
			
			// Box to hold remaining moves
			diceBox = new VBox(10);
			diceBox.setPadding(new Insets(10));
			diceBox.setSpacing(9);
			
			BorderPane dicePane = new BorderPane();
			dicePane.setLeft(diceCanvas);
			dicePane.setRight(diceBox);
			
			
			Scene diceScene = new Scene(dicePane,330, 150, Color.GRAY);
			diceStage.setScene(diceScene);
			
		} // End Code for Dice Scene
		

		stage.setOnCloseRequest(e-> {
			Platform.exit();
		});
		
		stage.show();
	}
	
	void resetImage() {
		int[] board = game.getBoard();
		int pieceNum = 0;
		
		synchronized(game) {
			
		
		// Places in-play pieces
		for (int i = 0; i < 24; i++) {
			for (int num = 0; num < Math.abs(board[i]); num++) {
				pieces[pieceNum].setLayoutX(getXFromRow(i));
				pieces[pieceNum].setLayoutY(getYFromRow(i, num));
				pieces[pieceNum].setVisible(true);
				if (board[i] < 0) {
					pieces[pieceNum].setStyle("-fx-base: #000000");
				} else {
					pieces[pieceNum].setStyle("-fx-base: #ffffff");
				}
				pieceNum++;
			}
		}
		// Places pieces into White Jail
		for (int i = 0; i < game.getJail0(); i++) {
			pieces[pieceNum].setLayoutX(346);
			pieces[pieceNum].setLayoutY(188 - 25 * i);
			pieces[pieceNum].setVisible(true);
			pieces[pieceNum].setStyle("-fx-base: #ffffff");
			pieceNum++;
		}
		// Places pieces into Black Jail
		for (int i = 0; i < game.getJail1(); i++) {
			pieces[pieceNum].setLayoutX(346);
			pieces[pieceNum].setLayoutY(272 + 25 * i);
			pieces[pieceNum].setVisible(true);
			pieces[pieceNum].setStyle("-fx-base: #000000");
			pieceNum++;
		}
		// Hides Extra Pieces
		for (; pieceNum < 30; pieceNum++) {
			pieces[pieceNum].setVisible(false);
			
		}
		}
	}
	void updateDice() {
		DiceRoll roll = game.getThisRoll();
		String turn = game.getWhiteTurn() ? "white": "black";
		diceGraphics.clearRect(0, 0, 450, 150);
		diceGraphics.drawImage(new Image("zimages/" + turn + "_" + roll.getFirstDie() + ".png"), 0, 0, 150, 150);
		diceGraphics.drawImage(new Image("zimages/" + turn + "_" + roll.getSecondDie() + ".png"), 150, 0, 150, 150);
		diceBox.getChildren().clear();
		for (int i : roll.movesLeft) {
			Label thisLabel = new Label(Integer.toString(i));
			thisLabel.setFont(new Font("Roboto", 16));
			diceBox.getChildren().add(thisLabel);
		}
	}
	
	private int getXFromRow(int row) {
		if (row < 6) {
			return 687 - row * 59; 
		} else if (row < 12) {
			return 304 - (row - 6) * 56;
		} else if (row < 18) {
			return 24 + (row - 12) * 56;
		} else {
			return 390 + (row - 18) * 59;
		}
	}
	private int getYFromRow(int row, int num) {
		if (row < 12) {
			return 442 - num * 30;
		} else {
			return 20 + num * 30;
		}
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	private class PieceButton extends Button {
		PieceButton() {
			this.setShape(new Circle(100));
			this.setMinSize(37, 37);
		}	
	}
	
	public void resetBoard() {
			game = new Backgammon();
			Thread thread = new Thread(player);
			thread.start();
		}
	}

