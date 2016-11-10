package objects;

import java.util.ArrayList;

public class SDNSwitch {
    private String dpid;
    private ArrayList<SDNPort> port_list = new ArrayList<>();

    public SDNSwitch(String dpid) {
        this.dpid = dpid;
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
