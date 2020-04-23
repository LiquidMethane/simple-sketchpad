package sample;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

class Coord {

    protected double x;
    protected double y;

    public Coord(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

public class Controller implements Initializable {

    @FXML private Canvas canvas;
    @FXML private VBox vbox;
    @FXML private MenuBar menu;
    @FXML private Pane pane;
    @FXML private ColorPicker colorPicker;

    private Bounds paneBounds;

    private Stack<Coord> positionStack;
    private List<Group> groupList;
    private Stack<Group> undoStack;

    private Group selectedGroup;
    private Group cuttedGroup;

    private Coord originCoord;

    private boolean polygonInProgress = false;

    EventHandler<MouseEvent> handleFreehand = mouseEvent -> {
        //store current mouse position
        positionStack.push(new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY()));

        //create a new group to put tiny lines that makes freehand line
        Group group = new Group();

        //attach group to the pane
        pane.getChildren().add(group);

        //push group reference to a stack
        groupList.add(group);
        undoStack.clear();

    };

    EventHandler<MouseEvent> handleFreehandDrag = mouseEvent -> {

        //restrict drawing to inside the pane
        if (mouseEvent.getSceneX() < paneBounds.getMinX() || mouseEvent.getSceneX() > paneBounds.getMaxX() || mouseEvent.getSceneY() < paneBounds.getMinY() || mouseEvent.getSceneY() > paneBounds.getMaxY())
            return;

        //get prev mouse position from stack
        Coord coord = positionStack.pop();
        positionStack.push(new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY()));

        //draw a tiny line
        Line line = new Line(coord.x, coord.y, mouseEvent.getSceneX(), mouseEvent.getSceneY());

        //set line color and add to group
        line.setStroke(colorPicker.getValue());
        groupList.get(groupList.size() - 1).getChildren().add(line);

    };

    EventHandler<MouseEvent> handleLine = mouseEvent -> {
        originCoord = new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        //create a new group to put tiny lines that makes freehand line
        Group group = new Group();

        //attach group to the pane
        pane.getChildren().add(group);

        //push group reference to a stack
        groupList.add(group);
        undoStack.clear();

    };

    EventHandler<MouseEvent> handleLineDrag = mouseEvent -> {

        //restrict drawing to inside the pane
        if (mouseEvent.getSceneX() < paneBounds.getMinX() || mouseEvent.getSceneX() > paneBounds.getMaxX() || mouseEvent.getSceneY() < paneBounds.getMinY() || mouseEvent.getSceneY() > paneBounds.getMaxY())
            return;

         groupList.get(groupList.size() - 1).getChildren().clear();


        //draw a straight line
        Line line = new Line(originCoord.x, originCoord.y, mouseEvent.getSceneX(), mouseEvent.getSceneY());

        //set line color and add to group
        line.setStroke(colorPicker.getValue());
        groupList.get(groupList.size() - 1).getChildren().add(line);

    };

    EventHandler<MouseEvent> handleRect = mouseEvent -> {
        originCoord = new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        //create a new group to put tiny lines that makes freehand line
        Group group = new Group();

        //attach group to the pane
        pane.getChildren().add(group);

        //push group reference to a stack
        groupList.add(group);
        undoStack.clear();

    };

    EventHandler<MouseEvent> handleRectDrag = mouseEvent -> {

        //restrict drawing to inside the pane
        if (mouseEvent.getSceneX() < paneBounds.getMinX() || mouseEvent.getSceneX() > paneBounds.getMaxX() || mouseEvent.getSceneY() < paneBounds.getMinY() || mouseEvent.getSceneY() > paneBounds.getMaxY())
            return;

        groupList.get(groupList.size() - 1).getChildren().clear();

        //calculate width and height of the rectangle
        double width = mouseEvent.getSceneX() - originCoord.x;
        double height = mouseEvent.getSceneY() - originCoord.y;

        double shorterSide = Math.min(height, width);

        //draw a rectangle or square
        Rectangle rect = !mouseEvent.isShiftDown() ? new Rectangle(originCoord.x, originCoord.y, width, height)
                : new Rectangle(originCoord.x, originCoord.y, shorterSide, shorterSide);

        //set line color and add to group
        rect.setStroke(colorPicker.getValue());
        rect.setFill(Color.TRANSPARENT);
        groupList.get(groupList.size() - 1).getChildren().add(rect);

    };

    EventHandler<MouseEvent> handleEllipse = mouseEvent -> {
        originCoord = new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        //create a new group to put tiny lines that makes freehand line
        Group group = new Group();

        //attach group to the pane
        pane.getChildren().add(group);

        //push group reference to a stack
        groupList.add(group);
        undoStack.clear();

    };

