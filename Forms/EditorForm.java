
package Forms;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import MetaData.HyperLink;
import MetaData.MetaFile;
import MetaData.BoxSeries;
import VideoPlayer.VideoPlayer;


//Form which handles the UI and Functionality of the Authoring Tool
public class EditorForm extends JFrame{
    // UI
    private JPanel panelMain;
    private JPanel mainPanelTop;
    private JPanel panelTopLeftButtons;
    private JPanel panelTopRightButtons;
    private JPanel panelVideo1;
    private JPanel panelVideo2;
    private JPanel panelLinks;
    private JPanel panelBottomLeft;
    private JPanel panelBottomRight;


    private JSplitPane mainPanelBottom;
    private JSplitPane splitPanelTopRight;
    private JSplitPane splitPanelTop;

    private JLabel lblNum1;
    private JLabel lblNum2;
    private JLabel Title;
    public JLabel leftVideoLabel;
    private JLabel rightVideoLabel;
    public JLabel drawBox;


    private JSlider slider1;
    private JSlider slider2;

    private JButton btnImportPrimaryVideo;
    private JButton btnImportSecondaryVideo;
    private JButton btnCreateNewHyperLink;
    private JButton buttonConnectLink;
    private JButton btnSaveFile;
    private JButton btnDeleteLink;
    private JButton btnPlayPause1;
    private JButton btnPlayPause2;

    private JList linksList;

    // other declarations
    static int imageWidth = 352;
    static int imageHeight = 288;

    public boolean isPrimaryVideoImported = false;
    public boolean isSecondaryVideoImported = false;


    public static VideoPlayer vp0;
    public static VideoPlayer vp1;
    public ImageIcon leftIcon;
    public ImageIcon rightIcon;


    public String primaryVideoPath;
    public String secondaryVideoPath;

    public MetaFile metaFile;

    public int boxX1, boxY1, boxX2, boxY2;

    public int connectLinkCount = 0;

    public BoxSeries boxSeries = new BoxSeries();

    public DefaultListModel<BoxUnit> boxes;

    public int OnlyOne = 0; // 0= no box, 1 = have draw box, 2= have boxes

    // mouse interactions
    class EditorMouseListener extends MouseAdapter {

        // corresponding methods
        public void setStartPoint(int x, int y) {
            boxX1 = x;
            boxY1 = y;
        }

