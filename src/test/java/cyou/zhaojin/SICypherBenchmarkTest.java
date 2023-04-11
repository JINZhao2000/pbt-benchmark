package cyou.zhaojin;

import org.junit.Test;
import org.neo4j.driver.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

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
public class SICypherBenchmarkTest {
    public final Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "123456"));
    public final static String serializable = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes) yield path return path";

    public final static Pattern pattern = Pattern.compile(":(\\w{2})");
    public final static String rw = "rw";
    @Benchmark
    @Test
    public void executionTest() throws Exception{
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

    public static void main(String[] args) throws Exception{
        final Options opts = new OptionsBuilder()
                .include(SICypherBenchmarkTest.class.getSimpleName())
                .forks(1)
                .measurementIterations(10)
                .warmupIterations(10)
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();
        new Runner(opts).run();
    }
}
