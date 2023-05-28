package cyou.zhaojin;

import cyou.zhaojin.graph.ImportUtil;
import cyou.zhaojin.obj.Edge;
import cyou.zhaojin.obj.Vertex;
import org.neo4j.driver.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhao JIN
 */
public class Application {
    public static final String VERTEX = "txn";
    public static final String EDGE = "dep";
    public static final String POSTFIX = ".json";

    public static final Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "12345678"));
    public final static String serializable = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes) yield path return path";
    public final static Pattern pattern = Pattern.compile(":(\\w{2})");

    public final static String rw = "rw";
    public static final String pl2 = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes, {relTypes: ['ww','wr']}) yield path return path";
    public static final String pl1 = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes, {relTypes: ['ww']}) yield path return path";

    public static final String dropproj = "call gds.graph.drop('pbt')";
    public static final String scc = "call gds.alpha.scc.write('pbt', {}) yield maxSetSize as s return s";
    public static final String serproj = "CALL gds.graph.project.cypher( 'pbt', 'MATCH (n:txn) RETURN id(n) AS id', 'MATCH (n:txn)-->(n2:txn) RETURN id(n) AS source, id(n2) AS target')";
    public static final String pl2proj = "CALL gds.graph.project.cypher( 'pbt', 'MATCH (n:txn) RETURN id(n) AS id', 'MATCH (n:txn)-[:ww|wr]->(n2:txn) RETURN id(n) AS source, id(n2) AS target')";
    public static final String pl1proj = "CALL gds.graph.project.cypher( 'pbt', 'MATCH (n:txn) RETURN id(n) AS id', 'MATCH (n:txn)-[:ww]->(n2:txn) RETURN id(n) AS source, id(n2) AS target')";
    public static final String sccstream = "CALL gds.alpha.scc.stream('pbt', {}) YIELD nodeId, componentId WITH componentId, COLLECT(nodeId) AS nodes, COUNT(nodeId) AS num WHERE num > 1 RETURN nodes";
    public static final String smallcycle = "match (n:txn) where id(n) in %s with collect(n) as nodes call apoc.nodes.cycles(nodes) yield path return path";

    public static boolean SerTest() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(serializable);
            return !result.hasNext();
        }
    }

    public static boolean SITest() throws Exception{
        try(Session session = driver.session()) {
            Result result = session.run(serializable);
            while (result.hasNext()) {
                Record next = result.next();
                String res = next.get("path").toString();
                Matcher matcher = pattern.matcher(res);
                List<String> cycle = new ArrayList<>();
                while (matcher.find())
                    cycle.add(matcher.group(1));
                boolean findRW = false;
                for (int i = 1; i <= cycle.size(); i++) {
                    if (i == cycle.size()) {
                        if (cycle.get(i - 1).equals(rw) && cycle.get(0).equals(rw)) {
                            findRW = true;
                            break;
                        }
                    } else {
                        if (cycle.get(i - 1).equals(rw) && cycle.get(i).equals(rw)) {
                            findRW = true;
                            break;
                        }
                    }
                }
                if (!findRW) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean PSITest() throws Exception{
        // the kernel of neo4j is java
        try(Session session = driver.session()) {
            Result result = session.run(serializable);
            while (result.hasNext()) {
                Record next = result.next();
                String res = next.get("path").toString();
                Matcher matcher = pattern.matcher(res);
                List<String> cycle = new ArrayList<>();
                while (matcher.find())
                    cycle.add(matcher.group(1));
                if (cycle.stream().filter(rw::equals).count() < 2)
                    return false;
            }
            return true;
        }
    }

    public static boolean PL2Test() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(pl2);
            return !result.hasNext();
        }
    }

    public static boolean PL1Test() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(pl1);
            return !result.hasNext();
        }
    }

    public static boolean Q3_SISCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(sccstream);
            while (result.hasNext()) {
                String list = result.next().get(0).toString();
                Result innerResult = session.run(String.format(smallcycle, list));
                while (innerResult.hasNext()) {
                    String res = innerResult.next().get("path").toString();
                    Matcher matcher = pattern.matcher(res);
                    List<String> cycle = new ArrayList<>();
                    while (matcher.find())
                        cycle.add(matcher.group(1));
                    boolean findRW = false;
                    for (int i = 1; i <= cycle.size(); i++) {
                        if (i == cycle.size()) {
                            if (cycle.get(i - 1).equals(rw) && cycle.get(0).equals(rw)) {
                                findRW = true;
                                break;
                            }
                        } else {
                            if (cycle.get(i - 1).equals(rw) && cycle.get(i).equals(rw)) {
                                findRW = true;
                                break;
                            }
                        }
                    }
                    if (!findRW) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static boolean Q4PSISCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(sccstream);
            while (result.hasNext()) {
                String list = result.next().get(0).toString();
                Result innerResult = session.run(String.format(smallcycle, list));
                while (innerResult.hasNext()) {
                    String res = innerResult.next().get("path").toString();
                    Matcher matcher = pattern.matcher(res);
                    List<String> cycle = new ArrayList<>();
                    while (matcher.find())
                        cycle.add(matcher.group(1));
                    if (cycle.stream().filter(rw::equals).count() < 2)
                        return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        String[] nameSet = new String[]{"rate", "collection_time_nemesis", "rate_nemesis", "history_30s"};
        for (String t : nameSet) {
            System.out.println(t);
            for (int i = 10; i <= 200; i+=10) {
                importGraph(t, i);
//                String res = t + " " + i + ":\t" +
//                        SerTest() + "\t" +
//                        SITest() + "\t" +
//                        PSITest() + "\t" +
//                        PL2Test() + "\t" +
//                        PL1Test();
//                System.out.println(res);
                System.out.println("expect:\t" + SITest() + "\t" + PSITest());
            }
        }
    }

    public static void importGraph(String type, int num) throws IOException {
        List<Vertex> vertices = ImportUtil.getVertex(type + "/" + VERTEX + num + POSTFIX);
        List<Edge> edges = ImportUtil.getEdge(type + "/" + EDGE + num + POSTFIX);
        try (Session session = driver.session()){
            Result hasGDSGraph = session.run("call gds.graph.exists('pbt')");
                if (hasGDSGraph.next().get("exists").isTrue()) {
                    session.run("call gds.graph.drop('pbt')");
                }
            session.run("match (n) detach delete (n)");
            StringBuilder vertex = new StringBuilder("create ");
            for (int i = 0; i < vertices.size(); i++) {
                vertex.append("(n").append(i).append(":txn{_id:\"").append(vertices.get(i).get_id()).append("\"}),");
            }
            vertex.delete(vertex.length()-1, vertex.length());
            session.run(vertex.toString());
            edges.forEach(e -> {
                String edge = "match (n1:txn), (n2:txn) where n1.`_id`=\""+e.get_from()+"\" and n2.`_id`=\""+e.get_to()+"\" create (n1)-[:"+e.getType()+"{from: '"+e.getFrom_evt()+"', to: '"+e.getTo_evt()+"'}]->(n2)";
                session.run(edge);
            });
            session.run("CALL gds.graph.project.cypher( 'pbt', 'MATCH (n:txn) RETURN id(n) AS id', 'MATCH (n:txn)-->(n2:txn) RETURN id(n) AS source, id(n2) AS target')");
        }
    }
}
