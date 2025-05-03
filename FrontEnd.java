import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles all user interface rendering and interaction logic for the HexOust game.
 * This includes drawing the board, managing input events, and updating UI elements.
 */

public class FrontEnd {
    private static Text invalidMoveText;
    public static Sphere sphere; // Store sphere as instance variable

    /**
     * Updates the user interface by clearing the board and rendering the current game state based on backend data.
     *
     * @param root the root group node where all UI elements are added or cleared
     * @param scene the scene that contains the game view, used for layout calculations
     */
    public static void updateGameUI(Group root, Scene scene) {
        clearUI(root);
        Layout layout = setupLayout(scene);
        HashMap<String, HexCube> hexMap = buildHexMap();//store all hexagons and make them easily findable
        renderHexGrid(root, layout, hexMap);
        updateScene(scene, root);

        if (GameManager.move == 2) {
            GameManager.isStartOfGame = false;
        }
    }

    /**
     * Checks if the game has ended by evaluating the current game state.
     * If a winner is found, it prints the result to the console and updates the UI accordingly.
     */
    public static void checkGameOverAndUpdateUI() {
        String winner = GameManager.checkAndDeclareWinner();
        if (winner != null && sphere != null && sphere.getParent() != null) {
            System.out.println(winner + " WINS!");
            updateGameUI((Group) sphere.getParent(), sphere.getScene());
        }
    }

    /**
     * Registers a mouse click event on the given hexagon to handle player moves.
     * Validates the move, updates the game state and board color, switches turns,
     * and refreshes the UI after each valid move.
     *
     * @param hex the stone that will be placed
     * @param hexagon the corresponding UI hexagon representing the stone in the backend
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void processAndUpdateMoveOnBoard(HexCube hex, Polygon hexagon, Group root) {
        if (GameManager.validateMove(hex, false) == 1) {
            GameManager.noMoreValidMove = false;
        }
        GameManager.isCapture = false;

        hexagon.setOnMouseClicked(event -> {
            if (GameManager.validateMove(hex, true) == 1) {
                if (GameManager.playerTurn == 1) {
                    hex.colour = Colour.RED;
                } else {
                    hex.colour = Colour.BLUE;
                }
                GameManager.playerTurnDecider(GameManager.playerTurn, false);
                GameManager.isCapture = false;
                updateGameUI((Group) sphere.getParent(), sphere.getScene());
            }
            FrontEnd.checkGameOverAndUpdateUI();
        });
        root.getChildren().add(hexagon);
    }

    /**
     * Creates and configures an "Invalid Move!" text element for the UI.
     * The message is styled, dynamically positioned near the bottom center of the scene,
     * and added to the root group in a hidden state.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */

    public static void invalidMove(Scene scene, Group root){
        invalidMoveText = new Text("Invalid Move!");
        invalidMoveText.setFill(Color.PURPLE);
        invalidMoveText.setFont(Font.font("Arial", 20));
        invalidMoveText.setTranslateX(scene.getWidth() / 2 - 60);  // Centering dynamically
        invalidMoveText.setTranslateY(scene.getHeight() - 45);
        invalidMoveText.setVisible(false); // Initially hidden

        root.getChildren().add(invalidMoveText);
    }

    /**
     * Creates a hexagon-shaped Polygon using the given list of corner points.
     *
     * @param corners a list of points representing the vertices of the hexagon
     * @return a Polygon object representing the hexagon shape
     */
    public static javafx.scene.shape.Polygon createHexagon(ArrayList<Point> corners) {
        javafx.scene.shape.Polygon hex = new javafx.scene.shape.Polygon();
        for (Point p : corners) {
            hex.getPoints().addAll(p.x, p.y);
        }
        return hex;
    }

    /**
     * Creates a hexagon shape for the given hex and applies fill color based on its state.
     *
     * @param hex the stone that will be placed
     * @param layout the layout used to calculate the hexagon's corner positions
     * @return the formatted Polygon representing the hexagon with appropriate styling
     */

    public static Polygon createAndFormatHexagon(HexCube hex, Layout layout) {
        ArrayList<Point> corners = layout.polygonCorners(hex);
        Polygon hexagon = createHexagon(corners);

        switch (hex.colour) {
            case Colour.RED:
                hexagon.setFill(Color.RED);
                break;
            case Colour.BLUE:
                hexagon.setFill(Color.BLUE);
                break;
            default:
                hexagon.setFill(Color.WHITE);
        }
        hexagon.setStroke(Color.BLACK);
        return hexagon;
    }

