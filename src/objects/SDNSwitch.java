package objects;

import java.util.ArrayList;

public class SDNSwitch {
    private String dpid;
    private ArrayList<SDNPort> port_list = new ArrayList<>();
    private ArrayList<String> ip_list = new ArrayList<>();
    private ArrayList<SDNswitchRoute> ip_default_list = new ArrayList<>();

    public SDNSwitch(String dpid) {
        this.dpid = dpid;
    }

    public ArrayList<String> getIp_list() {
        return this.ip_list;
    }

    public void setIp_list(ArrayList<String> ip_list) {
        this.ip_list = ip_list;
    }

    public ArrayList<SDNswitchRoute> getIp_default_list() {
        return ip_default_list;
    }

    public void setIp_default_list(ArrayList<SDNswitchRoute> ip_default_list) {
        this.ip_default_list = ip_default_list;
    }
    public void AddPort(SDNPort port) {
        this.port_list.add(port);
    }
    public String GetDPID() {
        return this.dpid;
    }
    public ArrayList<SDNPort> GetPortList() {
        return this.port_list;
    }

}
