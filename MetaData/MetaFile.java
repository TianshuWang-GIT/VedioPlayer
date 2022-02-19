package MetaData;

import Forms.BoxUnit;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
// metaFile.addHyperLink(String linkName, String targetVideoPath, int frameNo)

public class MetaFile {
    public DefaultListModel<BoxSeries> boxSerieses;
    public DefaultListModel<HyperLink> hyperLinks;
    public String primaryVideoPath;
    public String secondaryVideoPath;
    public BoxSeries tempBoxSeries;

    public MetaFile(String primaryVideoPath) {
        //this.primaryVideoPath = primaryVideoPath;
        boxSerieses = new DefaultListModel<BoxSeries>();
        hyperLinks = new DefaultListModel<HyperLink>();
        this.primaryVideoPath = primaryVideoPath;

    }


    public DefaultListModel<BoxUnit> getBoxes(int frameNum) {
        DefaultListModel<BoxUnit> resultList = new DefaultListModel<BoxUnit>();
        for (int i = 0; i < boxSerieses.size(); i++) {
            BoxSeries tempBoxSeries = boxSerieses.get(i);
            if ((frameNum >= tempBoxSeries.startFrame) && (frameNum <= tempBoxSeries.endFrame)) {
                BoxUnit boxUnit;
                if (tempBoxSeries.startFrame == tempBoxSeries.endFrame) {
                    boxUnit = new BoxUnit(i, new Point(tempBoxSeries.startTopLeft.x, tempBoxSeries.startTopLeft.y), new Point(tempBoxSeries.startBottomRight.x, tempBoxSeries.startBottomRight.y));
                } else {
                    int tempTLx = (int) ((double) tempBoxSeries.startTopLeft.x + ((double) (tempBoxSeries.endTopLeft.x - tempBoxSeries.startTopLeft.x) / (double) (tempBoxSeries.endFrame - tempBoxSeries.startFrame)) * (double) (frameNum - tempBoxSeries.startFrame));
                    int tempTLy = (int) ((double) tempBoxSeries.startTopLeft.y + ((double) (tempBoxSeries.endTopLeft.y - tempBoxSeries.startTopLeft.y) / (double) (tempBoxSeries.endFrame - tempBoxSeries.startFrame)) * (double) (frameNum - tempBoxSeries.startFrame));
                    int tempBRx = (int) ((double) tempBoxSeries.startBottomRight.x + ((double) (tempBoxSeries.endBottomRight.x - tempBoxSeries.startBottomRight.x) / (double) (tempBoxSeries.endFrame - tempBoxSeries.startFrame)) * (double) (frameNum - tempBoxSeries.startFrame));
                    int tempBRy = (int) ((double) tempBoxSeries.startBottomRight.y + ((double) (tempBoxSeries.endBottomRight.y - tempBoxSeries.startBottomRight.y) / (double) (tempBoxSeries.endFrame - tempBoxSeries.startFrame)) * (double) (frameNum - tempBoxSeries.startFrame));
                    boxUnit = new BoxUnit(i, new Point(tempTLx, tempTLy), new Point(tempBRx, tempBRy));
                }
                String tempTargetPath = "";
                int tempTargetFrame = 0;
                for (int j = 0; j < hyperLinks.size(); j++) {
                    if (hyperLinks.get(j).linkName.equals(tempBoxSeries.name)) {
                        tempTargetPath = hyperLinks.get(j).targetVideoPath;
                        tempTargetFrame = hyperLinks.get(j).targetFrame;
                    }
                }
                boxUnit.setHyperLinkInfo(tempBoxSeries.name, tempTargetPath, tempTargetFrame);
                resultList.addElement(boxUnit);
            }
        }
        return resultList;
    }

    public void removeBoxSeriesByID(int id) {
        boxSerieses.remove(id);
    }

    public void writeToBoxSeries() {
        try (PrintWriter writer = new PrintWriter(primaryVideoPath + "/BoxSeries.txt")) {
            for (int i = 0; i < boxSerieses.size(); i++) {
                writer.println(boxSerieses.getElementAt(i).getContent());
            }
        } catch (IOException ex) {

        }
    }

    public void writeToHyperLink() {
        try (PrintWriter writer = new PrintWriter(primaryVideoPath + "/HyperLink.txt")) {
            for (int i = 0; i < hyperLinks.size(); i++) {
                HyperLink mt = hyperLinks.getElementAt(i);
                writer.println(mt.linkName + "," + mt.targetVideoPath + "," + mt.targetFrame);
            }
        } catch (IOException ex) {
        }

    }

