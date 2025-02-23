import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.shape.Polygon;
import javafx.scene.control.Button;

import java.awt.*;
import java.util.ArrayList;
import javafx.scene.effect.DropShadow;




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

class HexCube
{
    public HexCube(int q, int r, int s)
    {
        this.q = q;
        this.r = r;
        this.s = s;
        if (q + r + s != 0)
            throw new IllegalArgumentException("q + r + s must be 0");
    }

    public final int q;
    public final int r;
    public final int s;

    public HexCube add(HexCube b)
    {
        return new HexCube(q + b.q, r + b.r, s + b.s);
    }

    public HexCube subtract(HexCube b)
    {
        return new HexCube(q - b.q, r - b.r, s - b.s);
    }

    static public ArrayList<HexCube> directions = new ArrayList<HexCube>(){{add(new HexCube(1, 0, -1)); add(new HexCube(1, -1, 0)); add(new HexCube(0, -1, 1)); add(new HexCube(-1, 0, 1)); add(new HexCube(-1, 1, 0)); add(new HexCube(0, 1, -1));}};

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
        return new HexCube(qi, ri, si);
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

    public FractionalHexCube pixelToHex(Point p)
    {
        Orientation M = orientation;
        Point pt = new Point((p.x - origin.x) / size.x, (p.y - origin.y) / size.y);
        double q = M.b0 * pt.x + M.b1 * pt.y;
        double r = M.b2 * pt.x + M.b3 * pt.y;
        return new FractionalHexCube(q, r, -q - r);
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

public class HexGrid extends Application {
    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 700, 700, Color.WHITE);
        stage.setTitle("HexOust");
        stage.setScene(scene);
        stage.show();

        DropShadow glow = new DropShadow();
        glow.setColor(Color.BROWN);
        glow.setRadius(10);

        double size = 25;
        double originX = 350;
        double originY = 360;

        Layout layout = new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));

        int baseN = 6;


        final Polygon[] selectedHexagon = {null};

        for (int q = -baseN; q <= baseN; q++) {
            for (int r = -baseN; r <= baseN; r++) {
                for (int s = -baseN; s <= baseN; s++) {
                    if ((q + r + s) == 0) {
                        HexCube hex = new HexCube(q, r, s);
                        ArrayList<Point> corners = layout.polygonCorners(hex);
                        Polygon hexagon = createHexagon(corners);

                        hexagon.setFill(Color.WHITE);
                        hexagon.setStroke(Color.BLACK);

                        // Hover Effect
                        hexagon.setOnMouseEntered(event -> {
                            if (selectedHexagon[0] != hexagon){
                                hexagon.setFill(Color.YELLOW);
                                hexagon.setEffect(glow);
                            }

                        });
                        hexagon.setOnMouseExited(event -> {
                            hexagon.setEffect(null);
                            if (selectedHexagon[0] != hexagon) {
                                hexagon.setFill(Color.WHITE);
                            }
                        });

                        hexagon.setOnMouseClicked(event -> {
                            if (selectedHexagon[0] != null) {
                                selectedHexagon[0].setFill(Color.WHITE);
                            }
                            hexagon.setFill(Color.RED);
                            selectedHexagon[0] = hexagon;
                        });

                        root.getChildren().add(hexagon);
                    }
                }
            }
        }



        Sphere sphere = new Sphere(13);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.RED);
        sphere.setMaterial(material);
        sphere.setTranslateX(70);
        sphere.setTranslateY(650);
        root.getChildren().add(sphere);

        Text title = new Text("HexOust");
        title.setFill(Color.BLACK);
        title.setFont(Font.font("Stencil", 35));
        title.setTranslateX(275);
        title.setTranslateY(50);
        root.getChildren().add(title);



        Text text = new Text("To make a move");
        text.setFill(Color.BLACK);
        text.setFont(Font.font("Arial", 20));
        text.setTranslateX(90);
        text.setTranslateY(660);
        root.getChildren().add(text);

        Button exit = new Button("Exit");
        exit.setTranslateX(620);
        exit.setTranslateY(10);
        exit.setPrefSize(70, 30);
        root.getChildren().add(exit);

        exit.setOnAction(event -> {
            System.out.println("Closing HexOust");
            Platform.exit();
        });

    }

    private Polygon createHexagon(ArrayList<Point> corners) {
        Polygon hex = new Polygon();
        for (Point p : corners) {
            hex.getPoints().addAll(p.x, p.y);
        }
        return hex;
    }

    public static void main(String[] args) {
        launch(args);
    }
}