    /**
     * Adds a hover effect to the given hexagon based on the current game state and player turn.
     * When hovered, the hexagon is filled with a semi-transparent color to indicate a valid move.
     * The fill resets when the mouse exits.
     *
     * @param hexagon the corresponding UI hexagon representing the stone in the backend
     * @param hex the stone that will be placed
     */
    public static void addHoverOverEffect(Polygon hexagon, HexCube hex){
        hexagon.setOnMouseEntered(event -> {
            if (hex.colour == Colour.WHITE) {
                if (GameManager.validateMove(hex, false) == 1) {
                    GameManager.isCapture = false;
                    if (GameManager.playerTurn == 1) {
                        hexagon.setFill(Color.color(1, 0, 0, 0.3));
                    } else {
                        hexagon.setFill(Color.color(0, 0, 1, 0.3));
                    }
                }
            }
        });
        hexagon.setOnMouseExited(event -> {
            if (hex.colour == Colour.WHITE) { // Only reset color if it's uncolored
                hexagon.setFill(Color.WHITE);
            }
        });
    }

    /**
     * Displays the "Invalid Move!" message briefly on the screen and hides it after one second.
     */
    public static void showInvalidMove() {

        if (invalidMoveText == null) return;

        invalidMoveText.setVisible(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            invalidMoveText.setVisible(false);
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * Displays the game title "HexOust" at the top center of the screen.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void Title(Scene scene, Group root){
        Text title = new Text("HexOust");
        title.setFill(javafx.scene.paint.Color.BLACK);
        title.setFont(javafx.scene.text.Font.font("Consolas", 35));
        title.setTranslateX(scene.getWidth() / 2 - 60); // Centering dynamically
        title.setTranslateY(50);
        root.getChildren().add(title);
    }

    /**
     * Displays instructional or status text on the screen based on the current game state.
     * The position is dynamically adjusted depending on whether the game is over.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void InstructionsText(Scene scene, Group root){
        Text text = GameManager.gameStatus();
        text.setFill(javafx.scene.paint.Color.BLACK);
        text.setFont(Font.font("Arial", 20));
        text.setTranslateX(scene.getWidth() / 6 - 30); // Adjust dynamically
        if (GameManager.isGameOver) {
            text.setTranslateX(scene.getWidth() / 2.5 - 30);
        }
        text.setTranslateY(scene.getHeight() - 40);
        root.getChildren().add(text);
    }

    /**
     * Creates and displays an "Exit" button in the top-right corner of the screen.
     * When clicked, it closes the game window and terminates the application.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void Exit(Scene scene, Group root){
        javafx.scene.control.Button exit = new javafx.scene.control.Button("Exit");
        exit.setPrefSize(70, 30);
        exit.setTranslateX(scene.getWidth() - 80);
        exit.setTranslateY(10);
        exit.setOnAction(event -> {
            System.out.println("Closing HexOust");
            Stage stage = (Stage) exit.getScene().getWindow();
            stage.close();
        });
        root.getChildren().add(exit);
    }

    /**
     * Creates and displays a "Restart" button in the top-left corner of the screen.
     * When clicked, it resets the game state, closes the current window,
     * and launches a new game instance.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void RestartGame(Scene scene, Group root){
        javafx.scene.control.Button restart = new javafx.scene.control.Button("Restart");
        restart.setPrefSize(70, 30);
        restart.setTranslateX(20);
        restart.setTranslateY(10);
        restart.setOnAction(event -> {
            System.out.println("Restarting HexOust");
            GameManager.resetBackEnd();
            Stage stage = (Stage) restart.getScene().getWindow();
            stage.close();
            GameManager.startGame(new Stage());
        });
        root.getChildren().add(restart);
    }

    /**
     * Displays a scoreboard showing the number of wins for each player.
     * The scoreboard is styled with headers and placed in the bottom-right corner of the screen.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     * @param redWins the number of games won by the red player
     * @param blueWins the number of games won by the blue player
     */
    public static void ScoreBoard(Scene scene, Group root, int redWins, int blueWins) {
        GridPane scoreboard = new GridPane();
        scoreboard.setGridLinesVisible(true);

        // Headers
        Text redHeader = new Text("Red Wins ");
        redHeader.setFill(Color.RED);
        redHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text blueHeader = new Text("Blue Wins ");
        blueHeader.setFill(Color.BLUE);
        blueHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Scores
        Text redScore = new Text(String.valueOf(redWins));
        redScore.setFill(Color.BLACK);
        redScore.setFont(Font.font("Arial", 16));

        Text blueScore = new Text(String.valueOf(blueWins));
        blueScore.setFill(Color.BLACK);
        blueScore.setFont(Font.font("Arial", 16));

        // Add to GridPane (col, row)
        scoreboard.add(redHeader, 0, 0);
        scoreboard.add(blueHeader, 1, 0);
        scoreboard.add(redScore, 0, 1);
        scoreboard.add(blueScore, 1, 1);

        // Position the scoreboard at bottom right
        scoreboard.setLayoutX(scene.getWidth() / 1.5 + 40);
        scoreboard.setLayoutY(scene.getHeight() - 70);

        root.getChildren().add(scoreboard);
    }

    /**
     * Creates and displays a colored sphere representing the current player's turn.
     * The sphere is positioned near the bottom-left of the screen, and its color
     * changes based on which player's turn it is.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void Sphere(Scene scene, Group root){
        sphere = new Sphere(13);
        PhongMaterial material;
        material = new PhongMaterial();
        if (GameManager.playerTurn == 1) {
            material.setDiffuseColor(Color.RED);
        } else {
            material.setDiffuseColor(Color.BLUE);
        }
        sphere.setMaterial(material);
        sphere.setTranslateX(scene.getWidth() * 0.1);
        if (GameManager.isGameOver) {
            sphere.setTranslateX(scene.getWidth() * 0.325);
        }
        sphere.setTranslateY(scene.getHeight() - 50);
        root.getChildren().add(sphere);
    }

    /**
     * Updates the game scene by re-adding all UI components, including the title, instructions,
     * current player indicator, invalid move message, control buttons, and scoreboard.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @param root the root group node where all UI elements are added or cleared
     */
    public static void updateScene(Scene scene, Group root){
        Title(scene, root);
        InstructionsText(scene, root);
        Sphere(scene, root);
        invalidMove(scene,root);
        Exit(scene, root);
        RestartGame(scene, root);
        ScoreBoard(scene, root, GameManager.redScore, GameManager.blueScore);
    }

    /**
     * Clears all UI elements from the root group.
     *
     * @param root the root group node where all UI elements are added or cleared
     */
    private static void clearUI(Group root) {
        root.getChildren().clear();
    }

    /**
     * Creates and returns a new layout configuration for the hexagonal grid,
     * centered within the scene.
     *
     * @param scene the scene that contains the game view, used for layout calculations
     * @return a Layout object defining the hex grid orientation, size, and origin
     */
    private static Layout setupLayout(Scene scene) {
        double size = 25;
        double originX = scene.getWidth() / 2;
        double originY = scene.getHeight() / 2;
        return new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));
    }

