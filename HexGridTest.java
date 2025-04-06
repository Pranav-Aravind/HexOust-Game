import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class HexGridTest {

    static {
        try {
            new JFXPanel(); // Starts JavaFX platform once for all tests
        } catch (Exception e) {
            System.err.println("JavaFX already initialized or failed.");
        }
    }

    @Test
    void testValidateMoveFunctionClickingSameStoneTwice() throws Exception {
        // Arrange
        HexGrid game = new HexGrid();
        HexCube hex = new HexCube(0, 0, 0, 0, 0);  // Start uncolored
        Text invalidMoveText = new Text("Invalid Move!");

        // Simulate internal hexs list
        var field = game.getClass().getDeclaredField("hexs");
        field.setAccessible(true);
        ArrayList<HexCube> hexList = new ArrayList<>();
        hexList.add(hex);
        field.set(game, hexList);

        // Act
        int firstClick = game.validateMove(hex, invalidMoveText, true);
        hex.colour = 1;
        int secondClick = game.validateMove(hex, invalidMoveText, true);

        // Assert
        assertEquals(1, firstClick, "First click should be valid");
        assertEquals(0, secondClick, "Second click should be invalid (already colored)");
    }
    @Test
    void testShowInvalidMove() throws Exception {
        // Arrange
        HexGrid game = new HexGrid();
        HexCube hex = new HexCube(0, 0, 0, 0, 0);
        Text invalidMoveText = new Text("Invalid Move!");

        // Inject hex into the game board
        var field = game.getClass().getDeclaredField("hexs");
        field.setAccessible(true);
        ArrayList<HexCube> hexList = new ArrayList<>();
        hexList.add(hex);
        field.set(game, hexList);

        // Act
        game.validateMove(hex, invalidMoveText, true);
        hex.colour=1;
        game.validateMove(hex, invalidMoveText, true);//clicking on the same cell; invalid

        // Assert
        assertTrue(invalidMoveText.isVisible(), "\"Invalid Move!\" should be printed");
    }


    @Test
    void testSetPlayerTurnNCP() throws Exception {
        HexGrid game = new HexGrid();
        HexCube hex = new HexCube(0, 0, 0, 0, 0);  // Start uncolored
        Text invalidMoveText = new Text("Invalid Move!");

        // Simulate internal hexs list
        var field = game.getClass().getDeclaredField("hexs");
        field.setAccessible(true);
        var turn = game.getClass().getDeclaredField("playerTurn");
        turn.setAccessible(true);
        var function= game.getClass().getDeclaredMethod("setPlayerTurn",int.class);
        function.setAccessible(true);
        ArrayList<HexCube> hexList = new ArrayList<>();
        hexList.add(hex);
        field.set(game, hexList);

        // Act
        int firstClick = game.validateMove(hex, invalidMoveText, true);
        hex.colour = 1;
        game.setPlayerTurn(2);
        assertEquals(2, (int)turn.get(game));


    }

    @Test
    void testSphereColorChangesWithPlayerTurnAndInstructionTextExists() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                HexGrid game = new HexGrid();
                Group root = new Group();
                Scene scene = new Scene(root, 700, 700);

                game.setPlayerTurn(1); // Force playerTurn to 1

                // Use reflection to call private updateUI(Group, Scene)
                var updateUIMethod = HexGrid.class.getDeclaredMethod("updateUI", Group.class, Scene.class);
                updateUIMethod.setAccessible(true);
                updateUIMethod.invoke(game, root, scene);

                // Find the sphere and assert its color
                Sphere sphere = (Sphere) root.getChildren().stream()
                        .filter(node -> node instanceof Sphere)
                        .findFirst()
                        .orElse(null);

                assertNotNull(sphere, "Sphere should exist");
                PhongMaterial material = (PhongMaterial) sphere.getMaterial();
                assertEquals(Color.RED, material.getDiffuseColor(), "Sphere should be red when playerTurn == 1");

                // Check that "To make a move" text is present
                boolean instructionFound = root.getChildren().stream()
                        .filter(node -> node instanceof Text)
                        .map(node -> ((Text) node).getText())
                        .anyMatch(text -> text.equals("To make a move"));

                assertTrue(instructionFound, "Instruction text 'To make a move' should exist");

            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out waiting for JavaFX thread");
    }
}
