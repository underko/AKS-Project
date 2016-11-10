package objects;

import java.util.ArrayList;

public class SDNHost {
    private SDNPort port;
    private String mac;
    private ArrayList<String> ipv4;
    private ArrayList<String> ipv6;

    public SDNHost(SDNPort port, String mac, ArrayList<String> ipv4, ArrayList<String> ipv6) {
        this.port = port;
        this.mac = mac;
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
    }

    public SDNPort getPort() {
        return port;
    }

    public String getMac() {
        return mac;
    }

    public ArrayList<String> getIpv4() {
        return ipv4;
    }

    public ArrayList<String> getIpv6() {
        return ipv6;
    }
}
