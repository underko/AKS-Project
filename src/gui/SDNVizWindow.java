package gui;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import sdntools.SDNNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SDNVizWindow implements ViewerListener {
    private JButton loadButton;
    private JPanel mainPanel;
    private JTextField url_textfield;
    private JLabel url_label;
    private JButton resetButton;
    private boolean loop = true;

    private Graph graph;
    private SDNNetwork sdn_network;
    private Viewer viewer;
    private ViewPanel view;

    private SDNVizWindow() {
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Start();
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Reset();
            }
        });
    }

    private void Start() {
        this.graph = new MultiGraph("Clicks");
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");

        this.sdn_network = new SDNNetwork(url_textfield.getText(), graph);

        Viewer viewer = graph.display();

        // The default action when closing the view is to quit
        // the program.
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // We connect back the viewer to the graph,
        // the graph becomes a sink for the viewer.
        // We also install us as a viewer listener to
        // intercept the graphic events.
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);

        // Then we need a loop to do our work and to wait for events.
        // In this loop we will need to call the
        // pump() method before each use of the graph to copy back events
        // that have already occurred in the viewer thread inside
        // our thread.


        new Thread(new Runnable() { //Create a new thread and pass a Runnable with your while loop to it
            @Override
            public void run() {

                while (loop) {
                    fromViewer.pump(); // or fromViewer.blockingPump(); in the nightly builds

                    // here your simulation code.

                    // You do not necessarily need to use a loop, this is only an example.
                    // as long as you call pump() before using the graph. pump() is non
                    // blocking.  If you only use the loop to look at event, use blockingPump()
                    // to avoid 100% CPU usage. The blockingPump() method is only available from
                    // the nightly builds.
                }
            }
        }).start();

    }

    private void Reset() {
        this.view.getCamera().setViewPercent(0.5);
        this.view.getCamera().resetView();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SDNViz");
        frame.setContentPane(new SDNVizWindow().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setVisible(true);
    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        System.out.println("Button pushed on node " + id);
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node " + id);
    }
}
