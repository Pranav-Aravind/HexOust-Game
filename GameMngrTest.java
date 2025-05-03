import javafx.scene.text.Text;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GameMngrTest {

    @Before
    public void setup() {
        GameManager.hexs.clear();
        GameManager.isGameOver = false;
        GameManager.isStartOfGame = true;
        GameManager.move = 0;
        GameManager.playerTurn = 1;
        GameManager.noMoreValidMove = true;
    }

    @Test
    public void testValidateMoveFailsOnGameOver() {
        GameManager.isGameOver = true;
        HexCube hex = new HexCube(0, 0, 0, Colour.WHITE, 0);
        assertEquals(0, GameManager.validateMove(hex, false));
    }

    @Test
    public void testValidateMoveRejectsColoredHex() {
        HexCube hex = new HexCube(0, 0, 0, Colour.RED, 0);
        assertEquals(0, GameManager.validateMove(hex, true));
    }

    @Test
    public void testValidateMoveAcceptsUncoloredHex() {
        HexCube hex = new HexCube(0, 0, 0, Colour.WHITE, 0);
        GameManager.hexs.add(hex);
        assertEquals(1, GameManager.validateMove(hex, true));
        assertEquals(1, GameManager.move);
    }

    @Test
    public void testGroupLogic() {
        HexCube a = new HexCube(0, 0, 0, Colour.RED, 0);
        HexCube b = a.neighbor(0);
        b.colour = Colour.RED;

        ArrayList<HexCube> hexList = new ArrayList<>();
        hexList.add(a);
        hexList.add(b);

        ArrayList<HexCube> group = GameManager.getGroup(a, hexList);
        assertEquals(2, group.size());
    }

    @Test
    public void testRemoveStone() {
        HexCube stone = new HexCube(0, 0, 0, Colour.BLUE, 0);
        GameManager.removeStone(stone);
        assertEquals(Colour.WHITE, stone.colour);
    }

    @Test
    public void testCaptureLogic() {
        GameManager.hexs.clear();
        GameManager.isGameOver = false;
        GameManager.playerTurn = Colour.RED;


        HexCube blue = new HexCube(0, 0, 0, Colour.BLUE, 0);

        GameManager.hexs.add(blue);
        GameManager.hexs.add(new HexCube(1, -1, 0, Colour.RED, 0));
        GameManager.hexs.add(new HexCube(1, 0, -1, Colour.RED, 0));


        HexCube redMove = new HexCube(0, -1, 1, Colour.WHITE, 0); // Uncolored
        GameManager.hexs.add(redMove);

        GameManager.removeStonesIfAny(redMove, true); // Simulate move

        assertEquals(Colour.WHITE, blue.colour); // Blue should now be captured
    }

    @Test
    public void testRedWinsLogic() {
        GameManager.hexs.clear();
        GameManager.isStartOfGame = false;
        GameManager.isGameOver = false;
        GameManager.playerTurn = Colour.RED;

        HexCube red = new HexCube(0, 0, 0, Colour.RED, 0);
        GameManager.hexs.add(red);

        String result = GameManager.checkAndDeclareWinner();
        assertEquals("RED", result);
        assertTrue(GameManager.isGameOver);
    }


    @Test
    public void testBlueWinsLogic() {
        GameManager.hexs.clear();
        GameManager.isGameOver = false;
        GameManager.isStartOfGame = false;
        GameManager.playerTurn = Colour.BLUE;

        HexCube blue = new HexCube(1, -1, 0, Colour.BLUE, 0);
        GameManager.hexs.add(blue);

        String result = GameManager.checkAndDeclareWinner();
        assertEquals("BLUE", result);
        assertTrue(GameManager.isGameOver);
    }

    @Test
    public void testGameStatus_GameOverRed() {
        GameManager.isGameOver = true;
        GameManager.playerTurn = 1;
        Text result = GameManager.gameStatus();
        assertEquals("Game Over! Red Wins!", result.getText());
    }

    @Test
    public void testGameStatus_GameOverBlue() {
        GameManager.isGameOver = true;
        GameManager.playerTurn = 2;
        Text result = GameManager.gameStatus();
        assertEquals("Game Over! Blue Wins!", result.getText());
    }

    @Test
    public void testGameStatus_InProgress() {
        GameManager.isGameOver = false;
        GameManager.playerTurn = 1; // or 2, doesn't matter in this case
        Text result = GameManager.gameStatus();
        assertEquals("To make a Move", result.getText());
    }


}
