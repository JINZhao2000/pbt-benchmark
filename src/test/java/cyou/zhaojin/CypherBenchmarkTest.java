package cyou.zhaojin;

import org.neo4j.driver.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
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
    public static final String pl1 = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes, {relTypes: ['ww','wr']}) yield path return path";
    public static final String pl0 = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes, {relTypes: ['ww']}) yield path return path";

    @Benchmark
    public void SerTest() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(serializable);
        }
    }

    @Benchmark
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

    @Benchmark
    public void PSITest() throws Exception{
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

    @Benchmark
    public void PL1Test() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(pl1);
        }
    }

    @Benchmark
    public void PL0Test() throws Exception {
        try (Session session = driver.session()){
            Result result = session.run(pl0);
        }
    }

    public static void main(String[] args) throws RunnerException {
        final Options opts = new OptionsBuilder()
                .include(CypherBenchmarkTest.class.getSimpleName())
                .forks(1)
                .measurementIterations(10)
                .warmupIterations(10)
                .measurementTime(TimeValue.seconds(5))
                .warmupTime(TimeValue.seconds(5))
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();
        new Runner(opts).run();
    }
}
