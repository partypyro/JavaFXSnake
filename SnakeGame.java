import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.*;
import javafx.event.EventHandler;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

public class SnakeGame extends Application implements EventHandler<KeyEvent> {
	private final int WIDTH = 30;
	private final int HEIGHT = 30;
	private GameBoard game = new GameBoard(WIDTH, HEIGHT);

	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws InterruptedException {
		MainMenu mainMenu = new MainMenu(game);
		Scene mainMenuScene = new Scene(mainMenu.getPane());
		stage.setScene(mainMenuScene);
		stage.show();
		stage.setTitle("Snake!");

		Snake snake = new Snake(Color.AQUA);
		for (Block a : snake.getBody())
			snake.setAlive(game.addBlock(a));

		game.drawBoard(snake);
		Scene gameScene = new Scene(game.getPane());
		gameScene.setOnKeyPressed(event -> {
			snake.setDirection(event);
		});

		GameOver gameOver = new GameOver(game, snake);
		Scene gameOverScene = new Scene(gameOver.getPane());

		AnimationTimer gameLoop = new AnimationTimer() {
			private long lastUpdate = 0;
			private boolean firstFood = false;

			@Override
			public void handle(long now) {
				if (gameOver.isRestart() && (now - lastUpdate >= 200_000_000 / (1 +  (mainMenu.getDifficulty() / 1.2)))
						&& mainMenu.isStart()) {
					stage.setScene(gameScene);
					if (!firstFood) {
						firstFood = true;
						game.generateFood();
					}
					if (!game.isOutOfBounds(snake.nextBlock()) && game.isCollided(snake.nextBlock())) {
						if (game.getBlock(snake.nextBlock().getX(), snake.nextBlock().getY()) instanceof Food) {
							game.removeBlock(game.getBlock(snake.nextBlock().getX(), snake.nextBlock().getY()));
							game.addBlock(snake.nextBlock());
							snake.grow();
							game.generateFood();
						} else
							snake.setAlive(false);
					}

					game.removeBlock(snake.getBody().get(snake.getBody().size() - 1));
					snake.move();
					if (game.isOutOfBounds(snake.getBody().get(0)))
						snake.setAlive(false);

					if (!snake.isAlive()) {
						stage.setScene(gameOverScene);
						gameOver.setRestart(false);
						mainMenu.setStart(false);
					}

					game.addBlock(snake.getBody().get(0));

					game.drawBoard(snake);

					lastUpdate = now;
				} else if (!gameOver.isRestart()) {
					System.out.println(snake.getScore());
					stage.setScene(gameOverScene);
				} else if (gameOver.isRestart() && !mainMenu.isStart()) {
					stage.setScene(mainMenuScene);
					snake.reset();
					game.reset();
					firstFood = false;
				}
			}
		};
		gameLoop.start();
	}

	public void handle(KeyEvent event) {

	}
}

class Snake {
	private ArrayList<Block> body = new ArrayList<Block>();
	private int direction = 0, score = 0;
	private boolean alive = true;
	private Color color;

	Snake(Color color) {
		this.color = color;
		body.ensureCapacity(3);
		body.add(0, new Block(5, 10, color));
		body.add(1, new Block(5, 11, color));
		body.add(2, new Block(5, 12, color));
	}

	public ArrayList<Block> getBody() {
		return this.body;
	}

	public void setDirection(KeyEvent key) {
		switch (key.getCode()) {
		case W:
			if (direction != 1)
				direction = 0;
			break;

		case S:
			if (direction != 0)
				direction = 1;
			break;

		case A:
			if (direction != 3)
				direction = 2;
			break;

		case D:
			if (direction != 2)
				direction = 3;
			break;

		default:
			break;
		}
	}

	public Block nextBlock() {
		Block firstBlock = body.get(0);
		Block newBlock = null;
		switch (direction) {
		case 0:
			newBlock = new Block(firstBlock.getX(), firstBlock.getY() - 1, color);
			break;

		case 1:
			newBlock = new Block(firstBlock.getX(), firstBlock.getY() + 1, color);
			break;

		case 2:
			newBlock = new Block(firstBlock.getX() - 1, firstBlock.getY(), color);
			break;

		case 3:
			newBlock = new Block(firstBlock.getX() + 1, firstBlock.getY(), color);
			break;

		default:
			break;
		}
		return newBlock;
	}

	public void move() {
		body.remove(body.size() - 1);
		body.add(0, nextBlock());
	}

	public void grow() {
		body.add(0, nextBlock());
		addScore();
	}

	public void addScore() {
		score += 1;
	}

	public int getScore() {
		return score;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean isAlive() {
		return alive;
	}

	public int getDirection() {
		return direction;
	}

	public void reset() {
		score = 0;
		direction = 0;
		body = new ArrayList<Block>();
		body.ensureCapacity(3);
		body.add(0, new Block(5, 10, color));
		body.add(1, new Block(5, 11, color));
		body.add(2, new Block(5, 12, color));
		alive = true;

	}
}

class Block {
	private static final int WIDTH = 15;
	private static final int HEIGHT = 15;
	private int x, y;
	private Rectangle2D bounds;
	private Rectangle rect;
	private Color color;

	Block(int x, int y, Color color) {
		this.x = x;
		this.y = y;
		bounds = new Rectangle2D(x * WIDTH, y * HEIGHT, WIDTH, HEIGHT);
		rect = new Rectangle(WIDTH, HEIGHT, color);
		this.color = color;
		rect.setX(x * WIDTH);
		rect.setY(y * HEIGHT);
	}

