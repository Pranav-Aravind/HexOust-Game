import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class HexGrid extends Application {
    //created an ArrayList of hexagons to store the state of each Hexagons
    private ArrayList<HexCube> hexs = new ArrayList<>();
    int playerTurn = 1;
    private Sphere sphere; // Store sphere as instance variable
    private PhongMaterial material; // Store material for updates

    public void setPlayerTurn(int player) {
        playerTurn = player;
    }

    @Override
    public void start(Stage stage) {
        Label label_size = new Label();
        Group root = new Group();
        Scene scene = new Scene(root, 700, 700, javafx.scene.paint.Color.WHITE);
        stage.setTitle("HexOust");
        stage.setScene(scene);
        stage.show();

        // Call method to draw hex grid dynamically, including UI elements
        updateUI(root, scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> updateUI(root, scene));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> updateUI(root, scene));


        double size = 25;
        double originX = scene.getWidth()/2;
        double originY = scene.getHeight()/2;

        Layout layout = new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));
    }

    private javafx.scene.shape.Polygon createHexagon(ArrayList<Point> corners) {
        javafx.scene.shape.Polygon hex = new javafx.scene.shape.Polygon();
        for (Point p : corners) {
            hex.getPoints().addAll(p.x, p.y);
        }
        return hex;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showInvalidMove(Text invalidMoveText) {
        invalidMoveText.setVisible(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            invalidMoveText.setVisible(false);
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }


    public int validateMove(HexCube hex) {
        int isSameColour = 0;
        if(hex.colour != 0) {
            return 0;
        }
        for(HexCube neighbour : hex.getNeighbours(hexs)) {
            if(neighbour.colour != playerTurn && neighbour.colour != 0) {
                return 1;
            }
            if(neighbour.colour == playerTurn) {
                isSameColour = 1;
            }
        }
        if(isSameColour == 1) {
            return 0;
        }
       return 1;
    }

    int finalGrpSize = 1;

    public int checkGroup(HexCube hex, ArrayList<HexCube> hexList) {
        hex.visited = 1;
        for (HexCube neighbour : hex.getNeighbours(hexs)) {
            if (neighbour.colour == hex.colour && neighbour.visited != 1) {
                finalGrpSize++;
                neighbour.visited = 1;
                checkGroup(neighbour, hexList);
            }
        }
        for(HexCube hexagon : hexs) {
            hexagon.visited = 0;
        }

        return finalGrpSize;
    }

    private void updateUI(Group root, Scene scene) {
        double size = 25;
        double originX = scene.getWidth() / 2;
        double originY = scene.getHeight() / 2;

        DropShadow validGlow = new DropShadow();
        validGlow.setColor(Color.LIME);
        validGlow.setRadius(10);

        DropShadow invalidGlow = new DropShadow();
        invalidGlow.setColor(Color.BROWN);
        invalidGlow.setRadius(10);

        final javafx.scene.shape.Polygon[] selectedHexagon = {null};

        root.getChildren().clear(); // Clear UI components before re-adding

        // --- HEX GRID ---
        Layout layout = new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));
        int baseN = 6;

        // --- INVALID MOVE TEXT ---
        Text invalidMoveText = new Text("Invalid Move!");
        invalidMoveText.setFill(Color.PURPLE);
        invalidMoveText.setFont(Font.font("Arial", 20));
        invalidMoveText.setTranslateX(scene.getWidth() / 2 - 60);  // Centering dynamically
        invalidMoveText.setTranslateY(scene.getHeight() - 45);
        invalidMoveText.setVisible(false); // Initially hidden

        root.getChildren().add(invalidMoveText);

        // Create a map to quickly check existing hexagons by their (q, r, s) coordinates
        HashMap<String, HexCube> hexMap = new HashMap<>();
        for (HexCube hex : hexs) {
            hexMap.put(hex.q + "," + hex.r + "," + hex.s, hex);
        }

        // Iterate through the hexagon grid
        for (int q = -baseN; q <= baseN; q++) {
            for (int r = -baseN; r <= baseN; r++) {
                int s = -q - r;
                if (Math.abs(s) > baseN) continue;

                String key = q + "," + r + "," + s;
                HexCube hex = hexMap.getOrDefault(key, new HexCube(q, r, s, 0, 0));

                if (!hexMap.containsKey(key)) {
                    hexs.add(hex); // Add to the list only if it's new
                }

                ArrayList<Point> corners = layout.polygonCorners(hex);
                Polygon hexagon = createHexagon(corners);

                // Maintain color state
                if (hex.colour == 1) {
                    hexagon.setFill(javafx.scene.paint.Color.RED);
                } else if (hex.colour == 2) {
                    hexagon.setFill(javafx.scene.paint.Color.BLUE);
                } else {
                    hexagon.setFill(javafx.scene.paint.Color.WHITE);
                }

                hexagon.setStroke(javafx.scene.paint.Color.BLACK);


                hexagon.setOnMouseEntered(event -> {
                    if (hex.colour == 0) {
                        if (validateMove(hex) == 1) {
                            hexagon.setFill(Color.color(0, 1, 0, 0.3)); // LIME with 50% opacity
                            hexagon.setEffect(validGlow);
                        } else {
                            hexagon.setFill(Color.color(0.6, 0.3, 0.1, 0.3)); // BROWN with 50% opacity
                            hexagon.setEffect(invalidGlow);
                        }
                    } else {
                        hexagon.setEffect(invalidGlow);
                    }
                });

                hexagon.setOnMouseExited(event -> {
                    hexagon.setEffect(null);
                    if (hex.colour == 0) { // Only reset color if it's uncolored
                        hexagon.setFill(Color.WHITE);
                    }
                });

                hexagon.setOnMouseClicked(event -> {
                    if (validateMove(hex) == 1) {
                        if (playerTurn == 1) {
                            hex.colour = 1;
                            setPlayerTurn(2);
                            hexagon.setFill(javafx.scene.paint.Color.RED);
                            material.setDiffuseColor(Color.BLUE);
                            sphere.setMaterial(material);
                        } else {
                            hex.colour = 2;
                            setPlayerTurn(1);
                            hexagon.setFill(javafx.scene.paint.Color.BLUE);
                            material.setDiffuseColor(Color.RED);
                            sphere.setMaterial(material);
                        }
                        updateUI((Group) sphere.getParent(), sphere.getScene());
                    }
                });
                root.getChildren().add(hexagon);
            }
        }

        // --- BALL (SPHERE) ---
        sphere = new Sphere(13);
        material = new PhongMaterial();
        if (playerTurn==1){
            material.setDiffuseColor(Color.RED);
        }
        else{
            material.setDiffuseColor(Color.BLUE);
        }

        sphere.setMaterial(material);
        sphere.setTranslateX(scene.getWidth() * 0.1);  // 10% from left
        sphere.setTranslateY(scene.getHeight() - 50); // Near bottom
        root.getChildren().add(sphere);

        // --- TITLE ---
        Text title = new Text("HexOust");
        title.setFill(javafx.scene.paint.Color.BLACK);
        title.setFont(javafx.scene.text.Font.font("Consolas", 35));
        title.setTranslateX(scene.getWidth() / 2 - 60); // Centering dynamically
        title.setTranslateY(50);
        root.getChildren().add(title);

        // --- INSTRUCTIONS TEXT ---
        Text text = new Text("To make a move");
        text.setFill(javafx.scene.paint.Color.BLACK);
        text.setFont(Font.font("Arial", 20));
        text.setTranslateX(scene.getWidth() / 6 - 30); // Adjust dynamically
        text.setTranslateY(scene.getHeight() - 40);
        root.getChildren().add(text);

        // --- EXIT BUTTON ---
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

}