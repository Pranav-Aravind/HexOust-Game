import javafx.scene.text.Text;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GameMngrTest {

    @Before
    public void setup() {
        GameMngr.hexs.clear();
        GameMngr.isGameOver = false;
        GameMngr.isStartOfGame = true;
        GameMngr.move = 0;
        GameMngr.playerTurn = 1;
        GameMngr.noMoreValidMove = true;
    }

    @Test
    public void testValidateMoveFailsOnGameOver() {
        GameMngr.isGameOver = true;
        HexCube hex = new HexCube(0, 0, 0, Colour.WHITE, 0);
        assertEquals(0, GameMngr.validateMove(hex, false));
    }

    @Test
    public void testValidateMoveRejectsColoredHex() {
        HexCube hex = new HexCube(0, 0, 0, Colour.RED, 0);
        assertEquals(0, GameMngr.validateMove(hex, true));
    }

    @Test
    public void testValidateMoveAcceptsUncoloredHex() {
        HexCube hex = new HexCube(0, 0, 0, Colour.WHITE, 0);
        GameMngr.hexs.add(hex);
        assertEquals(1, GameMngr.validateMove(hex, true));
        assertEquals(1, GameMngr.move);
    }

    @Test
    public void testGroupLogic() {
        HexCube a = new HexCube(0, 0, 0, Colour.RED, 0);
        HexCube b = a.neighbor(0);
        b.colour = Colour.RED;

        ArrayList<HexCube> hexList = new ArrayList<>();
        hexList.add(a);
        hexList.add(b);

        ArrayList<HexCube> group = GameMngr.getGroup(a, hexList);
        assertEquals(2, group.size());
    }

    @Test
    public void testRemoveStone() {
        HexCube stone = new HexCube(0, 0, 0, Colour.BLUE, 0);
        GameMngr.removeStone(stone);
        assertEquals(Colour.WHITE, stone.colour);
    }

    @Test
    public void testCaptureLogic() {
        GameMngr.hexs.clear();
        GameMngr.isGameOver = false;
        GameMngr.playerTurn = Colour.RED;


        HexCube blue = new HexCube(0, 0, 0, Colour.BLUE, 0);

        GameMngr.hexs.add(blue);
        GameMngr.hexs.add(new HexCube(1, -1, 0, Colour.RED, 0));
        GameMngr.hexs.add(new HexCube(1, 0, -1, Colour.RED, 0));


        HexCube redMove = new HexCube(0, -1, 1, Colour.WHITE, 0); // Uncolored
        GameMngr.hexs.add(redMove);

        GameMngr.removeStonesIfAny(redMove, true); // Simulate move

        assertEquals(Colour.WHITE, blue.colour); // Blue should now be captured
    }

    @Test
    public void testRedWinsLogic() {
        GameMngr.hexs.clear();
        GameMngr.isStartOfGame = false;
        GameMngr.isGameOver = false;
        GameMngr.playerTurn = Colour.RED;

        HexCube red = new HexCube(0, 0, 0, Colour.RED, 0);
        GameMngr.hexs.add(red);

        String result = GameMngr.checkAndDeclareWinner();
        assertEquals("RED", result);
        assertTrue(GameMngr.isGameOver);
    }


    @Test
    public void testBlueWinsLogic() {
        GameMngr.hexs.clear();
        GameMngr.isGameOver = false;
        GameMngr.isStartOfGame = false;
        GameMngr.playerTurn = Colour.BLUE;

        HexCube blue = new HexCube(1, -1, 0, Colour.BLUE, 0);
        GameMngr.hexs.add(blue);

        String result = GameMngr.checkAndDeclareWinner();
        assertEquals("BLUE", result);
        assertTrue(GameMngr.isGameOver);
    }

    @Test
    public void testGameStatus_GameOverRed() {
        GameMngr.isGameOver = true;
        GameMngr.playerTurn = 1;
        Text result = GameMngr.gameStatus();
        assertEquals("Game Over! Red Wins!", result.getText());
    }

    @Test
    public void testGameStatus_GameOverBlue() {
        GameMngr.isGameOver = true;
        GameMngr.playerTurn = 2;
        Text result = GameMngr.gameStatus();
        assertEquals("Game Over! Blue Wins!", result.getText());
    }

    @Test
    public void testGameStatus_InProgress() {
        GameMngr.isGameOver = false;
        GameMngr.playerTurn = 1; // or 2, doesn't matter in this case
        Text result = GameMngr.gameStatus();
        assertEquals("To make a Move", result.getText());
    }


}
