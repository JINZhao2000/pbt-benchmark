package cyou.zhaojin;

import cyou.zhaojin.graph.ImportUtil;
import cyou.zhaojin.obj.Edge;
import cyou.zhaojin.obj.Vertex;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.List;

/**
 * @author Zhao JIN
 */
public class Application {
    public static final String VERTEX = "txn";
    public static final String EDGE = "dep";
    public static final String POSTFIX = ".json";
    public static int NUM = 200;

    public static void main(String[] args) throws Exception {
        final Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "123456"));
        List<Vertex> vertices = ImportUtil.getVertex(VERTEX + NUM + POSTFIX);
        List<Edge> edges = ImportUtil.getEdge(EDGE + NUM + POSTFIX);
        try (Session session = driver.session()){
            session.run("match (n) detach delete (n)");
            StringBuilder vertex = new StringBuilder("create ");
            for (int i = 0; i < vertices.size(); i++) {
                vertex.append("(n").append(i).append(":txn{_id:\"").append(vertices.get(i).get_id()).append("\"}),");
            }
            vertex.delete(vertex.length()-1, vertex.length());
            session.run(vertex.toString());
            edges.forEach(e -> {
                String edge = "match (n1:txn), (n2:txn) where n1.`_id`=\""+e.get_from()+"\" and n2.`_id`=\""+e.get_to()+"\" create (n1)-[:"+e.getType()+"]->(n2)";
                session.run(edge);
            });
        }
    }
}
