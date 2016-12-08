package objects;

/**
 * Created by Toni on 8. 12. 2016.
 */
public class SDNswitchRoute {
    private int id;
    private String destination;
    private String gateway;

    public SDNswitchRoute(int id, String destination, String gateway) {
        this.id = id;
        this.destination = destination;
        this.gateway = gateway;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


}
