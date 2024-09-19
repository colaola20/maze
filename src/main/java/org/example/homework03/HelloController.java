package org.example.homework03;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;

public class HelloController {

    @FXML
    private Text inFrontTxt;
    @FXML
    private Text onLeftTxt;
    @FXML
    private Text direction;

    @FXML
    private ToggleButton startAuto;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ImageView maze;

    static final int UP=0, RIGHT=1, DOWN=2, LEFT=3;
    @FXML
    private ImageView robot;
    private final static int robotSpeed = 8;
    int robotFowardDirection=RIGHT;

    private void moveRobot() {
        int x=0;
        int y=0;
        switch (robotFowardDirection) {
            case UP: y=-1;break;
            case DOWN: y=1;break;
            case LEFT: x=-1;break;
            case RIGHT: x=1;break;
        }
        robot.setRotate( (y==0)?((x>0)?90:-90):(y>0)?180:0 ); //set rotation based off movement.

        //test move robot to new position.
        double newXPos = (robotSpeed*x) + robot.getLayoutX();
        double newYPos = (robotSpeed*y) + robot.getLayoutY();

        // find the center of the robot image
        double roboCenterX = (robot.getImage().getWidth()/2.0);
        double roboCenterY = (robot.getImage().getHeight()/2.0);

        //keep movement within the bounds of the image.
        if (newXPos>maze.getImage().getWidth()) newXPos = maze.getImage().getWidth();
        else if (newXPos<0) newXPos = 0;
        if (newYPos>maze.getImage().getHeight()) newYPos = maze.getImage().getHeight();
        else if (newYPos<0) newYPos = 0;

        //offset scan to center and outward into the movement direction
        double scanPosX = newXPos + roboCenterX + x*roboCenterX;
        double scanPosY = newYPos + roboCenterY + y*roboCenterY;

        //Set Debug Info..
        String txt1 = isWallInFront()+""; inFrontTxt.setText("isWallInFront: " + txt1); inFrontTxt.getStyleClass().clear(); inFrontTxt.getStyleClass().add(txt1);
        String txt2 = isWallOnLeft()+""; onLeftTxt.setText("isWallOnLeft: " + txt2); onLeftTxt.getStyleClass().clear(); onLeftTxt.getStyleClass().add(txt2);
        int d = robotFowardDirection; direction.setText("direction: " + (d==UP?"UP":(d==DOWN?"DOWN":(d==LEFT?"LEFT":"RIGHT"))) );

        //search the image at the scan location for a color. keep scan within bounds of image.
        if (isColorValid(getColorAtPosition(scanPosX, scanPosY))) {
            robot.setLayoutX(newXPos);
            robot.setLayoutY(newYPos);
        }
    }

    @FXML
    public void onKeyPressed(KeyEvent e) {//Event passed from main stage key event
        switch (e.getCode()) {
            case UP: robotFowardDirection=UP; moveRobot(); break;
            case DOWN: robotFowardDirection=DOWN; moveRobot(); break;
            case LEFT: robotFowardDirection=LEFT; moveRobot(); break;
            case RIGHT: robotFowardDirection=RIGHT; moveRobot(); break;
        }
    }

    //Image file uploader
    FileChooser fileChooser = new FileChooser();
    @FXML
    public void onImageSelectClicked() throws MalformedURLException {
        File file = fileChooser.showOpenDialog(maze.getScene().getWindow());
        if (file != null) maze.setImage(new Image(file.toURI().toURL().toExternalForm()));
    }

