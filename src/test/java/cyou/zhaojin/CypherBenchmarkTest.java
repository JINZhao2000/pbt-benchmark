package cyou.zhaojin;

import org.junit.Test;
import org.neo4j.driver.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhao JIN
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class CypherBenchmarkTest {
    public final Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "neo4j"));
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
    public static final String rlist = "match (n1:txn)-[r]->(n2:txn) where id(n1) in %s and id(n2) in %s with collect(type(r)) as r return r";

//    @Benchmark
    public void SerTest() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(serializable);
        }
    }

//    @Benchmark
    public void SITest() throws Exception{
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
                for (int i = 1; i < cycle.size(); i++) {
                    if (cycle.get(i).equals(rw) && cycle.get(i).equals(rw)) {
                        findRW = true;
                        break;
                    }
                }
                if (!findRW) {
                    break;
                }
            }
        }
    }

//    @Benchmark
    public void PSITest() throws Exception{
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
                    break;
            }
        }
    }

//    @Benchmark
    public void PL2Test() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(pl2);
        }
    }

//    @Benchmark
    public void PL1Test() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(pl1);
        }
    }

    @Benchmark
    public void Q1SerProjTest() throws Exception {
        try (Session session = driver.session()){
            session.run(dropproj);
            session.run(serproj);
        }
    }

    @Benchmark
    public void Q2SerSCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(scc);
        }
    }

    @Benchmark
    @Test
    public void Q3_SISCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(sccstream);
            while (result.hasNext()) {
                String list = result.next().get(0).toString();
                Result innerResult = session.run(String.format(rlist, list, list));
                List<String> cycle = innerResult.next().get(0).asList(Value::asString);
                boolean findRW = false;
                for (int i = 1; i < cycle.size(); i++) {
                    if (cycle.get(i).equals(rw) && cycle.get(i).equals(rw)) {
                        findRW = true;
                        break;
                    }
                }
                if (!findRW) {
                    break;
                }
            }
        }
    }

    @Benchmark
    public void Q4PSISCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(sccstream);
            while (result.hasNext()) {
                String list = result.next().get(0).toString();
                Result innerResult = session.run(String.format(rlist, list, list));
                List<String> cycle = innerResult.next().get(0).asList(Value::asString);
                if (cycle.stream().filter(rw::equals).count() < 2)
                    break;
            }
        }
    }

    @Benchmark
    public void Q5PL2ProjTest() throws Exception {
        try (Session session = driver.session()){
            session.run(dropproj);
            session.run(pl2proj);
        }
    }

    @Benchmark
    public void Q6PL2SCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(scc);
        }
    }

    @Benchmark
    public void Q7PL1ProjTest() throws Exception {
        try (Session session = driver.session()){
            session.run(dropproj);
            session.run(pl1proj);
        }
    }

    @Benchmark
    public void Q8PL1SCCTest() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(scc);
        }
    }

    @Test
    public void importG() throws Exception {
        Application.importGraph("list_append", 60);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || (!args[0].equals("rw_register") && !args[0].equals("list_append"))) {
            System.out.println("Args: \nlist_append\nrw_register");
            System.exit(0);
        }
        int max = 200;
        for (int i = 10; i <= 20; i+= 10) {
            Application.importGraph(args[0], i);
            final Options opts = new OptionsBuilder()
                    .include(CypherBenchmarkTest.class.getSimpleName())
                    .forks(1)
                    .measurementIterations(1)
                    .warmupIterations(1)
                    .measurementTime(TimeValue.seconds(1))
                    .warmupTime(TimeValue.seconds(1))
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .verbosity(VerboseMode.SILENT)
                    .build();
            Collection<RunResult> results = new Runner(opts).run();
            System.out.println("file:"+args[0]+" "+i);
            results.forEach(r -> {
                System.out.println(r.getPrimaryResult().getLabel() + "\t" + r.getPrimaryResult().getScore());
            });
            System.out.println();
        }
    }
}
