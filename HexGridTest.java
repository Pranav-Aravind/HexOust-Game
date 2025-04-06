import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

//    @Test
//    void testExitButtonPrintsClosingHexOust() {
//        // Arrange
//        Button exitButton = new Button("Exit");
//        HexGrid.exitFunction(exitButton);
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        PrintStream originalOut = System.out;
//        System.setOut(new PrintStream(outputStream));
//
//        // Act
//        exitButton.fire();
//
//        // Restore output
//        System.setOut(originalOut);
//        String output = outputStream.toString().trim();
//
//        // Assert
//        assertTrue(output.contains("Closing HexOust"), "Exit button should print 'Closing HexOust'");
//    }
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
    
}