    /**
     * Builds and returns a hash map of all hexes for quick lookup using their cube coordinates.
     * Each key is a string in the format "q,r,s" representing a hex's position.
     *
     * @return a HashMap mapping coordinate strings to their corresponding HexCube objects
     */
    private static HashMap<String, HexCube> buildHexMap() {
        HashMap<String, HexCube> hexMap = new HashMap<>();
        for (HexCube hex : GameManager.hexs) {
            String key = hex.q + "," + hex.r + "," + hex.s;
            hexMap.put(key, hex);
        }
        return hexMap;
    }

    /**
     * Renders the hexagonal game grid by creating and formatting hexagons based on cube coordinates.
     *
     * @param root the root group node where all UI elements are added or cleared
     * @param layout the layout used to calculate hexagon positioning and orientation
     * @param hexMap a map of coordinate keys to their corresponding HexCube objects
     */

    private static void renderHexGrid(Group root, Layout layout, HashMap<String, HexCube> hexMap) {
        int baseN = 6;

        for (int q = -baseN; q <= baseN; q++) {
            for (int r = -baseN; r <= baseN; r++) {
                int s = -q - r;
                if (Math.abs(s) > baseN) continue;

                String key = q + "," + r + "," + s;
                HexCube hex = hexMap.getOrDefault(key, new HexCube(q, r, s, Colour.WHITE, 0));

                if (!hexMap.containsKey(key)) {
                    GameManager.hexs.add(hex);
                }

                Polygon hexagon = createAndFormatHexagon(hex, layout);
                addHoverOverEffect(hexagon, hex);
                processAndUpdateMoveOnBoard(hex, hexagon, root);
            }
        }
        GameManager.playerTurnDecider(GameManager.playerTurn, true);
    }

}
