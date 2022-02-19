package Forms;

import MetaData.BoxSeries;

import java.awt.*;

public class BoxUnit {
    int id;
    Point topLeft;
    Point bottomRight;
    String linkName;
    String targetVideoPath;
    int targetVideoFrame;

    public BoxUnit(int id, Point topLeft, Point bottomRight) {
        this.id = id;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public void setHyperLinkInfo(String linkName, String targetVideoPath, int targetVideoFrame) {
        this.linkName = linkName;
        this.targetVideoFrame = targetVideoFrame;
        this.targetVideoPath = targetVideoPath;
    }

}