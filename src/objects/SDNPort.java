package objects;

public class SDNPort {
    private String hw_addr;
    private String name;
    private String port_no;
    private String dpid;

    public SDNPort(String hw_addr, String name, String port_no, String dpid) {
        this.dpid = dpid;
        this.hw_addr = hw_addr;
        this.name = name;
        this.port_no = port_no;
    }

    public SDNPort(SDNSwitch sw, String hw_addr, String name, String port_no) {
        this.dpid = sw.GetDPID();
        this.hw_addr = hw_addr;
        this.name = name;
        this.port_no = port_no;
    }

    public String getHwAddr() {
        return hw_addr;
    }

    public String getName() {
        return name;
    }

    public String getPortNo() {
        return port_no;
    }

    public String getDPID() {
        return dpid;
    }
}
