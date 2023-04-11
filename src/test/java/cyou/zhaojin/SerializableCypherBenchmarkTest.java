package cyou.zhaojin;

import org.junit.Test;
import org.neo4j.driver.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author Zhao JIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class SerializableCypherBenchmarkTest {
    public final Driver driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "neo4j"));
    public final static String serializable = "match (n:txn) with collect(n) as nodes call apoc.nodes.cycles(nodes) yield path return path";

    @Benchmark
    @Test
    public void executionTest() throws Exception {
        try (Session session = driver.session()) {
            Result result = session.run(serializable);
        }
    }

    public static void main(String[] args) throws Exception{
        final Options opts = new OptionsBuilder()
                .include(SerializableCypherBenchmarkTest.class.getSimpleName())
                .forks(1)
                .measurementIterations(10)
                .warmupIterations(10)
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();
        new Runner(opts).run();
    }
}
