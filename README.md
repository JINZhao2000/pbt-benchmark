# pbt-benchmark
## How to use
1. Use docker compose to start to container of neo4j
    ```bash
    docker compose start
    ```
2. Select the benchmark you want to run in `cyou.zhaojin.CypherBenchmarkTest`, to comment or uncomment the annotation `@Benchmark`
3. Then run `./mvnw clean compile` to build the project
4. Run the class `cyou.zhaoin.CypherBenchmarkTest`, it needs an argument as the name of directory