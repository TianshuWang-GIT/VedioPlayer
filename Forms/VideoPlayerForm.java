
package Forms;

import VideoPlayer.VideoPlayer;

import MetaData.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class VideoPlayerForm extends JFrame {
    public JLabel Title;
    public JButton btnPlayPause;
    public JButton ImportVideo;
    public JPanel panelVideoPlayer;
    public JPanel panelVideo;
    public JLabel labelVideoDisplay;
    public JSlider slider;
    public JLabel lblNum;
    public boolean isVideoImported = false;

    private JPanel panelBottomLeft;
    public String VideoPath;
    public VideoPlayer vp;

    public DefaultListModel<BoxUnit> boxes;

    public int currentIndex = 0;

    public MetaFile metaFile;

    class VideoPlayerMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int size = boxes.size();
            if (size == 0)
                return;
            for (int i = 0; i < boxes.size(); i++) {
                BoxUnit box = boxes.get(i);
                Point topLeft = box.topLeft;
                Point bottomRight = box.bottomRight;
                if (x > topLeft.x && y > topLeft.y && x < bottomRight.x && y < bottomRight.y) {
                    jumpTo(box.targetVideoPath, box.targetVideoFrame);
                    return;
                }
            }
        }
    }

    private void mouseListenerSetup() {
        VideoPlayerMouseListener videoPlayerMouseListener = new VideoPlayerMouseListener();
        panelVideo.addMouseListener(videoPlayerMouseListener);
    }

    public VideoPlayerForm() {
        add(panelVideoPlayer);
        setSize(720, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        labelVideoDisplay.setSize(panelVideo.getWidth(), panelVideo.getHeight());
        ImportVideo();
        mouseListenerSetup();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isVideoImported) vp.destroy();
            }
        });
    }





    private void loadVideo(String vPath, int frameNo) {
        metaFile = new MetaFile(vPath);
        metaFile.readHyperLink(vPath + "/HyperLink.txt");
        metaFile.readBoxSeries(vPath + "/BoxSeries.txt");
        vp = new VideoPlayer(vPath, frameNo);
        ActionListener VAL0 = new VideoPlayerForm.VideoPlayerActionListener(vp, labelVideoDisplay, slider);
        vp.setAL(VAL0);
        setBtnPlayPause(btnPlayPause, vp, slider);
        setIcon(vp.getBufferedImage(), labelVideoDisplay);
        btnPlayPause.setEnabled(true);
        sliderSetup();
        isVideoImported = true;
    }

    private void jumpTo(String vPath, int frameNo) {
        if (isVideoImported) {
            vp.destroy();
            isVideoImported = false;
        }
        loadVideo(vPath, frameNo);
        vp.play();
        btnPlayPause.setText("Pause");
        slider.setEnabled(false);

    }

    private void ImportVideo() {
        ImportVideo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // if already imported, then destroy current VideoPlayer instance
                if (isVideoImported) {
                    vp.destroy();
                    isVideoImported = false;
                }

                // get path and initialize everything
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showSaveDialog(panelBottomLeft) == 0) {
                    VideoPath = fileChooser.getSelectedFile().getAbsolutePath();
                    loadVideo(VideoPath, 0);
                }
            }
        });
    }

    private void setBtnPlayPause(JButton button, VideoPlayer vp, JSlider slider) {
        // remove current action listener, if there is one.
        ActionListener[] currentListeners = button.getActionListeners();
        if (currentListeners.length != 0) button.removeActionListener(currentListeners[0]);

        button.setEnabled(false);
        button.setText("Play");
        button.addActionListener(new VideoPlayerForm.PlayPauseBtnActionListener(button, vp, slider));
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
            if (vp.isPlaying()) {
                vp.pause();
                button.setText("Play");
                slider.setEnabled(true);
            } else {
                vp.play();
                button.setText("Pause");
                slider.setEnabled(false);
            }
        }
    }

    private void sliderSetup() {
        slider.setMaximum(8999);
        slider.setMinimum(0);
        slider.setValue(0);
        slider.setEnabled(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (slider.isEnabled()) vp.gotoFrame(slider.getValue());
                lblNum.setText("Frame Number: " + slider.getValue());

            }
        });
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
            setIcon(vp.getBufferedImage(), videoLabel);
            slider.setValue(vp.getCurrentFrame());
        }
    }

    private void setIcon(BufferedImage bufferedImage1, JLabel videoLabel) {
        videoLabel.setIcon(new ImageIcon(bufferedImage1));
        boxes = metaFile.getBoxes(vp.getCurrentFrame());
        EditorForm.drawBoxes(bufferedImage1, boxes);
        videoLabel.repaint();
    }


}

