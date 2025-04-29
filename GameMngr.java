import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;

public class GameMngr {
    static ArrayList<HexCube> hexs = new ArrayList<>(); //Stores the state of each Hexagon
    static int playerTurn = 1;
    static int move = 0;
    static boolean isStartOfGame = true;
    static boolean isGameOver = false;
    static boolean isCapture = false;
    static boolean noMoreValidMove = true;
    static int redScore = 0;
    static int blueScore = 0;

    public static void resetStones() {
        for(HexCube hex: hexs) {
            hex.colour = 0;
        }
    }

    //Resets the game state variables to start a new game session.
    public static void resetBackEnd() {
        resetStones();
        playerTurn = 1;
        move = 0;
        isStartOfGame = true;
        isGameOver = false;
        isCapture = false;
        noMoreValidMove = true;
    }

    //Sets up the game window, UI components, and initializes the board layout.
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
    public static int validateMove(HexCube hex, boolean makeMove) {
        if (isGameOver) {
            return 0;
        }

        if (hex.colour != Colour.WHITE) {
            if (makeMove) FrontEnd.showInvalidMove();
            return 0;
        }

        GameMngr.removeStonesIfAny(hex,makeMove);

        if (GameMngr.isTouchingOwnGroup(hex) && !isCapture) {
            if (makeMove) FrontEnd.showInvalidMove();
            return 0;
        }

        if (makeMove) {
            move++;
        }
        return 1;
    }

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
    public static void removeStonesIfAny(HexCube hex, boolean makeMove){
        ArrayList<ArrayList<HexCube>> opponentGroups = new ArrayList<>();

        hex.colour = playerTurn;
        ArrayList<HexCube> playerGroup = GameMngr.getGroup(hex, hexs);

        for (HexCube playerStone: playerGroup) {
            for (HexCube neighbour : playerStone.getNeighbours(hexs)) {
                if (neighbour.colour != Colour.WHITE && neighbour.colour != hex.colour) {
                    ArrayList<HexCube> group = GameMngr.getGroup(neighbour, hexs);
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
                        GameMngr.removeStone(stone);
                    }
                }
            }
        }
    }

    //Finds all connected hexes of the same color starting from the given hex.
    public static ArrayList<HexCube> getGroup(HexCube hex, ArrayList<HexCube> hexList) {
        ArrayList<HexCube> group = new ArrayList<>();

        if (hex == null || hex.colour == Colour.WHITE) return group; // Ignore uncolored hexagons

        boolean[] visited = new boolean[hexList.size()]; // Track visited hexagons
        getGroupUtil(hex, hexList, visited, group);
        return group;
    }

    //Helper method for getGroup
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

    //Returns a text description of the current game state.
    public static Text gameStatus(){
        if (isGameOver && playerTurn == 1) {
            return new Text("Game Over! Red Wins!");
        } else if (isGameOver && playerTurn == 2) {
            return new Text("Game Over! Blue Wins!");
        }
        return new Text("To make a Move");
    }
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

    public static void removeStone(HexCube hex) {
        hex.colour = Colour.WHITE;
    }
    public static void setPlayerTurn(int player) {
        playerTurn = player;
    }
}
