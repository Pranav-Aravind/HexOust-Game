import java.util.ArrayList;

class Colour {
    public static final int WHITE = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;
}

class Point
{
    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    public final double x;
    public final double y;
}

/**
 * Represents a hexagonal tile using cube coordinates (q, r, s).
 * Used for grid positioning and movement logic in the game.
 */
public class HexCube
{
    public HexCube(int q, int r, int s, int colour, int status)
    {
        this.q = q;
        this.r = r;
        this.s = s;
        this.colour = colour;
        this.visited = status;
        if (q + r + s != 0)
            throw new IllegalArgumentException("q + r + s must be 0");
    }

    public final int q;
    public final int r;
    public final int s;
    public int colour;
    public int visited;

    public HexCube add(HexCube b)
    {
        return new HexCube(q + b.q, r + b.r, s + b.s,b.colour, b.visited);
    }

    public HexCube subtract(HexCube b)
    {
        return new HexCube(q - b.q, r - b.r, s - b.s,b.colour, b.visited);
    }

    static public ArrayList<HexCube> directions = new ArrayList<HexCube>(){{add(new HexCube(1, 0, -1,Colour.WHITE, 0)); add(new HexCube(1, -1, 0,Colour.WHITE, 0)); add(new HexCube(0, -1, 1,Colour.WHITE, 0)); add(new HexCube(-1, 0, 1,Colour.WHITE, 0)); add(new HexCube(-1, 1, 0,Colour.WHITE, 0)); add(new HexCube(0, 1, -1,Colour.WHITE, 0));}};

    static public HexCube direction(int direction)
    {
        return HexCube.directions.get(direction);
    }

    public HexCube neighbor(int direction)
    {
        return add(HexCube.direction(direction));
    }

    public int length()
    {
        return (int)((Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2);
    }

    public int distance(HexCube b)
    {
        return subtract(b).length();
    }

    //Returns all neighboring hexes from the provided list that are adjacent to this hex.
    public ArrayList<HexCube> getNeighbours(ArrayList<HexCube> hexList) {
        ArrayList<HexCube> neighbours = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            HexCube neighbor = this.neighbor(i);
            for (HexCube hex : hexList) {
                if (hex.q == neighbor.q && hex.r == neighbor.r && hex.s == neighbor.s) {
                    neighbours.add(hex);
                    break;
                }
            }
        }
        return neighbours;
    }

}

class FractionalHexCube
{
    public FractionalHexCube(double q, double r, double s)
    {
        this.q = q;
        this.r = r;
        this.s = s;
        if (Math.round(q + r + s) != 0)
            throw new IllegalArgumentException("q + r + s must be 0");
    }

    public final double q;
    public final double r;
    public final double s;

    public HexCube hexRound()
    {
        int qi = (int)(Math.round(q));
        int ri = (int)(Math.round(r));
        int si = (int)(Math.round(s));
        double q_diff = Math.abs(qi - q);
        double r_diff = Math.abs(ri - r);
        double s_diff = Math.abs(si - s);
        if (q_diff > r_diff && q_diff > s_diff)
        {
            qi = -ri - si;
        }
        else
        if (r_diff > s_diff)
        {
            ri = -qi - si;
        }
        else
        {
            si = -qi - ri;
        }
        return new HexCube(qi, ri, si,Colour.WHITE, 0);
    }
}

class Orientation
{
    public Orientation(double f0, double f1, double f2, double f3,
                       double b0, double b1, double b2, double b3,
                       double start_angle)
    {
        this.f0 = f0;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.start_angle = start_angle;
    }
    public final double f0;
    public final double f1;
    public final double f2;
    public final double f3;
    public final double b0;
    public final double b1;
    public final double b2;
    public final double b3;
    public final double start_angle;
}

class Layout
{
    public Layout(Orientation orientation, Point size, Point origin)
    {
        this.orientation = orientation;
        this.size = size;
        this.origin = origin;
    }

    public final Orientation orientation;
    public final Point size;
    public final Point origin;

    static public Orientation flat = new Orientation(3.0 / 2.0, 0.0, Math.sqrt(3.0) / 2.0, Math.sqrt(3.0), 2.0 / 3.0,
            0.0, -1.0 / 3.0, Math.sqrt(3.0) / 3.0, 0.0);

    public Point hexToPixel(HexCube h)
    {
        Orientation M = orientation;
        double x = (M.f0 * h.q + M.f1 * h.r) * size.x;
        double y = (M.f2 * h.q + M.f3 * h.r) * size.y;
        return new Point(x + origin.x, y + origin.y);
    }

    public Point hexCornerOffset(int corner)
    {
        Orientation M = orientation;
        double angle = 2.0 * Math.PI * (M.start_angle - corner) / 6.0;
        return new Point(size.x * Math.cos(angle), size.y * Math.sin(angle));
    }


    public ArrayList<Point> polygonCorners(HexCube h)
    {
        ArrayList<Point> corners = new ArrayList<Point>(){{}};
        Point center = hexToPixel(h);
        for (int i = 0; i < 6; i++)
        {
            Point offset = hexCornerOffset(i);
            corners.add(new Point(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }
}