    public void readHyperLink(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                String[] stringList = line.split(",");
                hyperLinks.addElement(new HyperLink(stringList[0], stringList[1], Integer.parseInt(stringList[2])));
                //matchTable.put(stringList[0], stringList[1]+","+stringList[2]);
                line = reader.readLine();
            }
        } catch (IOException e) {
        }
    }


    public void readBoxSeries(String metaFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(metaFilePath))) {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                boxSerieses.addElement(parseBoxSeries(line));
                line = reader.readLine();
            }
        } catch (IOException e) {

        }
    }

    public HyperLink getHyperLinkByIndex(int index) {
        return hyperLinks.get(index);
    }

    public void removeBoxesByName(String name) {
        for (int i = 0; i < boxSerieses.size(); i++) {
            if (boxSerieses.get(i).name.compareTo(name) == 0) {
                boxSerieses.remove(i);
                i--;
            }
        }
    }

    public boolean findHyperLinkByName(String name) {
        for (int i = 0; i < hyperLinks.size(); i++) {
            if (hyperLinks.get(i).linkName.equals(name)) return true;
        }
        return false;
    }

    public void addHyperLink(String linkName, String targetVideoPath, int frameNo) {
        System.out.println("HyperLink -->  linkName: " + linkName + ", targetPath: " + targetVideoPath + ", targetFrame: " + frameNo);
        hyperLinks.addElement(new HyperLink(linkName, secondaryVideoPath, frameNo));
    }

    public void removeHyperLink(int index) {
        removeBoxesByName(hyperLinks.get(index).linkName);
        hyperLinks.removeElementAt(index);
    }

    public void removeBoxesByNameStartFrame(String name, int startFrame) {
        for (int i = 0; i < boxSerieses.size(); i++) {
            if (boxSerieses.get(i).name.compareTo(name) == 0 && boxSerieses.get(i).startFrame == startFrame) {
                boxSerieses.remove(i);
                i--;
            }
        }
    }


    private BoxSeries parseBoxSeries(String line) {
        String[] stringList = line.split(";");
        BoxSeries boxSeries = new BoxSeries();
        boxSeries.name = stringList[0].replace("name:", "");
        boxSeries.startFrame = Integer.parseInt(stringList[1].replace("startFrame:", ""));
        boxSeries.endFrame = Integer.parseInt(stringList[2].replace("endFrame:", ""));
        String[] startROI = stringList[3].replace("startROI:", "").split(",");
        boxSeries.startTopLeft = new Point(Integer.parseInt(startROI[0]), Integer.parseInt(startROI[1]));
        boxSeries.startBottomRight = new Point(Integer.parseInt(startROI[2]), Integer.parseInt(startROI[3]));
        String[] endROI = stringList[4].replace("endROI:", "").split(",");
        boxSeries.endTopLeft = new Point(Integer.parseInt(endROI[0]), Integer.parseInt(endROI[1]));
        boxSeries.endBottomRight = new Point(Integer.parseInt(endROI[2]), Integer.parseInt(endROI[3]));
        return boxSeries;
    }

    public void connectLink(String name, int frameNo, int boxX1, int boxX2, int boxY1, int boxY2, int mode) {
        // mode: 0 = set start , 1 = set end
        BoxSeries boxSeries;
        if (mode == 0) {
            boxSeries = new BoxSeries();
            boxSeries.name = name;
            boxSeries.startFrame = frameNo;
            boxSeries.endFrame = frameNo;
            Point startTopLeft = new Point(boxX1, boxY1);
            Point startBottomRight = new Point(boxX2, boxY2);
            boxSeries.startTopLeft = startTopLeft;
            boxSeries.startBottomRight = startBottomRight;
        } else { // mode == 1
            boxSeries = tempBoxSeries;
            boxSeries.endFrame = frameNo;
        }
        Point endTopLeft = new Point(boxX1, boxY1);
        Point endBottomRight = new Point(boxX2, boxY2);
        boxSeries.endTopLeft = endTopLeft;
        boxSeries.endBottomRight = endBottomRight;
        if (mode == 0) {
            tempBoxSeries = boxSeries;
        } else if (mode == 1) {
            removeBoxesByNameStartFrame(boxSeries.name, boxSeries.startFrame);
        }
        boxSerieses.addElement(boxSeries);
        System.out.println("linkName: " + boxSeries.name + ", startFrame: " + boxSeries.startFrame + ", endFrame: " + boxSeries.endFrame + ", start point: " + boxSeries.startTopLeft + " " + boxSeries.startBottomRight + ", end point: " + boxSeries.endTopLeft + " " + boxSeries.endBottomRight);
    }

    public int getStartFrameNum() {
        return tempBoxSeries.startFrame;
    }

}