package MetaData;

public class HyperLink {
    public String linkName;
    public String targetVideoPath;
    public int targetFrame;


    public HyperLink(String linkName, String targetVideoPath, int targetFrame) {
        this.linkName = linkName;
        this.targetVideoPath = targetVideoPath;
        this.targetFrame = targetFrame;
    }

    public HyperLink() {

    }


    public String toString() {
        return this.linkName;
    }

}
