package cyou.zhaojin.deprecated;

import java.util.Objects;

/**
 * @author Zhao JIN
 */
@Deprecated
public class Edge {
    private String nodeSrc;
    private String nodeDest;
    private String prop;

    public Edge(String nodeSrc, String nodeDest, String prop) {
        this.nodeSrc = nodeSrc;
        this.nodeDest = nodeDest;
        this.prop = prop;
    }

    public String getNodeSrc() {
        return nodeSrc;
    }

    public void setNodeSrc(String nodeSrc) {
        this.nodeSrc = nodeSrc;
    }

    public String getNodeDest() {
        return nodeDest;
    }

    public void setNodeDest(String nodeDest) {
        this.nodeDest = nodeDest;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return Objects.equals(getNodeSrc(), edge.getNodeSrc()) && Objects.equals(getNodeDest(), edge.getNodeDest()) && Objects.equals(getProp(), edge.getProp());
    }

    public String query() {
        return String.format("MATCH (s:Transaction {id: \"%s\"}), (t:Transaction {id: \"%s\"}) \n MERGE (s)-[r:%s]->(t)\n", this.nodeSrc, this.nodeDest, this.prop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeSrc(), getNodeDest(), getProp());
    }

    @Override
    public String toString() {
        return this.nodeSrc + "-[" + this.prop + "]->" + this.nodeDest;
    }
}
