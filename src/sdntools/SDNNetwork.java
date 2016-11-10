package sdntools;

import objects.SDNHost;
import objects.SDNLink;
import objects.SDNPort;
import objects.SDNSwitch;
import org.graphstream.graph.Graph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.util.ArrayList;

public class SDNNetwork {
    private SDNConnector sdn_connector;
    private Graph graph;

    private ArrayList<SDNSwitch> switch_list = new ArrayList<>();
    private ArrayList<SDNHost> host_list = new ArrayList<>();
    private ArrayList<SDNLink> link_list = new ArrayList<>();

    private String SDNSwitches = "/v1.0/topology/switches";
    private String SDNHosts = "/v1.0/topology/hosts";
    private String SDNLinks = "/v1.0/topology/links";

    public SDNNetwork (String connection_url, Graph graph) {
        this.sdn_connector = new SDNConnector(connection_url);
        this.graph = graph;
        this.CreateSDNNetwork();
    }

    private void CreateSDNNetwork () {
        this.LoadSDNNetwork();
        this.DrawDNSNetwork();
    }

    private void LoadSDNNetwork() {
        LoadSDNSwitches();
        LoadSDNLinks();
        LoadSDNHosts();
    }

    private void DrawDNSNetwork() {
        int i = 0;

        for (SDNSwitch sw :switch_list) {
            this.graph.addNode(sw.GetDPID());
            this.graph.getNode(sw.GetDPID()).addAttribute("ui.label", sw.GetDPID());
            this.graph.getNode(sw.GetDPID()).addAttribute("ui.class", "switch");
        }

        for (SDNLink lnk :link_list) {
            this.graph.addEdge("link" + i++, lnk.GetDstDPID(), lnk.GetSrcDPID());
        }

        for (SDNHost hst :host_list) {
            this.graph.addNode(hst.getMac());
            this.graph.getNode(hst.getMac()).addAttribute("ui.label", hst.getMac());
            this.graph.addEdge("link" + i++, hst.getPort().getDPID(), hst.getMac());
            this.graph.getNode(hst.getMac()).addAttribute("ui.class", "host");
        }

        this.graph.addAttribute("ui.stylesheet", "url('file:///home/martin/IdeaProjects/SDNViz/src/stylesheet.css')");
    }

    private void LoadSDNSwitches() {
        JSONArray json_switches = this.sdn_connector.GetSDNJsonArray(SDNSwitches);
        int swc_l = json_switches.length();

        try {
            for (int i = 0; i < swc_l; i++) {
                if (json_switches.getJSONObject(i).has("dpid")) {
                    SDNSwitch tmp_switch = new SDNSwitch(json_switches.getJSONObject(i).getString("dpid"));
                    JSONArray tmp_port_list = new JSONArray(json_switches.getJSONObject(i).get("ports").toString());

                    int prt_l = tmp_port_list.length();

                    for (int j = 0; j < prt_l; j++) {
                        String tmp_hw_addr = tmp_port_list.getJSONObject(j).getString("hw_addr");
                        String tmp_name = tmp_port_list.getJSONObject(j).getString("name");
                        String tmp_port_no = tmp_port_list.getJSONObject(j).getString("port_no");

                        SDNPort tmp_port = new SDNPort(tmp_switch, tmp_hw_addr, tmp_name, tmp_port_no);
                        tmp_switch.AddPort(tmp_port);
                    }

                    System.out.print("Adding switch dpid: " + tmp_switch.GetDPID());
                    System.out.println(" Switch ports: " + tmp_switch.GetPortList().size());

                    switch_list.add(tmp_switch);
                }
            }
        }
        catch (JSONException e) {
            System.out.println("Error while parsing switches:\n" + e);
        }
    }

    private void LoadSDNHosts() {
        JSONArray json_hosts = this.sdn_connector.GetSDNJsonArray(SDNHosts);
        int hst_l = json_hosts.length();

        try {
            for (int i = 0; i < hst_l; i++) {
                JSONObject tmp_port_obj = new JSONObject(json_hosts.getJSONObject(i).get("port").toString());
                String tmp_mac = json_hosts.getJSONObject(i).get("mac").toString();
                JSONArray tmp_ipv4 = new JSONArray(json_hosts.getJSONObject(i).getJSONArray("ipv4").toString());
                JSONArray tmp_ipv6 = new JSONArray(json_hosts.getJSONObject(i).getJSONArray("ipv6").toString());

                int ipv4_l = tmp_ipv4.length();
                int ipv6_l = tmp_ipv6.length();
                ArrayList<String> tmp_ipv4_arr = new ArrayList<>();
                ArrayList<String> tmp_ipv6_arr = new ArrayList<>();

                for (int j = 0; j < ipv4_l; j++) {
                    tmp_ipv4_arr.add(tmp_ipv4.getString(j));
                }

                for (int j = 0; j < ipv6_l; j++) {
                    tmp_ipv6_arr.add(tmp_ipv6.getString(j));
                }
                SDNPort tmp_port = new SDNPort(
                        tmp_port_obj.getString("hw_addr"),
                        tmp_port_obj.getString("name"),
                        tmp_port_obj.getString("port_no"),
                        tmp_port_obj.getString("dpid")
                );
                SDNHost tmp_host = new SDNHost(tmp_port, tmp_mac, tmp_ipv4_arr, tmp_ipv6_arr);

                System.out.print("Adding host mac: " + tmp_host.getMac());
                System.out.println(" Connected to dpid: " + tmp_port.getDPID());
                this.host_list.add(tmp_host);
            }
        }
        catch (JSONException e) {
            System.out.println("Error while parsing switches:\n" + e);
        }
    }

    private void LoadSDNLinks() {
        JSONArray json_links = this.sdn_connector.GetSDNJsonArray(SDNLinks);
        int lnk_l = json_links.length();

        try {
            for (int i = 0; i < lnk_l; i++) {
                JSONObject tmp_link_json = json_links.getJSONObject(i);
                JSONObject tmp_link_src = tmp_link_json.getJSONObject("src");
                JSONObject tmp_link_dst = tmp_link_json.getJSONObject("dst");
                SDNPort tmp_src = new SDNPort(
                        tmp_link_src.getString("hw_addr"),
                        tmp_link_src.getString("name"),
                        tmp_link_src.getString("port_no"),
                        tmp_link_src.getString("dpid")
                );
                SDNPort tmp_dst = new SDNPort(
                        tmp_link_dst.getString("hw_addr"),
                        tmp_link_dst.getString("name"),
                        tmp_link_dst.getString("port_no"),
                        tmp_link_dst.getString("dpid")
                );

                System.out.println(String.format("Adding link connecting %s and %s.", tmp_src.getDPID(), tmp_dst.getDPID()));
                SDNLink tmp_link = new SDNLink(tmp_src, tmp_dst);
                link_list.add(tmp_link);
            }
        }
        catch (JSONException e) {
            System.out.println("Error while parsing links:\n" + e);
        }
    }
}
