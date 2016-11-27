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

    private boolean loop = true;
    private JFrame frame;
    private JTextField urlAddrTF;
    private JButton btnReset;
    private JTextField txtSwitch;
    private JTextField routPathTF;
    private JTextField routAddrTF;
    private JTextField routGateTF;
    private JTextField sourceTF;
    private JTextField destTF;
    private JTextField hostIpTF;
    private JTextField defGateTF;
    private JTextField hostPortTf;
    private JTextArea textProperties;

    private Graph graph;
    private SDNNetwork sdn_network;
    private Viewer viewer;
    private ViewPanel view;
    private String myUrl = "http://192.168.1.25:8080";

    private SDNVizWindow() {
        initialize();
    }

    private void Start() {
        this.graph = new MultiGraph("Clicks");
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");

        this.sdn_network = new SDNNetwork(urlAddrTF.getText(), graph);

        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (loop) {
                    fromViewer.pump();
                }
            }
        }).start();
    }

    private void Reset() {
        this.view.getCamera().setViewPercent(0.5);
        this.view.getCamera().resetView();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SDNVizWindow window = new SDNVizWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        System.out.println("Button pushed on node " + id);
        this.textProperties.setText(sdn_network.GetFormattedInfoSDNSwitch(id));
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node " + id);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 733, 515);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        urlAddrTF = new JTextField();
        urlAddrTF.setBounds(95, 409, 205, 20);
        urlAddrTF.setText(myUrl);
        frame.getContentPane().add(urlAddrTF);
        urlAddrTF.setColumns(10);

        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Start();
            }
        });
        btnLoad.setBounds(325, 408, 97, 23);
        frame.getContentPane().add(btnLoad);

        btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Reset();
            }
        });
        btnReset.setBounds(432, 408, 90, 23);
        frame.getContentPane().add(btnReset);

        JButton btnPing = new JButton("Ping");
        btnPing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnPing.setBounds(613, 179, 77, 23);
        frame.getContentPane().add(btnPing);

        JButton btnHost = new JButton("Send");
        btnHost.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnHost.setBounds(613, 11, 77, 23);
        frame.getContentPane().add(btnHost);

        JButton btnDefGate = new JButton("Send");
        btnDefGate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnDefGate.setBounds(613, 50, 77, 23);
        frame.getContentPane().add(btnDefGate);

        JButton btnRoutAddr = new JButton("Send");
        btnRoutAddr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnRoutAddr.setBounds(613, 102, 77, 23);
        frame.getContentPane().add(btnRoutAddr);

        JButton btnRoutGate = new JButton("Send");
        btnRoutGate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btnRoutGate.setBounds(613, 133, 77, 23);
        frame.getContentPane().add(btnRoutGate);

        JLabel lblNewLabel = new JLabel("URL:");
        lblNewLabel.setBounds(10, 412, 46, 14);
        frame.getContentPane().add(lblNewLabel);

        txtSwitch = new JTextField();
        txtSwitch.setEditable(false);
        txtSwitch.setText("Switch/Host");
        txtSwitch.setBounds(10, 11, 235, 20);
        frame.getContentPane().add(txtSwitch);
        txtSwitch.setColumns(10);

        JLabel lblRoutingIp = new JLabel("Info");
        lblRoutingIp.setBounds(10, 39, 97, 14);
        frame.getContentPane().add(lblRoutingIp);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 52, 235, 313);
        frame.getContentPane().add(scrollPane);

        textProperties = new JTextArea();
        textProperties.setEditable(false);
        scrollPane.setViewportView(textProperties);

        JLabel lblRootingPath = new JLabel("Rooting Path");
        lblRootingPath.setBounds(10, 376, 77, 14);
        frame.getContentPane().add(lblRootingPath);

        routPathTF = new JTextField();
        routPathTF.setEditable(false);
        routPathTF.setBounds(95, 373, 519, 20);
        frame.getContentPane().add(routPathTF);
        routPathTF.setColumns(10);

        JLabel lblRootAddress = new JLabel("Rout Address");
        lblRootAddress.setBounds(255, 106, 94, 14);
        frame.getContentPane().add(lblRootAddress);

        routAddrTF = new JTextField();
        routAddrTF.setBounds(387, 103, 197, 20);
        frame.getContentPane().add(routAddrTF);
        routAddrTF.setColumns(10);

        JLabel lblRoutGateway = new JLabel("Rout Gateway");
        lblRoutGateway.setBounds(255, 137, 94, 14);
        frame.getContentPane().add(lblRoutGateway);

        routGateTF = new JTextField();
        routGateTF.setBounds(387, 134, 197, 20);
        frame.getContentPane().add(routGateTF);
        routGateTF.setColumns(10);

        sourceTF = new JTextField();
        sourceTF.setBounds(325, 180, 77, 20);
        frame.getContentPane().add(sourceTF);
        sourceTF.setColumns(10);

        destTF = new JTextField();
        destTF.setBounds(507, 180, 77, 20);
        frame.getContentPane().add(destTF);
        destTF.setColumns(10);

        JLabel lblSource = new JLabel("Source");
        lblSource.setBounds(255, 183, 60, 14);
        frame.getContentPane().add(lblSource);

        JLabel lblDest = new JLabel("Destination");
        lblDest.setBounds(420, 183, 77, 14);
        frame.getContentPane().add(lblDest);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(255, 208, 359, 155);
        frame.getContentPane().add(scrollPane_1);

        JTextArea textArea_1 = new JTextArea();
        scrollPane_1.setViewportView(textArea_1);

        JLabel label = new JLabel("Host IP");
        label.setBounds(255, 14, 47, 14);
        frame.getContentPane().add(label);

        hostIpTF = new JTextField();
        hostIpTF.setColumns(10);
        hostIpTF.setBounds(321, 12, 133, 20);
        frame.getContentPane().add(hostIpTF);

        JLabel label_1 = new JLabel("Defaul Gateway");
        label_1.setBounds(256, 53, 121, 14);
        frame.getContentPane().add(label_1);

        defGateTF = new JTextField();
        defGateTF.setColumns(10);
        defGateTF.setBounds(387, 50, 197, 20);
        frame.getContentPane().add(defGateTF);

        JLabel label_2 = new JLabel("HostPort");
        label_2.setBounds(469, 14, 53, 14);
        frame.getContentPane().add(label_2);

        hostPortTf = new JTextField();
        hostPortTf.setColumns(10);
        hostPortTf.setBounds(533, 12, 70, 20);
        frame.getContentPane().add(hostPortTf);
    }
}
