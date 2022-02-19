package MetaData;

import java.awt.*;

public class BoxSeries {
    public String name;

    public int startFrame;
    public int endFrame;

    public Point startTopLeft;
    public Point startBottomRight;
    public Point endTopLeft;
    public Point endBottomRight;


    public BoxSeries(String name, int startFrame, int endFrame, Point startTopLeft, Point startBottomRight, Point endTopLeft, Point endBottomRight) {
        this.name = name;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.startTopLeft = startTopLeft;
        this.startBottomRight = startBottomRight;
        this.endTopLeft = endTopLeft;
        this.endBottomRight = endBottomRight;
    }

    public BoxSeries(String name) {
        this.name = name;
    }

    public BoxSeries() {

    }

    public String toString() {
        return this.name;
    }

    public String getContent() {
        String startROI = startTopLeft.x + "," + startTopLeft.y + "," + startBottomRight.x + "," + startBottomRight.y;
        String endROI = endTopLeft.x + "," + endTopLeft.y + "," + endBottomRight.x + "," + endBottomRight.y;
        StringBuilder log = new StringBuilder();
        log.append("name:");
        log.append(name);
        log.append(";");
        log.append("startFrame:");
        log.append(startFrame);
        log.append(";");
        log.append("endFrame:");
        log.append(endFrame);
        log.append(";");
        log.append("startROI:");
        log.append(startROI);
        log.append(";");
        log.append("endROI:");
        log.append(endROI);
        log.append(";");
        return log.toString();
    }
}




