package gui;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import sdntools.SDNNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SDNVizWindow {
    private JButton loadButton;
    private JPanel mainPanel;
    private JTextField url_textfield;
    private JLabel url_label;
    private JPanel display_panel;
    private JButton resetButton;

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
        this.graph = new MultiGraph("embedded");
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");

        this.sdn_network = new SDNNetwork(url_textfield.getText(), graph);

        this.viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        this.viewer.enableAutoLayout();
        this.view = viewer.getDefaultView();

        this.graph.display();
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
}