    //Auto Solver
    @FXML
    Timeline timeline;
    public void onStartClicked() {

        if (!startAuto.isSelected()) {
            timeline.stop();
            startAuto.setText("Start Auto Solve");
            return;
        }
        startAuto.setText("Stop Auto Solve");

        timeline = new Timeline(new KeyFrame(Duration.millis(20), event -> {
            if (robot.getLayoutX() < maze.getImage().getWidth() * .94) {// navigate while robo position is less than %94 of the maze's width
                if (isWallOnLeft()) {
                    if (isWallInFront()) {
                        moveRobot();// move a bit before rotating
                        robotFowardDirection=(robotFowardDirection+1)%4;// Rotate CW
                    }
                    moveRobot();//left wall is present. keep moving!
                } else {
                    moveRobot();// move a bit before rotating
                    robotFowardDirection=(robotFowardDirection+3)%4;// Rotate CCW
                    moveRobot();moveRobot();moveRobot();//extra moves in case left wall is not immediately there.
                }
            }
            else {// It workss
                System.out.println("DOONNNNEE!!!!!");
                startAuto.setSelected(false);
                startAuto.setText("Start Auto Solve");
                timeline.stop();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    //region Collision Test Methods
    private boolean isWallOnLeft() {
        //set scan pos to the center of the robot.
        double scanPosX = robot.getLayoutX() + (robot.getImage().getWidth()/2.0);;
        double scanPosY = robot.getLayoutY() + (robot.getImage().getHeight()/2.0);;
        //Move scan toward the left of the robot.
        switch ( robotFowardDirection ) {
            case UP: scanPosX-=robot.getImage().getWidth();break;
            case DOWN: scanPosX+=robot.getImage().getWidth();break;
            case LEFT: scanPosY+=robot.getImage().getHeight();break;
            case RIGHT: scanPosY-=robot.getImage().getHeight();break;
        }
        // Read the color at the scan position
        return !isColorValid(getColorAtPosition(scanPosX,scanPosY));
    }

    private boolean isWallInFront() {
        //set scan pos to the center of the robot.
        double scanPosX = robot.getLayoutX() + (robot.getImage().getWidth()/2.0);;
        double scanPosY = robot.getLayoutY() + (robot.getImage().getHeight()/2.0);;
        //Move scan toward the current facing direction.
        switch (robotFowardDirection) {
            case UP: scanPosY-=robot.getImage().getHeight();break;
            case DOWN: scanPosY+=robot.getImage().getHeight();break;
            case LEFT: scanPosX-=robot.getImage().getWidth();break;
            case RIGHT: scanPosX+=robot.getImage().getWidth();break;
        }
        // Read the color at the scan position
        return !isColorValid(getColorAtPosition(scanPosX,scanPosY));
    }
    //endregion

    //region Color Test Methods
    public boolean isColorValid(Color colorAtPos) {
        return (colorAtPos.getRed()>0.9 && colorAtPos.getGreen()>0.9 && colorAtPos.getBlue()>0.9 );
    }
    private Color getColorAtPosition(double posX, double posY) {
        return (posX > 0 && posY > 0 && posX < maze.getImage().getWidth() && posY < maze.getImage().getHeight()) ? maze.getImage().getPixelReader().getColor((int) (posX), (int) (posY)) : Color.BLACK;
    }
    //endregion

    //ToDo clean up path code.
//    private Path findPath() {
//        // Setting up the path
//        Path path = new Path();
//        robot.setLayoutX(0);
//        robot.setLayoutY(0);
//        path.getElements().add(new MoveTo(0.0f, (maze.getImage().getHeight() / 2.0)));
//
//        while (robot.getLayoutX() < maze.getImage().getWidth() - 8) {
//            // Find the center of the robot image
//
//
//            // Offset scan to center and outward into the movement direction
////            double scanPosX = robot.getLayoutX() + roboCenterX + robotSpeed * roboCenterX;
////            double scanPosY = robot.getLayoutY() + roboCenterY + 0 * roboCenterY;
////
////            // Search the image at the scan location for a color. Keep scan within bounds of image.
////            Color colorAtPos = (scanPosX < maze.getImage().getWidth() && scanPosY < maze.getImage().getHeight()) ? maze.getImage().getPixelReader().getColor((int) (scanPosX), (int) (scanPosY)) : Color.BLACK;
//
//            // If the color is white or close to white, then proceed to move
////            if (colorAtPos.getRed() > 0.9 && colorAtPos.getGreen() > 0.9 && colorAtPos.getBlue() > 0.9) {
////                double newX = robot.getLayoutX() + robotSpeed;
////                if (newX != robot.getLayoutX()) {
////                    path.getElements().add(new LineTo(newX, robot.getLayoutY()));
////                    robot.setLayoutX(newX);
////                }
////            } else {
////                double newY = robot.getLayoutY() + robotSpeed;
////                if (newY != robot.getLayoutY()) {
////                    path.getElements().add(new LineTo(robot.getLayoutX(), newY));
////                    robot.setLayoutY(newY);
////                }
////            }
//        }
//
//        return path;
//    }

//        //Instantiating PathTransition class
//        javafx.animation.PathTransition pathTransition = new javafx.animation.PathTransition();
//
//        //Setting duration for the PathTransition
//        pathTransition.setDuration(Duration.millis(1000));
//
//        //Setting Node on which the path transition will be applied
//        pathTransition.setNode(robot);
//
//        //setting path for the path transition
//        pathTransition.setPath(findPath());
//
//        //setting orientation for the path transition
//        //pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
//
//        //setting up the cycle count
//        pathTransition.setCycleCount(1);
//
//        //setting auto reverse to be true
//        pathTransition.setAutoReverse(true);
//
//        //Playing path transition
//        pathTransition.play();
}
