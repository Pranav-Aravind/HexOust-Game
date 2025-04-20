import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class HexCubeTest {

    @Test
    public void testAddSubtract() {
        HexCube a = new HexCube(1, -1, 0, Colour.RED, 0);
        HexCube b = new HexCube(0, 1, -1, Colour.BLUE, 0);
        HexCube sum = a.add(b);
        HexCube diff = a.subtract(b);

        assertEquals(1, sum.q);
        assertEquals(0, sum.r);
        assertEquals(-1, sum.s);

        assertEquals(1, diff.q);
        assertEquals(-2, diff.r);
        assertEquals(1, diff.s);
    }

    @Test
    public void testDirectionValues() {
        HexCube dir0 = HexCube.direction(0);
        assertEquals(1, dir0.q);
        assertEquals(0, dir0.r);
        assertEquals(-1, dir0.s);

        HexCube dir3 = HexCube.direction(3);
        assertEquals(-1, dir3.q);
        assertEquals(0, dir3.r);
        assertEquals(1, dir3.s);
    }

    @Test
    public void testNeighbor() {
        HexCube center = new HexCube(0, 0, 0, Colour.WHITE, 0);
        HexCube neighbor0 = center.neighbor(0);
        assertEquals(1, neighbor0.q);
        assertEquals(0, neighbor0.r);
        assertEquals(-1, neighbor0.s);

        HexCube neighbor5 = center.neighbor(5);
        assertEquals(0, neighbor5.q);
        assertEquals(1, neighbor5.r);
        assertEquals(-1, neighbor5.s);
    }

    @Test
    public void testLength() {
        HexCube a = new HexCube(2, -1, -1, Colour.BLUE, 0);
        assertEquals(2, a.length());
    }

    @Test
    public void testDistance() {
        HexCube a = new HexCube(0, 0, 0, Colour.WHITE, 0);
        HexCube b = new HexCube(2, -1, -1, Colour.BLUE, 0);
        assertEquals(2, a.distance(b));
    }

    @Test
    public void testGetNeighboursReturnsSix() {
        HexCube center = new HexCube(0, 0, 0, Colour.WHITE, 0);
        ArrayList<HexCube> board = new ArrayList<HexCube>();

        for (int i = 0; i < 6; i++) {
            HexCube neighbor = center.neighbor(i);
            board.add(neighbor);
        }
        board.add(center);

        ArrayList<HexCube> neighbors = center.getNeighbours(board);
        assertEquals(6, neighbors.size());
    }

    @Test
    public void testInvalidHexCubeThrows() {
        try {
            HexCube invalid = new HexCube(1, 1, 1, Colour.WHITE, 0);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("q + r + s must be 0"));
        }
    }

    @Test
    public void testPolygonCornersReturnsSixPoints() {
        Layout layout = new Layout(Layout.flat, new Point(10, 10), new Point(100, 100));
        HexCube hex = new HexCube(0, 0, 0, Colour.WHITE, 0);

        ArrayList<Point> corners = layout.polygonCorners(hex);
        assertEquals(6, corners.size(), "A hexagon must have 6 corners");
    }


    @Test
    public void testHexRoundGivesValidCube() {
        FractionalHexCube fHex = new FractionalHexCube(1.2, -0.6, -0.6);
        HexCube rounded = fHex.hexRound();

        assertEquals(rounded.q + rounded.r + rounded.s, 0, "Rounded HexCube must satisfy q + r + s = 0");
    }

    @Test
    public void testHexRoundNearOrigin() {
        FractionalHexCube fHex = new FractionalHexCube(0.49, -0.24, -0.25);
        HexCube rounded = fHex.hexRound();

        assertEquals(0, rounded.q);
        assertEquals(0, rounded.r);
        assertEquals(0, rounded.s);
    }

    @Test
    public void testHexRoundThrowsOnBadInput() {
        try {
            FractionalHexCube invalid = new FractionalHexCube(0.5, 0.5, 0.5);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("q + r + s must be 0"));
        }
    }
}
