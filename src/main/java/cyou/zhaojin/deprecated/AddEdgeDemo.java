package cyou.zhaojin.deprecated;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Zhao JIN
 */
@Deprecated
public class AddEdgeDemo {
    public static void main(String[] args) {
        final Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "123456"));
        final String objs = "match (m:Event) return distinct m.object as objlst";
        final String lst = "match (m)-[:In]->(n) where m.object = \"%s\" return m.type, n.id, m.value as list";
        final Set<Edge> edgeSet = new HashSet<>();
        try (Session session = driver.session()){
            Result objlst = session.run(objs);
            while (objlst.hasNext()) {
                String obj = objlst.next().get(0).asString();
                Result res = session.run(String.format(lst, obj));
                List<String> lastRead = new ArrayList<>();
                String lastWrite = "";
                while (res.hasNext()) {
                    Record rec = res.next();
                    String type = rec.get("m.type").asString();
                    String curr = rec.get("n.id").asString();
                    if ("Read".equals(type)) {
                        if (!lastWrite.equals("")) {
                            if (!lastWrite.equals(curr)) {
                                edgeSet.add(new Edge(lastWrite, curr, "WR"));
                            }
                            lastRead.add(curr);
                        }
                    }
                    if ("Write".equals(type)) {
                        if (!lastWrite.equals("")) {
                            if (!lastWrite.equals(curr)) {
                                edgeSet.add(new Edge(lastWrite, curr, "WW"));
                            }
                        }
                        for (String r : lastRead) {
                            if (!lastWrite.equals(r)) {
                                edgeSet.add(new Edge(lastWrite, r, "RW"));
                            }
                        }
                        lastWrite = curr;
                        lastRead = new ArrayList<>();
                    }
                }
//            System.out.println(Arrays.stream(list.substring(1, list.length() - 2).split(", ")).map(s -> Integer.parseInt(s)).collect(Collectors.toList()));
            }
            edgeSet.forEach(e -> session.run(e.query()));
        }
//        edgeSet.stream().filter(e -> e.getNodeDest().equals("6461")).collect(Collectors.toList()).forEach(e -> System.out.println(e.query()));
    }
}