    EventHandler<MouseEvent> handleEllipseDrag = mouseEvent -> {

        //restrict drawing to inside the pane
        if (mouseEvent.getSceneX() < paneBounds.getMinX() || mouseEvent.getSceneX() > paneBounds.getMaxX() || mouseEvent.getSceneY() < paneBounds.getMinY() || mouseEvent.getSceneY() > paneBounds.getMaxY())
            return;

        groupList.get(groupList.size() - 1).getChildren().clear();

        //calculate the x-radius and y-radius of ellipse
        double width = mouseEvent.getSceneX() - originCoord.x;
        double height = mouseEvent.getSceneY() - originCoord.y;

        double shorterSide = Math.min(height, width);

        //draw a ellipse or circle
        Ellipse ellipse = !mouseEvent.isShiftDown() ? new Ellipse(originCoord.x, originCoord.y, width, height)
                : new Ellipse(originCoord.x, originCoord.y, shorterSide, shorterSide);

        //set line color and add to group
        ellipse.setStroke(colorPicker.getValue());
        ellipse.setFill(Color.TRANSPARENT);
        groupList.get(groupList.size() - 1).getChildren().add(ellipse);

    };

    EventHandler<MouseEvent> handlePolygon = mouseEvent -> {

        System.out.println(polygonInProgress);
        System.out.println(mouseEvent.getClickCount());


        if (polygonInProgress) {
            //restrict drawing to inside the pane
            if (mouseEvent.getSceneX() < paneBounds.getMinX() || mouseEvent.getSceneX() > paneBounds.getMaxX() || mouseEvent.getSceneY() < paneBounds.getMinY() || mouseEvent.getSceneY() > paneBounds.getMaxY())
                return;

            //draw a straight line
            Line line = new Line(originCoord.x, originCoord.y, mouseEvent.getSceneX(), mouseEvent.getSceneY());

            //set line color and add to group
            line.setStroke(colorPicker.getValue());
            groupList.get(groupList.size() - 1).getChildren().add(line);



        } else {
            polygonInProgress = true;

            originCoord = new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY());

            //create a new group to put tiny lines that makes freehand line
            Group group = new Group();

            //attach group to the pane
            pane.getChildren().add(group);

            //push group reference to a stack
            groupList.add(group);
            undoStack.clear();
        }

