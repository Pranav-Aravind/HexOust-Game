import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;

/**
 * Represents the main game logic for HexOust.
 * Manages the game state, player turns, and win conditions.
 */
public class GameManager {
    static ArrayList<HexCube> hexs = new ArrayList<>(); //Stores the state of each Hexagon
    static int playerTurn = 1;
    static int move = 0;
    static boolean isStartOfGame = true;
    static boolean isGameOver = false;
    static boolean isCapture = false;
    static boolean noMoreValidMove = true;
    static int redScore = 0;
    static int blueScore = 0;

    /**
     * Resets the color of all stones to white
     */
    public static void resetStones() {
        for(HexCube hex: hexs) {
            hex.colour = 0;
        }
    }

    /**
     * Resets the game state variables to start a new game session.
     */
    public static void resetBackEnd() {
        resetStones();
        playerTurn = 1;
        move = 0;
        isStartOfGame = true;
        isGameOver = false;
        isCapture = false;
        noMoreValidMove = true;
    }

    /**
     * Sets up the game window, UI components, and initializes the board layout.
     *
     * @param stage the primary window (JavaFX Stage) where the game scene is displayed
     */
    public static void startGame(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 700, 700, javafx.scene.paint.Color.WHITE);
        stage.setTitle("HexOust");
        stage.setScene(scene);
        stage.show();