	public Rectangle getRect() {
		return rect;
	}

	public Rectangle2D getBounds() {
		return bounds;
	}

	public Color getColor() {
		return color;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isTouching(Block block) {
		return this.getBounds().contains(block.getBounds());
	}

	public static int getWidth() {
		return WIDTH;
	}

	public static int getHeight() {
		return HEIGHT;
	}
}

class Food extends Block {
	Food(int x, int y) {
		super(x, y, Color.GREEN);
	}
}

class GameBoard {
	private int width, height;
	private Block[][] board;
	private Pane pane = new Pane();

	GameBoard(int width, int height) {
		this.width = width;
		this.height = height;
		board = new Block[width][height];
		pane.setPrefSize(width * Block.getWidth(), height * Block.getHeight() + 30);
	}

	public boolean addBlock(Block block) {
		try {
			board[block.getX()][block.getY()] = block;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void removeBlock(Block block) {
		board[block.getX()][block.getY()] = null;
	}

	public Block getBlock(int x, int y) {
		if (isOutOfBounds(x, y))
			return null;
		if (board[x][y] instanceof Block)
			return board[x][y];
		else
			return null;
	}

	public void reset() {
		this.board = new Block[width][height];
	}

	public void drawBoard(Snake snake) {
		pane.getChildren().clear();
		for (Block[] a : board) {
			for (Block b : a) {
				if (b instanceof Block)
					pane.getChildren().add(b.getRect());
			}
		}
		Text score = new Text("Score: " + Integer.toString(snake.getScore()));
		score.setFont(Font.font(20));
		score.setY(height * Block.getHeight() + 20);
		pane.getChildren().add(score);
	}

	public Pane getPane() {
		return pane;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void generateFood() {
		int x, y;
		do {
			x = (int) (Math.random() * width);
			y = (int) (Math.random() * height);
		} while (getBlock(x, y) instanceof Block);
		addBlock(new Food(x, y));
	}

	public boolean isCollided(Block block) {
		if (getBlock(block.getX(), block.getY()) instanceof Block)
			return true;
		else
			return false;
	}

	public boolean isOutOfBounds(Block block) {
		if (block.getX() < 0 || block.getX() > (width - 1))
			return true;
		else if (block.getY() < 0 || block.getY() > (height - 1))
			return true;
		else
			return false;
	}

	public boolean isOutOfBounds(int x, int y) {
		if (x < 0 || x > width)
			return true;
		else if (y < 0 || y > height)
			return true;
		else
			return false;
	}

}

class MainMenu {
	private VBox pane = new VBox();
	private boolean startGame = false;
	private int diffIndex = 0;

	MainMenu(GameBoard game) {
		pane.setSpacing(10);
		pane.setStyle("-fx-alignment:center");
		pane.setPrefSize(game.getWidth() * Block.getWidth(), game.getHeight() * Block.getHeight());

		Text titleText = new Text("Snake!");
		titleText.setFont(Font.font(30));
		add(titleText);

		Button start = new Button("Start Game");
		start.setPrefWidth(200);
		start.setFont(Font.font(25));
		start.setOnAction(event -> {
			startGame = true;
		});
		add(start);

		Text difficultyText = new Text("Difficulty:");
		difficultyText.setFont(Font.font(20));
		Slider difficultySlider = new Slider(1, 5, 1);
		difficultySlider.setPrefWidth(200);
		difficultySlider.setMinorTickCount(0);
		difficultySlider.setMajorTickUnit(1.0f);
		difficultySlider.setShowTickMarks(true);
		difficultySlider.setShowTickLabels(true);
		difficultySlider.setBlockIncrement(1.0f);
		difficultySlider.setSnapToTicks(true);
		add(difficultyText);
		add(difficultySlider);
		diffIndex = pane.getChildren().indexOf(difficultySlider);
	}

	private void add(Node node) {
		pane.getChildren().add(node);
	}

	public boolean isStart() {
		return startGame;
	}

	public void setStart(boolean start) {
		startGame = start;
	}

	public VBox getPane() {
		return pane;
	}

	public int getDifficulty() {
		return (int) ((Slider) pane.getChildren().get(diffIndex)).getValue();
	}
}

class GameOver {
	private VBox pane = new VBox();
	private boolean restart = true;

	GameOver(GameBoard game, Snake snake) {
		pane.setSpacing(10);
		pane.setPrefSize(game.getWidth() * Block.getWidth(), game.getHeight() * Block.getHeight());
		pane.setStyle("-fx-alignment:center");
		Text gameOverText = new Text("Game Over!\nYour score was " + snake.getScore());
		gameOverText.setFont(Font.font(20));
		gameOverText.setX((game.getWidth() * Block.getWidth()) / 2 - (gameOverText.getLayoutBounds().getMaxX() / 2));
		gameOverText.setY((game.getHeight() * Block.getHeight()) / 2 - gameOverText.getLayoutBounds().getMaxY());
		add(gameOverText);

		Button retry = new Button("Retry?");
		retry.setFont(Font.font(20));
		retry.setOnAction(event -> {
			restart = true;
		});
		add(retry);
	}

	public VBox getPane() {
		return pane;
	}

	public boolean isRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	private void add(Node node) {
		pane.getChildren().add(node);
	}
}
