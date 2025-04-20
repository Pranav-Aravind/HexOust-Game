import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;

public class FrontEnd {
    private static Text invalidMoveText;
    public static Sphere sphere; // Store sphere as instance variable

    public static void updateGameUI(Group root, Scene scene) {

        clearUI(root);
        Layout layout = setupLayout(scene);
        HashMap<String, HexCube> hexMap = buildHexMap();//store all hexagons and make them easily findable
        renderHexGrid(root, layout, hexMap);
        updateScene(scene, root);

        if (GameMngr.move == 2) {
            GameMngr.isStartOfGame = false;
        }
    }

    public static void processAndUpdateMoveOnBoard(HexCube hex, Polygon hexagon, Group root) {
        if (GameMngr.validateMove(hex, false) == 1) {
            GameMngr.noMoreValidMove = false;
        }
        GameMngr.isCapture = false;

        hexagon.setOnMouseClicked(event -> {
            if (GameMngr.validateMove(hex, true) == 1) {
                if (GameMngr.playerTurn == 1) {
                    hex.colour = Colour.RED;
                } else {
                    hex.colour = Colour.BLUE;
                }
                GameMngr.playerTurnDecider(GameMngr.playerTurn, false);
                GameMngr.isCapture = false;
                updateGameUI((Group) sphere.getParent(), sphere.getScene());
            }
            GameMngr.updateBoardIfGameOver(hexagon);
        });
        root.getChildren().add(hexagon);
    }

    public static void invalidMove(Scene scene, Group root){
        invalidMoveText = new Text("Invalid Move!");
        invalidMoveText.setFill(Color.PURPLE);
        invalidMoveText.setFont(Font.font("Arial", 20));
        invalidMoveText.setTranslateX(scene.getWidth() / 2 - 60);  // Centering dynamically
        invalidMoveText.setTranslateY(scene.getHeight() - 45);
        invalidMoveText.setVisible(false); // Initially hidden

        root.getChildren().add(invalidMoveText);
    }

    public static javafx.scene.shape.Polygon createHexagon(ArrayList<Point> corners) {
        javafx.scene.shape.Polygon hex = new javafx.scene.shape.Polygon();
        for (Point p : corners) {
            hex.getPoints().addAll(p.x, p.y);
        }
        return hex;
    }

    public static Polygon createAndFormatHexagon(HexCube hex, Layout layout) {
        ArrayList<Point> corners = layout.polygonCorners(hex);
        Polygon hexagon = createHexagon(corners);

        // Maintain color state
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

    public static void addHoverOverEffect(Polygon hexagon, HexCube hex){
        //Hover effect
        hexagon.setOnMouseEntered(event -> {
            if (hex.colour == Colour.WHITE) {
                if (GameMngr.validateMove(hex, false) == 1) {
                    GameMngr.isCapture = false;
                    if (GameMngr.playerTurn == 1) {
                        hexagon.setFill(Color.color(1, 0, 0, 0.3)); // RED with 30% opacity
                    } else {
                        hexagon.setFill(Color.color(0, 0, 1, 0.3)); // BLUE with 30% opacity
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

    public static void showInvalidMove() {
        invalidMoveText.setVisible(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            invalidMoveText.setVisible(false);
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    public static void updateScene(Scene scene, Group root){
        Title(scene, root);
        InstructionsText(scene, root);
        Sphere(scene, root);
        invalidMove(scene,root);
        Exit(scene, root);
    }
    public static void Title(Scene scene, Group root){
        Text title = new Text("HexOust");
        title.setFill(javafx.scene.paint.Color.BLACK);
        title.setFont(javafx.scene.text.Font.font("Consolas", 35));
        title.setTranslateX(scene.getWidth() / 2 - 60); // Centering dynamically
        title.setTranslateY(50);
        root.getChildren().add(title);
    }
    public static void InstructionsText(Scene scene, Group root){
        Text text = GameMngr.gameStatus();
        text.setFill(javafx.scene.paint.Color.BLACK);
        text.setFont(Font.font("Arial", 20));
        text.setTranslateX(scene.getWidth() / 6 - 30); // Adjust dynamically
        if (GameMngr.isGameOver) {
            text.setTranslateX(scene.getWidth() / 2.5 - 30);
        }
        text.setTranslateY(scene.getHeight() - 40);
        root.getChildren().add(text);
    }
    public static void Exit(Scene scene, Group root){
        javafx.scene.control.Button exit = new javafx.scene.control.Button("Exit");
        exit.setPrefSize(70, 30);
        exit.setTranslateX(scene.getWidth() - 80); // Position at top-right dynamically
        exit.setTranslateY(10);
        exit.setOnAction(event -> {
            System.out.println("Closing HexOust");
            Platform.exit();
        });
        root.getChildren().add(exit);
    }
    public static void Sphere(Scene scene, Group root){
        sphere = new Sphere(13);
        PhongMaterial material; // Store material for updates
        material = new PhongMaterial();
        if (GameMngr.playerTurn == 1) {
            material.setDiffuseColor(Color.RED);
        } else {
            material.setDiffuseColor(Color.BLUE);
        }
        sphere.setMaterial(material);
        sphere.setTranslateX(scene.getWidth() * 0.1);  // 10% from left
        if (GameMngr.isGameOver) {
            sphere.setTranslateX(scene.getWidth() * 0.325);  // 32.5% from left
        }
        sphere.setTranslateY(scene.getHeight() - 50); // Near bottom
        root.getChildren().add(sphere);
    }

    private static void clearUI(Group root) {
        root.getChildren().clear();
    }

    private static Layout setupLayout(Scene scene) {
        double size = 25;
        double originX = scene.getWidth() / 2;
        double originY = scene.getHeight() / 2;
        return new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));
    }

    private static HashMap<String, HexCube> buildHexMap() {
        HashMap<String, HexCube> hexMap = new HashMap<>();
        for (HexCube hex : GameMngr.hexs) {
            String key = hex.q + "," + hex.r + "," + hex.s;
            hexMap.put(key, hex);
        }
        return hexMap;
    }

    private static void renderHexGrid(Group root, Layout layout, HashMap<String, HexCube> hexMap) {
        int baseN = 6;

        for (int q = -baseN; q <= baseN; q++) {
            for (int r = -baseN; r <= baseN; r++) {
                int s = -q - r;
                if (Math.abs(s) > baseN) continue;

                String key = q + "," + r + "," + s;
                HexCube hex = hexMap.getOrDefault(key, new HexCube(q, r, s, Colour.WHITE, 0));

                if (!hexMap.containsKey(key)) {
                    GameMngr.hexs.add(hex);
                }

                Polygon hexagon = createAndFormatHexagon(hex, layout);
                addHoverOverEffect(hexagon, hex);
                processAndUpdateMoveOnBoard(hex, hexagon, root);
            }
        }
        GameMngr.playerTurnDecider(GameMngr.playerTurn, true);
    }

}
