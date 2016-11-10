package objects;

public class SDNLink {
    private SDNPort src;
    private SDNPort dst;

    public SDNLink(SDNPort src, SDNPort dst) {
        this.src = src;
        this.dst = dst;
    }

    public String GetSrcDPID() {
        return this.src.getDPID();
    }

    public String GetDstDPID() {
        return this.dst.getDPID();
    }
}
