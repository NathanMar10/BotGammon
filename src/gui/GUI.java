package gui;

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

import game.Backgammon;
import game.DiceRoll;


public class GUI extends Application {

	Backgammon game;
	PieceButton[] pieces;
	
	Scene gameScene;
	
	Stage diceStage;
	Stage gameStage;
	Stage startStage;
	
	GraphicsContext diceGraphics;
	VBox diceBox;
	Label welcomeLabel;
		
	OffButton offButton;
	

	int firstRow;

	public void start(Stage stage) {

		String welcomeMessage = "Welcome to BotGammon!";
		String sliderLabel = "Luck Factor";

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
					diceStage.show();
					updateDice();
					resetImage();
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
			
			Canvas gameCanvas = new Canvas(1500, 1000);
			gameCanvas.getGraphicsContext2D().drawImage(
					new Image("zimages/board.png"), 0, 0, 1500, 1000);
			gameGroup.getChildren().add(gameCanvas);
			
			// Creating Game Pieces
			pieces = new PieceButton[30];
			for (int i = 0; i < 30; i++) {
				pieces[i] = new PieceButton();
				gameGroup.getChildren().add(pieces[i]);
			}
			// Creating Row Buttons
			for (int i = 0; i < 24; i++) {
				RowButton thisButton = new RowButton(i, i < 12 ? 960: 10);
				gameGroup.getChildren().add(thisButton);
			}
			// Creating off Button
			offButton = new OffButton();
			gameGroup.getChildren().add(offButton);
			
			
			gameScene = new Scene(gameGroup, 1500, 1000);
			
			gameStage = new Stage();
			gameStage.setScene(gameScene);
			gameStage.setTitle("My Money is on Nate");
			
			gameStage.setX(330);
			gameStage.setY(50);
			gameStage.setOnCloseRequest(e-> {
				Platform.exit();
			});
			
			resetImage();
			
		} // End code for Game Scene
		
		
		
		{ // Code for Dice Scene
			
			diceStage = new Stage();
			diceStage.setX(0);
			diceStage.setY(450);
			
			Canvas diceCanvas = new Canvas(300, 150);
			diceGraphics = diceCanvas.getGraphicsContext2D();
			
			// Box to hold remaining moves
			diceBox = new VBox(10);
			diceBox.setPadding(new Insets(10));
			diceBox.setSpacing(9);
			
			// Button to skip roll
			Button newRoll = new Button();
			newRoll.setPrefSize(300,  20);
			newRoll.setText("Skip Turn (No Moves)");
			newRoll.setOnAction(e-> {
				game.switchTurnIfNoMoves();
				updateDice();
			});
			
			BorderPane dicePane = new BorderPane();
			dicePane.setLeft(diceCanvas);
			dicePane.setRight(diceBox);
			dicePane.setBottom(newRoll);
			
			
			Scene diceScene = new Scene(dicePane,330, 180, Color.GRAY);
			diceStage.setScene(diceScene);
			
		} // End Code for Dice Scene
		

		stage.setOnCloseRequest(e-> {
			Platform.exit();
		});
		
		stage.show();
	}
	
	private void resetImage() {
		int[] board = game.getBoard();
		int pieceNum = 0;
		
		// Places in-play pieces
		for (int i = 0; i < 24; i++) {
			for (int num = 0; num < Math.abs(board[i]); num++) {
				pieces[pieceNum].setLayoutX(getXFromRow(i));
				pieces[pieceNum].setLayoutY(getYFromRow(i, num));
				pieces[pieceNum].setVisible(true);
				pieces[pieceNum].row = i;
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
			pieces[pieceNum].setLayoutX(693);
			pieces[pieceNum].setLayoutY(375 - 50 * i);
			pieces[pieceNum].setVisible(true);
			pieces[pieceNum].setStyle("-fx-base: #ffffff");
			pieces[pieceNum].row = 100;
			pieceNum++;
		}
		// Places pieces into Black Jail
		for (int i = 0; i < game.getJail1(); i++) {
			pieces[pieceNum].setLayoutX(693);
			pieces[pieceNum].setLayoutY(543 + 50 * i);
			pieces[pieceNum].setVisible(true);
			pieces[pieceNum].setStyle("-fx-base: #000000");
			pieces[pieceNum].row = 101;
			pieceNum++;
		}
		// Hides Extra Pieces
		for (; pieceNum < 30; pieceNum++) {
			pieces[pieceNum].setVisible(false);
			
		}
		// Makes off Button visible based on progression
		if (game.getWhiteOff() || game.getBlackOff()) {
				offButton.setVisible(true);
		} else {
				offButton.setVisible(false);
		}

		switch (game.isGameOver()) {
		case 1: welcomeLabel.setText("White Wins!"); startStage.show(); break;
		case -1: welcomeLabel.setText("Black Wins!"); startStage.show(); break;
		}
	}
	private void updateDice() {
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
			return 1374 - row * 119; 
		} else if (row < 12) {
			return 608 - (row - 6) * 112;
		} else if (row < 18) {
			return 48 + (row - 12) * 112;
		} else {
			return 780 + (row - 18) * 119;
		}
	}
	private int getYFromRow(int row, int num) {
		if (row < 12) {
			return 885 - num * 60;
		} else {
			return 40 + num * 60;
		}
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	private class PieceButton extends Button {
		int row;
		PieceButton() {
			this.setShape(new Circle(100));
			this.setMinSize(75, 75);
			this.setOnAction(e -> {
					firstRow = row;
			});
		}	
	}
	private class RowButton extends Button {
		RowButton(int row, int y) {
			this.setMinSize(75, 30);
			this.setStyle("-fx-base: #605040");
			this.setLayoutX(getXFromRow(row));
			this.setLayoutY(y);
			
			this.setOnAction(e -> {
				if (firstRow != -1) {
					game.movePiece(firstRow, row);
					//firstRow = -1;
					updateDice();
					resetImage();
				}
			});
		}
	}
	private class OffButton extends Button {
		OffButton() {
			Label offLabel = new Label("Move Piece Off");
			offLabel.setRotate(90);
			this.setGraphic(new Group(offLabel));
			this.setPrefSize(50, 400);
			this.setStyle("-fx-base: #605040");
			this.setLayoutX(1450);
			this.setLayoutY(300);
			
			this.setOnAction(e -> {
				if (game.getWhiteTurn()) {
					for (int i = -1; i > -7; i--) {
						if (game.movePiece(firstRow, i)) {
							resetImage();
							updateDice();
							return;
						}
					}
				} else {
					for (int i = 24; i < 30; i++) {
						if (game.movePiece(firstRow, i)) {
							resetImage();
							updateDice();
							return;
						}
					}
				}
			});
		}

	}
}