        if (mouseEvent.getClickCount() > 1) {
            polygonInProgress = false;
            originCoord = new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        }
    };

    EventHandler<MouseEvent> handlePolygonDrag = mouseEvent -> {

        //restrict drawing to inside the pane
        if (mouseEvent.getSceneX() < paneBounds.getMinX() || mouseEvent.getSceneX() > paneBounds.getMaxX() || mouseEvent.getSceneY() < paneBounds.getMinY() || mouseEvent.getSceneY() > paneBounds.getMaxY())
            return;

        Group group = groupList.get(groupList.size() - 1);

        Iterable<Node> nodes = group.getChildren();
        List<Node> nodesToRemove = new LinkedList<>();

        for (Node n : nodes) {
            if (n instanceof Line) {
                Line l = (Line) n;
                if (l.getStartX() == originCoord.x && l.getStartY() == originCoord.y) {
                    nodesToRemove.add(n);
                }
            }
        }

        for (Node n : nodesToRemove) {
            group.getChildren().remove(n);
        }


        //draw a straight line
        Line line = new Line(originCoord.x, originCoord.y, mouseEvent.getSceneX(), mouseEvent.getSceneY());

        //set line color and add to group
        line.setStroke(colorPicker.getValue());
        groupList.get(groupList.size() - 1).getChildren().add(line);


    };

    EventHandler<MouseEvent> handlePolygonRelease = mouseEvent -> {
        originCoord = new Coord(mouseEvent.getSceneX(), mouseEvent.getSceneY());
    };



    @FXML private void setSelectMode() {

        canvas.setOnMouseDragged(null);
        canvas.setOnMousePressed(null);
        canvas.setOnMouseReleased(null);

        //traverse groupList
        for (Group g : groupList) {

            //record position on mouse press
            g.setOnMousePressed(e -> {
                positionStack.push(new Coord(e.getSceneX(), e.getSceneY()));
                selectedGroup = g;
            });

            //move every child in group
            g.setOnMouseDragged(e -> {
                Coord prevMousePos = positionStack.pop();
                positionStack.push(new Coord(e.getSceneX(), e.getSceneY()));

                double deltaX = e.getSceneX() - prevMousePos.x;
                double deltaY = e.getSceneY() - prevMousePos.y;

                for (Node n: g.getChildren()) {
                    if (n instanceof Line) {
                        Line line = (Line) n;
                        line.setStartX(line.getStartX() + deltaX);
                        line.setStartY(line.getStartY() + deltaY);
                        line.setEndX(line.getEndX() + deltaX);
                        line.setEndY(line.getEndY() + deltaY);
                    }

                    else if (n instanceof Rectangle) {
                        Rectangle rect = (Rectangle) n;
                        rect.setX(rect.getX() + deltaX);
                        rect.setY(rect.getY() + deltaY);

                    }

                    else if (n instanceof Ellipse) {
                        Ellipse ell = (Ellipse) n;
                        ell.setCenterX(ell.getCenterX() + deltaX);
                        ell.setCenterY(ell.getCenterY() + deltaY);
                    }

                }

                menu.toFront();
                vbox.toFront();
            });
        }
    }

    private void resetSelectMode() {
        for (Group g : groupList) {
            g.setOnMousePressed(null);
            g.setOnMouseDragged(null);
        }
    }

    @FXML private void setFreehandMode() {
        resetSelectMode();
        System.out.println("Freehand mode");
        canvas.setOnMousePressed(handleFreehand);
        canvas.setOnMouseDragged(handleFreehandDrag);
    }

    @FXML private void setLineMode() {
        System.out.println("Line mode");
        resetSelectMode();
        canvas.setOnMousePressed(handleLine);
        canvas.setOnMouseDragged(handleLineDrag);
    }

    @FXML private void setRectangleMode() {
        System.out.println("Rectangle mode");
        resetSelectMode();
        canvas.setOnMousePressed(handleRect);
        canvas.setOnMouseDragged(handleRectDrag);
    }

    @FXML private void setEllipseMode() {
        System.out.println("Ellipse mode");
        resetSelectMode();
        canvas.setOnMousePressed(handleEllipse);
        canvas.setOnMouseDragged(handleEllipseDrag);
    }

    @FXML private void setPolygonMode() {
        System.out.println("Polygon mode");
        resetSelectMode();
        canvas.setOnMousePressed(handlePolygon);
        canvas.setOnMouseDragged(handlePolygonDrag);
        canvas.setOnMouseReleased(handlePolygonRelease);
    }

    @FXML private void undo() {
        if (!groupList.isEmpty()) {
            undoStack.push(groupList.remove(groupList.size() - 1));
            pane.getChildren().remove(undoStack.peek());
        }

    }

    @FXML private void redo() {
        if (!undoStack.isEmpty()) {
            pane.getChildren().add(undoStack.peek());
            groupList.add(undoStack.pop());
        }
    }

    @FXML private void copy() {
        cuttedGroup = selectedGroup;
    }

    @FXML private void cut() {
        cuttedGroup = selectedGroup;
        groupList.remove(cuttedGroup);
        pane.getChildren().remove(cuttedGroup);
    }

    @FXML private void paste() {
        Group g = new Group();

        for (Node n : cuttedGroup.getChildren()) {
            if (n instanceof Line) {
                Line line = (Line) n;
                Line newLine = new Line(line.getStartX() + 50, line.getStartY() + 50, line.getEndX() + 50, line.getEndY() + 50);
                newLine.setStroke(line.getStroke());
                g.getChildren().add(newLine);
            }

            else if (n instanceof Rectangle) {
                Rectangle rect = (Rectangle) n;
                Rectangle newRect = new Rectangle(rect.getX() + 50, rect.getY() + 50, rect.getWidth(), rect.getHeight());
                newRect.setFill(Color.TRANSPARENT);
                newRect.setStroke(rect.getStroke());
                g.getChildren().add(newRect);
            }

            else if (n instanceof Ellipse) {
                Ellipse ell = (Ellipse) n;
                Ellipse newEll = new Ellipse(ell.getCenterX() + 50, ell.getCenterY() + 50, ell.getRadiusX(), ell.getRadiusY());
                newEll.setFill(Color.TRANSPARENT);
                newEll.setStroke(ell.getStroke());
                g.getChildren().add(newEll);

            }
        }

        pane.getChildren().add(g);
        groupList.add(g);
        setSelectMode();
    }

    @FXML private void save() {
        System.out.println("saving to file");

        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(pane.getScene().getWindow());

        if (file != null) {
            WritableImage wi = pane.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }


    }


    @FXML private void load() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(pane.getScene().getWindow());
        if (file != null) {
            BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString()), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
            //then you set to your node
            pane.setBackground(new Background(myBI));
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //initialize bounds
        paneBounds = pane.getBoundsInParent();

        //set backgrounds
        vbox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        //initialise data structures
        positionStack = new Stack<>();
        groupList = new LinkedList<>();
        undoStack = new Stack<>();

        //initialize colour picker
        colorPicker.setValue(Color.BLACK);

    }
}
