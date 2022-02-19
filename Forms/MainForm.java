
package Forms;

import VideoPlayer.VideoPlayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainForm extends JFrame{
    private JLabel Title;
    private JButton btnVideoPlayer;
    private JButton btnEditor;
    private JPanel panelMain;

    private VideoPlayerForm videoPlayerForm;
    private EditorForm editorForm;

    public MainForm(){
        add(panelMain);
        setSize(640,480);

        btnEditor.setSelected(false);
        btnVideoPlayer.setSelected(false);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });

        btnVideoPlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                videoPlayerForm = new VideoPlayerForm();
                videoPlayerForm.setVisible(true);
                videoPlayerForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                videoPlayerForm.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        super.windowClosing(e);
                    }
                });
            }
        });

        btnEditor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editorForm = new EditorForm();
                editorForm.setVisible(true);
                editorForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editorForm.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        super.windowClosing(e);
                    }
                });
            }
        });
    }

}
