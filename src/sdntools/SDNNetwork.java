package sdntools;

import objects.*;
import org.graphstream.graph.Graph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class SDNNetwork {
    private SDNConnector sdn_connector;
    private Graph graph;

    public ArrayList<SDNSwitch> getSwitch_list() {
        return switch_list;
    }

    public void setSwitch_list(ArrayList<SDNSwitch> switch_list) {
        this.switch_list = switch_list;
    }

    public ArrayList<SDNHost> getHost_list() {
        return host_list;
    }

    public void setHost_list(ArrayList<SDNHost> host_list) {
        this.host_list = host_list;
    }

    public ArrayList<SDNLink> getLink_list() {
        return link_list;
    }

    public void setLink_list(ArrayList<SDNLink> link_list) {
        this.link_list = link_list;
    }

    private ArrayList<SDNSwitch> switch_list = new ArrayList<>();
    private ArrayList<SDNHost> host_list = new ArrayList<>();
    private ArrayList<SDNLink> link_list = new ArrayList<>();

    private String SDNSwitches = "/v1.0/topology/switches";
    private String SDNHosts = "/v1.0/topology/hosts";
    private String SDNLinks = "/v1.0/topology/links";
    private String SDNrouter = "/router/";

    public SDNNetwork (String connection_url, Graph graph) {
        this.sdn_connector = new SDNConnector(connection_url);
        this.graph = graph;
        this.CreateSDNNetwork();
    }

    public String GetFormattedInfoSDNSwitch(String guid) {
        SDNSwitch s = GetSDNSwitch(guid);


        if (s == null) { return "Object not found."; }
        String port_string = "";
        String route = "";
        String ip = "";

        for (SDNPort p: s.GetPortList()) {
            String port_info = String.format(
                    "Name:\n" +
                    "%s\n" +
                    "HW Address:\n" +
                    "%s\n" +
                    "Port number:\n" +
                    "%s\n" +
                    "DPID:\n" +
                    "%s\n",
                    p.getName(),
                    p.getHwAddr(),
                    p.getPortNo(),
                    p.getDPID()
            );

            port_string += port_info;
        }

        for (SDNswitchRoute sr : s.getIp_default_list()) {
            String route_info = String.format(
                    "ID :" +
                    "%s\n" +
                    "Destination: " +
                    "%s\n" +
                    "Gateway: " +
                    "%s\n\n",
                    sr.getId(),
                    sr.getDestination(),
                    sr.getGateway()
            );
            route += route_info;
        }
        for (String str : s.getIp_list()) {
            String ip_info = String.format(
                    "address:\n" +
                    "%s\n",
                    str
            );
            ip += ip_info;
        }

        String out = String.format(
                "GUID:\n" +
                "%s\n\n" +
                "Ports:\n" +
                "%s\n\n" +
                "Route:\n" +
                "%s\n\n" +
                "Address:\n" +
                "%s\n\n",
                s.GetDPID(),
                port_string,
                route,
                ip
        );

        return out;
    }

    public String GetFormattedInfoSDNHost(String mac) {
        SDNHost h = GetSDNHost(mac);

        if (h == null) { return "Object not found."; }

        String ipv4_text = "";
        String ipv6_text = "";

        for (String ip: h.getIpv4()) {
            ipv4_text += ip + "\n";
        }

        for (String ip: h.getIpv6()) {
            ipv6_text += ip + "\n";
        }

        String port_info = String.format(
                "Name:\n" +
                "%s\n" +
                "HW Address:\n" +
                "%s\n" +
                "Port number:\n" +
                "%s\n" +
                "DPID:\n" +
                "%s\n",
                h.getPort().getName(),
                h.getPort().getHwAddr(),
                h.getPort().getPortNo(),
                h.getPort().getDPID()
        );

        String out = String.format(
                "MAC:\n" +
                "%s\n\n" +
                "Port:\n" +
                "%s\n\n" +
                "IPv4:\n" +
                "%s\n\n" +
                "IPv6:\n" +
                "%s\n\n",
                h.getMac(),
                port_info,
                ipv4_text,
                ipv6_text
        );

        return out;
    }

    private SDNSwitch GetSDNSwitch(String guid) {
        for (SDNSwitch s : switch_list) {
            if (s.GetDPID().equals(guid)) {
                return s;
            }
        }
        return null;
    }

    public SDNHost GetSDNHost(String mac) {
        for (SDNHost h : host_list) {
            if (h.getMac().equals(mac)) {
                return h;
            }
        }
        return null;
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

        String css_url = this.getClass().getResource("stylesheet.css").getPath();
        css_url = "url('file://" + css_url + "')";
        System.out.println("CSS: " + css_url);
        this.graph.addAttribute("ui.stylesheet", css_url);
    }

    private void LoadSDNSwitches() {
        try {
            JSONArray json_switches = this.sdn_connector.GetSDNJsonArray(SDNSwitches);

            if (json_switches == null) { throw new JSONException("JSON Array Empty"); }

            int swc_l = json_switches.length();

            for (int i = 0; i < swc_l; i++) {
                if (json_switches.getJSONObject(i).has("dpid")) {
                    String dpid = json_switches.getJSONObject(i).getString("dpid");
                    SDNSwitch tmp_switch = new SDNSwitch(dpid);

                    JSONArray jsonArray = this.sdn_connector.GetSDNJsonArray(SDNrouter + dpid);
                    if (jsonArray != null && jsonArray.getJSONObject(0).has("internal_network")) {
                        JSONArray internalArr = jsonArray.getJSONObject(0).getJSONArray("internal_network");
                        if (internalArr.getJSONObject(0).has("route")) {
                            JSONArray json_route = internalArr.getJSONObject(0).getJSONArray("route");
                            int rt = json_route.length();
                            ArrayList<SDNswitchRoute> list_route = new ArrayList<>();
                            for (int k = 0; k < rt; k++) {
                                JSONObject json_route_obj = json_route.getJSONObject(k);
                                SDNswitchRoute switchRoute = new SDNswitchRoute(
                                        json_route_obj.getInt("route_id"),
                                        json_route_obj.getString("destination"),
                                        json_route_obj.getString("gateway")
                                );
                                list_route.add(switchRoute);
                            }
                            tmp_switch.setIp_default_list(list_route);
                        }

                        if (internalArr.getJSONObject(0).has("address")) {
                            JSONArray json_address = internalArr.getJSONObject(0).getJSONArray("address");
                            int add = json_address.length();
                            ArrayList<String> list_addr = new ArrayList<>();
                            for (int l = 0; l < add; l++) {
                                JSONObject json_addr_obj = json_address.getJSONObject(l);
                                list_addr.add(json_addr_obj.getString("address"));
                            }
                            tmp_switch.setIp_list(list_addr);
                        }
                    }


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
        try {
            JSONArray json_hosts = this.sdn_connector.GetSDNJsonArray(SDNHosts);
            JSONArray json_tmp = this.sdn_connector.GetSDNJsonArray(SDNHosts);

            if (json_hosts == null || json_tmp == null) { throw new JSONException("JSON Array Empty"); }

            int hst_l = json_hosts.length();

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
                if(tmp_port_obj.getString("name").contains("eth1")){
                    SDNHost tmp_host = new SDNHost(tmp_port, tmp_mac, tmp_ipv4_arr, tmp_ipv6_arr);

                    System.out.print("Adding host mac: " + tmp_host.getMac());
                    System.out.println(" Connected to dpid: " + tmp_port.getDPID());
                    this.host_list.add(tmp_host);
                }
                else{
                    JSONObject tmp_link_json = json_hosts.getJSONObject(i);
                    JSONObject tmp_link_src = tmp_link_json.getJSONObject("port");
                    SDNPort tmp_dst=null;
                    SDNPort tmp_src = new SDNPort(
                            tmp_link_src.getString("hw_addr"),
                            tmp_link_src.getString("name"),
                            tmp_link_src.getString("port_no"),
                            tmp_link_src.getString("dpid")
                    );
                    String mac=tmp_link_json.getString("mac");

                    for(int m=0;m<hst_l;m++){
                        JSONObject tmp_link_json2 = json_tmp.getJSONObject(m);
                        JSONObject tmp_link_dsc = tmp_link_json2.getJSONObject("port");
                        if(tmp_link_dsc.getString("hw_addr").equals(mac)){
                            tmp_dst = new SDNPort(
                                    tmp_link_dsc.getString("hw_addr"),
                                    tmp_link_dsc.getString("name"),
                                    tmp_link_dsc.getString("port_no"),
                                    tmp_link_dsc.getString("dpid")
                            );
                            System.out.println(String.format("Adding link connecting %s and %s.", tmp_src.getDPID(), tmp_dst.getDPID()));
                            SDNLink tmp_link = new SDNLink(tmp_src, tmp_dst);
                            link_list.add(tmp_link);
                            break;
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            System.out.println("Error while parsing switches:\n" + e);
        }
    }

    private void LoadSDNLinks() {
        try {
            JSONArray json_links = this.sdn_connector.GetSDNJsonArray(SDNLinks);

            if (json_links == null) { throw new JSONException("JSON Array Empty"); }

            int lnk_l = json_links.length();

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

    public void sendSDNPost(String url,String[] key,String[] ipAdd) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<key.length;i++){
            jsonObject.accumulate(key[i], ipAdd[i]);
        }
        String json = jsonObject.toString();
        sdn_connector.setSDNcommand(url,json);
    }

    public String getRoutingString(String src, String dst) {
        String route_string = "";

        // TODO: 12/12/16 Return String containing routing path

        return route_string;
    }
}