        // Call method to draw hex grid dynamically, including UI elements
        FrontEnd.updateGameUI(root, scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> FrontEnd.updateGameUI(root, scene));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> FrontEnd.updateGameUI(root, scene));

        double size = 25;
        double originX = scene.getWidth() / 2;
        double originY = scene.getHeight() / 2;

        Layout layout = new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));
    }

    /**
     * checks whether a move is valid and also removes stones if possible
     *
     * @param hex the stone that will be placed
     * @param makeMove true if it's making a move, false for when checking for noMoreValidMove
     * @return 1 if the move is valid
     */
    public static int validateMove(HexCube hex, boolean makeMove) {
        if (isGameOver) {
            return 0;
        }

        if (hex.colour != Colour.WHITE) {
            if (makeMove) FrontEnd.showInvalidMove();
            return 0;
        }

        GameManager.removeStonesIfAny(hex,makeMove);

        if (GameManager.isTouchingOwnGroup(hex) && !isCapture) {
            if (makeMove) FrontEnd.showInvalidMove();
            return 0;
        }

        if (makeMove) {
            move++;
        }
        return 1;
    }

    /**
     * checks the number of stones of each player and if one reaches zero the opponent is declared winner
     *
     * @return a string that represents the winner if there's one
     */
    public static String checkAndDeclareWinner() {
        if (!isStartOfGame && !isGameOver) {
            boolean noRedStones = hexs.stream().noneMatch(h -> h.colour == Colour.RED);
            boolean noBlueStones = hexs.stream().noneMatch(h -> h.colour == Colour.BLUE);

            if (noBlueStones) {
                isGameOver = true;
                redScore++;
                return "RED";
            } else if (noRedStones) {
                isGameOver = true;
                blueScore++;
                return "BLUE";
            }
        }
        return null;
    }

    /**
     * checks if hex is touching its own group
     *
     * @param hex the stone that will be placed
     * @return true if hex is touching own group
     */
    public static boolean isTouchingOwnGroup(HexCube hex){
        boolean TouchingOwnGroup = false;
        for (HexCube neighbour : hex.getNeighbours(hexs)) {
            if (neighbour.colour == hex.colour) {
                TouchingOwnGroup = true;
                break;
            }
        }
        hex.colour=0;
        return TouchingOwnGroup;
    }

    /**
     * first gets all the opponent groups,
     * then compares the player's group size with the size of each of the opponent groups,
     * removing the opponent group's stones if player's group size is greater
     *
     * @param hex the stone that will be placed
     * @param makeMove true if it's making a move, false for when checking for noMoreValidMove
     */
    public static void removeStonesIfAny(HexCube hex, boolean makeMove){
        ArrayList<ArrayList<HexCube>> opponentGroups = new ArrayList<>();

        hex.colour = playerTurn;
        ArrayList<HexCube> playerGroup = GameManager.getGroup(hex, hexs);

        for (HexCube playerStone: playerGroup) {
            for (HexCube neighbour : playerStone.getNeighbours(hexs)) {
                if (neighbour.colour != Colour.WHITE && neighbour.colour != hex.colour) {
                    ArrayList<HexCube> group = GameManager.getGroup(neighbour, hexs);
                    if (!opponentGroups.contains(group)) {
                        opponentGroups.add(group);
                    }
                }
            }
        }

        for (ArrayList<HexCube> opponentGroup : opponentGroups) {
            if (playerGroup.size() > opponentGroup.size()) {
                isCapture = true;
                if (makeMove) {
                    for (HexCube stone : opponentGroup) {
                        GameManager.removeStone(stone);
                    }
                }
            }
        }
    }

    /**
     * Finds all connected hexes of the same color starting from the given hex.
     *
     * @param hex the stone that will be placed
     * @param hexList the list of all hexagons
     * @return the group of hexs
     */
    public static ArrayList<HexCube> getGroup(HexCube hex, ArrayList<HexCube> hexList) {
        ArrayList<HexCube> group = new ArrayList<>();

        if (hex == null || hex.colour == Colour.WHITE) return group; // Ignore uncolored hexagons

        boolean[] visited = new boolean[hexList.size()]; // Track visited hexagons
        getGroupUtil(hex, hexList, visited, group);
        return group;
    }

    /**
     * Determines the group of connected hexagons by recursively checking unvisited neighbors of the same color.
     *
     * @param hex the stone that will be placed
     * @param hexList the list of all hexagons
     * @param visited used to track visited hexagons
     * @param group the group of hexagons the hex belongs to
     * @return an int indicating the hex's group size
     */
    private static int getGroupUtil(HexCube hex, ArrayList<HexCube> hexList, boolean[] visited, ArrayList<HexCube> group) {
        int index = hexList.indexOf(hex);
        if (index == -1 || visited[index]) return 0; // Prevent revisiting

        visited[index] = true;
        group.add(hex);

        int groupSize = 1;

        for (HexCube neighbor : hex.getNeighbours(hexList)) {
            if (neighbor.colour == hex.colour && !visited[hexList.indexOf(neighbor)]) {
                groupSize += getGroupUtil(neighbor, hexList, visited, group);
            }
        }
        return groupSize;
    }


    /**
     * Decides what the current game status is
     *
     * @return a text description of the current game state.
     */
    public static Text gameStatus(){
        if (isGameOver && playerTurn == 1) {
            return new Text("Game Over! Red Wins!");
        } else if (isGameOver && playerTurn == 2) {
            return new Text("Game Over! Blue Wins!");
        }
        return new Text("To make a Move");
    }

    /**
     * Decides which player plays after a move
     *
     * @param playerTurn used to decide whose turn it is
     * @param isCheckForNoMoreValidMove used when checking for if there's noMoreValidMove for a player, and if so turn automatically goes to the opponent
     */
    public static void playerTurnDecider(int playerTurn, boolean isCheckForNoMoreValidMove) {
        if(isCheckForNoMoreValidMove) {
            if (noMoreValidMove && !isGameOver) {
                if (playerTurn == 1) {
                    setPlayerTurn(2);
                } else {
                    setPlayerTurn(1);
                }
                noMoreValidMove = true;
            }
        } else {
            if (!isCapture) {
                if (playerTurn == 1) {
                    setPlayerTurn(2);
                } else {
                    setPlayerTurn(1);
                }
                isCapture = false;
            }
        }
    }

    /**
     * removes stone by making its color white
     */
    public static void removeStone(HexCube hex) {
        hex.colour = Colour.WHITE;
    }

    /**
     * set player turn to be a specific player's
     */
    public static void setPlayerTurn(int player) {
        playerTurn = player;
    }
}
