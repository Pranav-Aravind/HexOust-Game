import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FrontEndTest {

    @BeforeClass
    public static void initJFX() {
        new JFXPanel();
    }

    @Test
    public void testCreateHexagon() {
        ArrayList<Point> corners = new ArrayList<Point>();
        corners.add(new Point(0, 0));
        corners.add(new Point(1, 0));
        corners.add(new Point(1, 1));
        corners.add(new Point(0, 1));

        Polygon poly = FrontEnd.createHexagon(corners);
        assertEquals(8, poly.getPoints().size());
    }

    @Test
    public void testCreateAndFormatHexagonColors() {
        Layout layout = new Layout(Layout.flat, new Point(25, 25), new Point(100, 100));

        HexCube redHex = new HexCube(0, 0, 0, Colour.RED, 0);
        Polygon redPoly = FrontEnd.createAndFormatHexagon(redHex, layout);
        assertEquals(Color.RED, redPoly.getFill());

        HexCube blueHex = new HexCube(0, 0, 0, Colour.BLUE, 0);
        Polygon bluePoly = FrontEnd.createAndFormatHexagon(blueHex, layout);
        assertEquals(Color.BLUE, bluePoly.getFill());

        HexCube whiteHex = new HexCube(0, 0, 0, Colour.WHITE, 0);
        Polygon whitePoly = FrontEnd.createAndFormatHexagon(whiteHex, layout);
        assertEquals(Color.WHITE, whitePoly.getFill());
    }

    @Test
    public void testInvalidMoveAddsText() {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 600);

        FrontEnd.invalidMove(scene, root);

        boolean found = false;
        List<javafx.scene.Node> children = root.getChildren();
        for (int i = 0; i < children.size(); i++) {
            javafx.scene.Node node = children.get(i);
            if (node instanceof Text) {
                Text t = (Text) node;
                if ("Invalid Move!".equals(t.getText())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Invalid Move! text should be added");
    }

    @Test
    public void testTitleAdded() {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);

        FrontEnd.Title(scene, root);

        boolean found = false;
        List<javafx.scene.Node> children = root.getChildren();
        for (int i = 0; i < children.size(); i++) {
            javafx.scene.Node node = children.get(i);
            if (node instanceof Text) {
                Text t = (Text) node;
                if ("HexOust".equals(t.getText())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Title 'HexOust' should be added to the scene");
    }

    @Test
    public void testExitButtonIsAdded() {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);

        FrontEnd.Exit(scene, root);

        boolean found = false;
        List<javafx.scene.Node> children = root.getChildren();
        for (int i = 0; i < children.size(); i++) {
            javafx.scene.Node node = children.get(i);
            if (node instanceof Button) {
                Button b = (Button) node;
                if ("Exit".equals(b.getText())) {
                    found = true;
                    break;
                }
            }
        }

        assertTrue(found, "Exit button should be added to the scene");
    }
}
