package gui;

import objects.SDNHost;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.json.JSONException;
import sdntools.SDNNetwork;
import sdntools.X11Forwarding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

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
    private JTextField cmdTF;
    private JTextArea controllerArea;
    private JTextArea ryuArea;
    private JTextArea cmdArea;
    private JTextField controllerTF;
    private JTextField ryuTF;
    private JTextField destRoutTF;
    private JTextField gateTF;

    private Graph graph;
    private SDNNetwork sdn_network;
    private Viewer viewer;
    private ViewPanel view;

    private String myUrl = "http://192.168.100.5:8080";
    private String selectedHost="";
    private String selectedSwitchId;

    private String last_clicked_node = null;
    private String second_last_clicked_node = null;

    private SDNVizWindow() {
        initialize();
    }

    private void Start() {
        loop = true;
        this.graph = new MultiGraph("Clicks");
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");

        this.sdn_network = new SDNNetwork(urlAddrTF.getText(), graph);

        viewer = graph.display();
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

        // trackign last and second last clicked node
        if (last_clicked_node == null)
        {
            last_clicked_node = id;
            second_last_clicked_node = null;
        }
        else
        {
            second_last_clicked_node = last_clicked_node;
            last_clicked_node = id;
        }

        // check if if contains ":", then it is a host
        if (id.contains(":")) {
            this.txtSwitch.setText("Host");
            this.textProperties.setText(sdn_network.GetFormattedInfoSDNHost(id));
            SDNHost h =sdn_network.GetSDNHost(id);
            this.selectedHost=Integer.parseInt(h.getPort().getDPID())+"";
        }
        else {
            this.txtSwitch.setText("Switch");
            this.textProperties.setText(sdn_network.GetFormattedInfoSDNSwitch(id));
            this.selectedSwitchId=id;
        }
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node " + id);
    }
    public void sendRoutAddress(){
        try {
            String[] key={"address"};
            String[] value={routAddrTF.getText()};
            sdn_network.sendSDNPost("/router/" + selectedSwitchId, key,value);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
    public void sendGatewayAddress(){
        try {
            String[] key={"gateway"};
            String[] value={routGateTF.getText()};
            sdn_network.sendSDNPost("/router/" + selectedSwitchId, key,value);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
    public void sendStaticRout(){
        try {
            String[] key={"destination","gateway"};
            String[] value={destRoutTF.getText(),gateTF.getText()};
            sdn_network.sendSDNPost("/router/" + selectedSwitchId, key,value);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

    }
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 727, 684);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        urlAddrTF = new JTextField();
        urlAddrTF.setBounds(95, 613, 205, 20);
	         urlAddrTF.setText(myUrl);
        frame.getContentPane().add(urlAddrTF);
        urlAddrTF.setColumns(10);

        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Start();}
        });
        btnLoad.setBounds(325, 612, 97, 23);
        frame.getContentPane().add(btnLoad);

        btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Reset();
            }
        });
        btnReset.setBounds(432, 612, 90, 23);
        frame.getContentPane().add(btnReset);

        JButton btnPing = new JButton("Ping");
        btnPing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: 12/4/16 add ping functionality, possibly use ping -R if supported
                if (sourceTF.getText().isEmpty() || destTF.getText().isEmpty()) {
                    return;
                }
                controllerTF.setText(sourceTF.getText() + " ping -c 1 " + destTF.getText());
                controllerTF.postActionEvent();
            }
        });
        btnPing.setBounds(10, 542, 77, 23);
        frame.getContentPane().add(btnPing);

        JButton btnHost = new JButton("Send");
        btnHost.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controllerTF.setText("py h" + selectedHost + ".setIP('" + hostIpTF.getText() + "')");
                controllerTF.postActionEvent();
            }
        });
        btnHost.setBounds(613, 11, 77, 23);
        frame.getContentPane().add(btnHost);

        JButton btnDefGate = new JButton("Send");
        btnDefGate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controllerTF.setText("h" + selectedHost + " route add default gw " + defGateTF.getText());
                controllerTF.postActionEvent();
            }
        });
        btnDefGate.setBounds(613, 50, 77, 23);
        frame.getContentPane().add(btnDefGate);

        JButton btnRoutAddr = new JButton("Send");
        btnRoutAddr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendRoutAddress();
            }
        });
        btnRoutAddr.setBounds(613, 84, 77, 23);
        frame.getContentPane().add(btnRoutAddr);

        JButton btnRoutGate = new JButton("Send");
        btnRoutGate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendGatewayAddress();
            }
        });
        btnRoutGate.setBounds(613, 109, 77, 23);
        frame.getContentPane().add(btnRoutGate);

        JButton btnStatic = new JButton("Send");
        btnStatic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendStaticRout();
            }
        });
        btnStatic.setBounds(613, 158, 77, 23);
        frame.getContentPane().add(btnStatic);

        JLabel lblNewLabel = new JLabel("URL:");
        lblNewLabel.setBounds(10, 616, 46, 14);
        frame.getContentPane().add(lblNewLabel);

        txtSwitch = new JTextField();
        txtSwitch.setEditable(false);
        txtSwitch.setText("Load graph and select a node");
        txtSwitch.setBounds(10, 11, 235, 20);
        frame.getContentPane().add(txtSwitch);
        txtSwitch.setColumns(10);

        JLabel lblRoutingIp = new JLabel("Info");
        lblRoutingIp.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblRoutingIp.setBounds(10, 39, 97, 14);
        frame.getContentPane().add(lblRoutingIp);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 52, 235, 420);
        frame.getContentPane().add(scrollPane);

        textProperties = new JTextArea();
        textProperties.setEditable(false);
        scrollPane.setViewportView(textProperties);

        JLabel lblRootingPath = new JLabel("Rooting Path");
        lblRootingPath.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblRootingPath.setBounds(10, 580, 77, 14);
        frame.getContentPane().add(lblRootingPath);

        routPathTF = new JTextField();
        routPathTF.setEditable(false);
        routPathTF.setBounds(95, 577, 519, 20);
        frame.getContentPane().add(routPathTF);
        routPathTF.setColumns(10);

        JLabel lblRoutAddress = new JLabel("Router Address");
        lblRoutAddress.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblRoutAddress.setBounds(255, 88, 94, 14);
        frame.getContentPane().add(lblRoutAddress);

        routAddrTF = new JTextField();
        routAddrTF.setBounds(387, 85, 197, 20);
        frame.getContentPane().add(routAddrTF);
        routAddrTF.setColumns(10);

        JLabel lblRoutGateway = new JLabel("Router Gateway");
        lblRoutGateway.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblRoutGateway.setBounds(255, 113, 94, 14);
        frame.getContentPane().add(lblRoutGateway);

        routGateTF = new JTextField();
        routGateTF.setBounds(387, 110, 197, 20);
        frame.getContentPane().add(routGateTF);
        routGateTF.setColumns(10);

        // PING source field
        sourceTF = new JTextField();
        sourceTF.setBounds(97, 483, 77, 20);
        frame.getContentPane().add(sourceTF);
        sourceTF.setColumns(10);

        // PING destination field
        destTF = new JTextField();
        destTF.setBounds(97, 514, 77, 20);
        frame.getContentPane().add(destTF);
        destTF.setColumns(10);

        JLabel lblSource = new JLabel("Source");
        lblSource.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblSource.setBounds(10, 486, 60, 14);
        frame.getContentPane().add(lblSource);

        JLabel lblDest = new JLabel("Destination");
        lblDest.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblDest.setBounds(10, 517, 77, 14);
        frame.getContentPane().add(lblDest);

        JLabel label = new JLabel("Host IP");
        label.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        label.setBounds(255, 14, 47, 14);
        frame.getContentPane().add(label);

        hostIpTF = new JTextField();
        hostIpTF.setColumns(10);
        hostIpTF.setBounds(321, 12, 133, 20);
        frame.getContentPane().add(hostIpTF);

        JLabel label_1 = new JLabel("Defaul Gateway");
        label_1.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        label_1.setBounds(256, 53, 121, 14);
        frame.getContentPane().add(label_1);

        defGateTF = new JTextField();
        defGateTF.setColumns(10);
        defGateTF.setBounds(387, 50, 197, 20);
        frame.getContentPane().add(defGateTF);

        JLabel label_2 = new JLabel("HostPort");
        label_2.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        label_2.setBounds(469, 14, 53, 14);
        frame.getContentPane().add(label_2);

        hostPortTf = new JTextField();
        hostPortTf.setColumns(10);
        hostPortTf.setBounds(533, 12, 70, 20);
        frame.getContentPane().add(hostPortTf);

        JScrollPane scrollPane_2 = new JScrollPane();
        scrollPane_2.setBounds(675, 418, 111, -154);
        frame.getContentPane().add(scrollPane_2);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(255, 211, 435, 355);
        frame.getContentPane().add(tabbedPane);

        JPanel panel = new JPanel();
        tabbedPane.addTab("Controller", null, panel, null);
        panel.setLayout(null);

        controllerTF = new JTextField();
        controllerTF.setBounds(10, 11, 410, 20);
        panel.add(controllerTF);
        controllerTF.setColumns(10);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(10, 42, 410, 274);
        panel.add(scrollPane_1);

        controllerArea = new JTextArea();
        controllerArea.setWrapStyleWord(true);
        controllerArea.setLineWrap(true);
        scrollPane_1.setViewportView(controllerArea);

        JPanel panel_1 = new JPanel();
        tabbedPane.addTab("Ryu", null, panel_1, null);
        panel_1.setLayout(null);

        ryuTF = new JTextField();
        ryuTF.setBounds(10, 11, 410, 20);
        panel_1.add(ryuTF);
        ryuTF.setColumns(10);

        JScrollPane scrollPane_3 = new JScrollPane();
        scrollPane_3.setBounds(10, 42, 410, 274);
        panel_1.add(scrollPane_3);

        ryuArea = new JTextArea();
        ryuArea.setLineWrap(true);
        ryuArea.setWrapStyleWord(true);
        scrollPane_3.setViewportView(ryuArea);

        JPanel panel_2 = new JPanel();
        tabbedPane.addTab("CMD", null, panel_2, null);
        panel_2.setLayout(null);

        cmdTF = new JTextField();
        cmdTF.setBounds(10, 11, 410, 20);
        panel_2.add(cmdTF);
        cmdTF.setColumns(10);

        JScrollPane scrollPane_4 = new JScrollPane();
        scrollPane_4.setBounds(10, 42, 410, 274);
        panel_2.add(scrollPane_4);

        cmdArea = new JTextArea();
        cmdArea.setWrapStyleWord(true);
        cmdArea.setLineWrap(true);
        scrollPane_4.setViewportView(cmdArea);

        JButton mininetCmd = new JButton("SSH");
        mininetCmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<JTextField> text_field_list = new ArrayList<>();
                ArrayList<JTextArea> text_area_list = new ArrayList<>();

                // these need to be aligned so they match in order
                text_field_list.add(controllerTF);
                text_field_list.add(ryuTF);
                text_field_list.add(cmdTF);

                text_area_list.add(controllerArea);
                text_area_list.add(ryuArea);
                text_area_list.add(cmdArea);

                X11Forwarding.runSerialSSh(text_field_list, text_area_list, myUrl);
            }
        });
        mininetCmd.setBounds(545, 612, 141, 23);
        frame.getContentPane().add(mininetCmd);

        JLabel lblDestination = new JLabel("Destination");
        lblDestination.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblDestination.setBounds(255, 141, 77, 14);
        frame.getContentPane().add(lblDestination);

        JLabel lblGateway = new JLabel("Gateway");
        lblGateway.setFont(lblRoutingIp.getFont().deriveFont(10.0f));
        lblGateway.setBounds(432, 141, 65, 14);
        frame.getContentPane().add(lblGateway);

        destRoutTF = new JTextField();
        destRoutTF.setColumns(10);
        destRoutTF.setBounds(255, 159, 146, 20);
        frame.getContentPane().add(destRoutTF);

        gateTF = new JTextField();
        gateTF.setColumns(10);
        gateTF.setBounds(432, 159, 133, 20);
        frame.getContentPane().add(gateTF);
    }
}