        public void setEndPoint(int x, int y) {
            // ensure x1,y1 is the top left, x2,y2 is the bottom right
            if (boxX1 <= x) {
                boxX2 = x;
            } else {
                boxX2 = boxX1;
                boxX1 = x;
            }
            if (boxY1 <= y) {
                boxY2 = y;
            } else {
                boxY2 = boxY1;
                boxY1 = y;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            setStartPoint(e.getX(), e.getY());
            if (OnlyOne == 0 || OnlyOne == 1) {
                setIcon(vp0.getBufferedImage(), leftVideoLabel);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
//            todo: implement dragging
//            setEndPoint(e.getX(), e.getY());
//            drawAdditionalBox(true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            setEndPoint(e.getX(), e.getY());
            drawAdditionalBox(false);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (metaFile.getBoxes(vp0.getCurrentFrame()).size() != 0)
                OnlyOne = 2;
            int x = e.getX();
            int y = e.getY();
            System.out.println(OnlyOne);
            if (OnlyOne == 2) {
                int result = JOptionPane.showConfirmDialog(null, "Are you sure to delete the whole series of this box?", "Attention", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    if (boxes == null || boxes.size() == 0)
                        return;
                    // determine which box is clicked
                    for (int i = 0; i < boxes.size(); i++) {
                        Point topLeft = boxes.get(i).topLeft;
                        Point bottomRight = boxes.get(i).bottomRight;
                        BoxUnit box = boxes.get(i);
                        if (x > topLeft.x && y > topLeft.y && x < bottomRight.x && y < bottomRight.y && !vp0.isPlaying()) {
                            System.out.println("this is clickMouse function!");
                            metaFile.removeBoxSeriesByID(box.id);
                            boxes = metaFile.getBoxes(vp0.getCurrentFrame());
                            if (boxes.size() == 0)
                                OnlyOne = 0;
                            setIcon(vp0.getBufferedImage(), leftVideoLabel);
                            break;
                        }
                    }
                } else if (result == JOptionPane.NO_OPTION) {

                } else {  //close the window
                    return;
                }
            }
        }
    }

    private void mouseListenerSetup() {
        EditorMouseListener editorMouseListener = new EditorMouseListener();
        panelVideo1.addMouseListener(editorMouseListener);
        panelVideo1.addMouseMotionListener(editorMouseListener);
    }

    public EditorForm() {

        setSize(720, 610);

        mainPanelTop.setSize(mainPanelTop.getWidth(), 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImportPrimaryVideo();
        ImportSecondaryVideo();
        CreateLink();
        DeleteLink();
        ConnectLink();
        SaveFile();
        setupLabels();
        SelectLink();
        Exit();


        add(panelMain);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isPrimaryVideoImported) vp0.destroy();
                if (isSecondaryVideoImported) vp1.destroy();
            }
        });
    }



    private void slider1Setup() {
        slider1.setMaximum(8999);
        slider1.setMinimum(0);
        slider1.setValue(0);
        slider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (slider1.isEnabled()) {
                    vp0.gotoFrame(slider1.getValue());
                }
                lblNum1.setText("Frame Number: " + slider1.getValue());

            }
        });
    }

    private void slider2Setup() {
        slider2.setMaximum(8999);
        slider2.setMinimum(0);
        slider2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (slider2.isEnabled()) vp1.gotoFrame(slider2.getValue());
                lblNum2.setText("Frame Number: " + slider2.getValue());
            }
        });
        slider2.setValue(vp1.getCurrentFrame());

    }

    private void setupLabels() {
        leftIcon = new ImageIcon(new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB));
        rightIcon = new ImageIcon(new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB));
        leftVideoLabel.setSize(panelVideo1.getWidth(), panelVideo1.getHeight());
        rightVideoLabel.setSize(panelVideo2.getWidth(), panelVideo2.getHeight());
        leftVideoLabel.setIcon(leftIcon);
        rightVideoLabel.setIcon(rightIcon);
    }

    public void setIcon(BufferedImage bufferedImage1, JLabel videoLabel) {
        BufferedImage bufferedImage = copyImage(bufferedImage1);
        if (videoLabel == leftVideoLabel) {
            boxes = metaFile.getBoxes(vp0.getCurrentFrame());
            if (boxes.size() == 0)
                OnlyOne = 0;
            else
                OnlyOne = 2;
            drawBoxes(bufferedImage, boxes);
        } // only draw boxes on the left side
        videoLabel.setIcon(new ImageIcon(bufferedImage));
        videoLabel.repaint();

        return;
    }

    class VideoPlayerActionListener implements ActionListener {
        VideoPlayer vp;
        JLabel videoLabel;
        JSlider slider;

        public VideoPlayerActionListener(VideoPlayer vp, JLabel videoLabel, JSlider slider) {
            this.vp = vp;
            this.videoLabel = videoLabel;
            this.slider = slider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // james 刷新页面
            setIcon(vp.getBufferedImage(), videoLabel);
            slider.setValue(vp.getCurrentFrame());
        }
    }

    private void ImportPrimaryVideo() {


        btnImportPrimaryVideo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                // if already imported, then destroy current VideoPlayer instance
//                if (isPrimaryVideoImported) {
//                    vp0.destroy();
//                    btnPlayPause1.setEnabled(false);
//                    slider1.setEnabled(false);
//                    isPrimaryVideoImported = false;
//                }

                // get path and initialize everything
                if (isPrimaryVideoImported) {
                    int result = JOptionPane.showConfirmDialog(null, "Do you want to save changes to file?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        if (metaFile != null) {
                            metaFile.writeToBoxSeries();
                            metaFile.writeToHyperLink();
                            JOptionPane.showMessageDialog(panelMain, "Files Successfully Saved");
                        } else {
                            JOptionPane.showMessageDialog(panelMain, "Invalid import and connection");
                        }
                    } else if (result == JOptionPane.NO_OPTION) {

                    } else {
                        return;
                    }
                }


                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showSaveDialog(panelBottomLeft) == 0) {
                    if (isPrimaryVideoImported) {
                        vp0.destroy();
                    }
                    connectLinkCount = 0;
                    linksList.removeAll();
                    primaryVideoPath = fileChooser.getSelectedFile().getAbsolutePath();
                    slider1.setEnabled(true);
                    metaFile = new MetaFile(primaryVideoPath);
                    linksList.setModel(metaFile.hyperLinks);
                    System.out.println(metaFile.hyperLinks);
                    metaFile.readHyperLink(primaryVideoPath + "/HyperLink.txt");
                    metaFile.readBoxSeries(primaryVideoPath + "/BoxSeries.txt");
                    vp0 = new VideoPlayer(primaryVideoPath, 0);
                    ActionListener VAL0 = new VideoPlayerActionListener(vp0, leftVideoLabel, slider1);
                    vp0.setAL(VAL0);
                    setBtnPlayPause(btnPlayPause1, vp0, slider1);
                    setIcon(vp0.getBufferedImage(), leftVideoLabel);
                    btnPlayPause1.setEnabled(true);


                    mouseListenerSetup();
                    slider1Setup();
                    btnImportSecondaryVideo.setEnabled(true);

                    isPrimaryVideoImported = true;

                }

            }
        });
    }

    private void loadSecondaryVideo(String vPath, int frameNo) {
        if (isSecondaryVideoImported) {
            vp1.destroy();
            btnPlayPause2.setEnabled(false);
            slider2.setEnabled(false);
            isSecondaryVideoImported = false;
        }
        secondaryVideoPath = vPath;
        slider2.setEnabled(true);
        vp1 = new VideoPlayer(vPath, frameNo);
        ActionListener VAL1 = new VideoPlayerActionListener(vp1, rightVideoLabel, slider2);
        vp1.setAL(VAL1);
        setBtnPlayPause(btnPlayPause2, vp1, slider2);
        setIcon(vp1.getBufferedImage(), rightVideoLabel);
        btnPlayPause2.setEnabled(true);
        metaFile.secondaryVideoPath = vPath;
        slider2Setup();
        btnCreateNewHyperLink.setEnabled(true);
        btnSaveFile.setEnabled(true);
        isSecondaryVideoImported = true;
    }

    private void ImportSecondaryVideo() {
        btnImportSecondaryVideo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // get path and initialize everything
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showSaveDialog(panelBottomRight) == 0) {
                    secondaryVideoPath = fileChooser.getSelectedFile().getAbsolutePath();
                    loadSecondaryVideo(secondaryVideoPath, 0);
                }
            }
        });

    }

    private void CreateLink() { // create hyperlink
        btnCreateNewHyperLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((isPrimaryVideoImported && isSecondaryVideoImported) && (primaryVideoPath != null) && (secondaryVideoPath != null)) {
                    String newLinkName = JOptionPane.showInputDialog(panelMain, "Enter the new HyperLink name");
                    if (newLinkName != null) {
                        if (newLinkName.equals("")) {
                            JOptionPane.showMessageDialog(panelMain, "Invalid name: name provided is null");
                        } else if (metaFile.findHyperLinkByName(newLinkName)) {
                            JOptionPane.showMessageDialog(panelMain, "Invalid name: " + newLinkName + " already exists");
                        } else {
                            metaFile.addHyperLink(newLinkName, secondaryVideoPath, slider2.getValue());
                        }
                    }
                    panelLinks.setSize(350, 300);
                } else {
                    JOptionPane.showMessageDialog(panelMain, "You should import both the videos first");
                }
            }
        });
    }

    private void SelectLink() {
        linksList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (linksList.getSelectedIndex() >= 0) {
                        if (vp1 != null) {
                            vp1.destroy();
                        }
                        HyperLink hl1 = metaFile.getHyperLinkByIndex(linksList.getSelectedIndex());
                        loadSecondaryVideo(hl1.targetVideoPath, hl1.targetFrame);
                    }
                    buttonConnectLink.setEnabled(true);
                    btnDeleteLink.setEnabled(true);
                }
            }
        });
    }

    private void DeleteLink() {
        btnDeleteLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedListIndex = linksList.getSelectedIndex();
                if (selectedListIndex >= 0) {
                    //metaFile.removeBoxesByName(metaFile.getHyperLinkByIndex(selectedListIndex).linkName);
                    metaFile.removeHyperLink(selectedListIndex);
                    System.out.println("we are delete the link!");
                    setIcon(vp0.getBufferedImage(), leftVideoLabel);
                } else {
                    JOptionPane.showMessageDialog(panelMain, "Invalid Selection");
                }
                linksList.clearSelection();
                //
            }
        });
    }

    private void SaveFile(){
        btnSaveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (metaFile != null) {
                    metaFile.writeToBoxSeries();
                    metaFile.writeToHyperLink();
                    setIcon(vp0.getBufferedImage(), leftVideoLabel);
                    JOptionPane.showMessageDialog(panelMain, "Files Successfully Saved");
                } else {
                    JOptionPane.showMessageDialog(panelMain, "Invalid import and connection");
                }
            }
        });
    }


    class PlayPauseBtnActionListener implements ActionListener {
        JButton button;
        VideoPlayer vp;
        JSlider slider;

        public PlayPauseBtnActionListener(JButton button, VideoPlayer vp, JSlider slider) {
            this.button = button;
            this.vp = vp;
            this.slider = slider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            button.setText(button.getText().substring(0, 4) + "ing...");// todo: (Tony) not working
            button.setEnabled(false);
            if (vp.isPlaying()) {
                vp.pause();
                button.setText("Play");
                slider.setEnabled(true);
            } else {
                vp.play();
                button.setText("Pause");
                slider.setEnabled(false);
            }
            button.setEnabled(true);
        }
    }

    private void setBtnPlayPause(JButton button, VideoPlayer vp, JSlider slider) {
        // remove current action listener, if there is one.
        ActionListener[] currentListeners = button.getActionListeners();
        if (currentListeners.length != 0) button.removeActionListener(currentListeners[0]);

        button.setEnabled(false);
        button.setText("Play");
        button.addActionListener(new PlayPauseBtnActionListener(button, vp, slider));
    }

    private void ConnectLink() {
        buttonConnectLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (OnlyOne == 0 || OnlyOne == 2) {
                    JOptionPane.showMessageDialog(panelMain, "Drag a box before connecting");
                    return;
                }
                if (linksList.getSelectedIndex() >= 0) {
                    String linkName = metaFile.getHyperLinkByIndex(linksList.getSelectedIndex()).linkName;
                    if (connectLinkCount == 0) {
                        metaFile.connectLink(linkName, slider1.getValue(), boxX1, boxX2, boxY1, boxY2, 0);
                        connectLinkCount++;
                    } else {
                        Object[] options = {"end frame", "new boxSeries"};
                        int result = JOptionPane.showOptionDialog(null, "Is it the end frame or the start frame of a new boxSeries?", "Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        //result: 0 end 1 new, -1 close the window
                        if (result == 0) {
                            if (slider1.getValue() > metaFile.getStartFrameNum()) {
                                metaFile.connectLink(linkName, slider1.getValue(), boxX1, boxX2, boxY1, boxY2, 1);
                                connectLinkCount = 0;
                            } else {
                                JOptionPane.showMessageDialog(null, "The end frame must be selected after start frame: " + metaFile.getStartFrameNum() + ". Please redo it.", "Warning", 1);
                            }
                        } else if (result == 1) {
                            metaFile.connectLink(linkName, slider1.getValue(), boxX1, boxX2, boxY1, boxY2, 0);
                            connectLinkCount++;
                        } else {
                            setIcon(vp0.getBufferedImage(), leftVideoLabel);
                            return;
                        }
                    }
                    setIcon(vp0.getBufferedImage(), leftVideoLabel);
                } else {
                    JOptionPane.showMessageDialog(panelMain, "Select a HyperLink before connecting");
                }
            }
        });
    }



    public void drawAdditionalBox(boolean preview) {

        if (OnlyOne == 0 || OnlyOne == 2) {
            OnlyOne = 1;
            Graphics g = panelVideo1.getGraphics();
            Graphics2D g2d = (Graphics2D) g;
            Stroke st = g2d.getStroke();
            Stroke bs = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{16, 4}, 0);
            g2d.setStroke(bs);
            g2d.setColor(Color.red);
            g2d.drawRect(boxX1, boxY1, boxX2 - boxX1, boxY2 - boxY1);
            g2d.setStroke(st);

        }
    }


    public static void drawBoxes(BufferedImage label, DefaultListModel<BoxUnit> boxes) {
        if (boxes == null || boxes.size() == 0) {
            return;
        }
        int size = boxes.size();
        for (int i = 0; i < size; i++) {
            BoxUnit box = boxes.get(i);
            Graphics g = label.getGraphics();
            g.setColor(Color.red);
            g.drawRect(box.topLeft.x, box.topLeft.y, box.bottomRight.x - box.topLeft.x, box.bottomRight.y - box.topLeft.y);
        }
    }


    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }


    public void Exit() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isPrimaryVideoImported == false) {
                    return;
                }

                int result = JOptionPane.showConfirmDialog(null, "Do you want to save changes to file?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    if (metaFile != null) {
                        metaFile.writeToBoxSeries();
                        metaFile.writeToHyperLink();
                        JOptionPane.showMessageDialog(panelMain, "Files Successfully Saved");
                    } else {
                        JOptionPane.showMessageDialog(panelMain, "Invalid import and connection");
                    }
                } else if (result == JOptionPane.NO_OPTION) {

                } else {
                    return;
                }
            }
        });
    }


}