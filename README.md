# pbt-benchmark
## How to use
1. Use docker compose to start to container of neo4j
    ```bash
    docker compose start
    ```
2. Modify the name of file in `cyou.zhaojin.Application` with the name `NUM` from 10 to 200
3. Run the `Application.class` to import the graph into neo4j
4. Run the `main` method in `cyou.zhaojin.CypherBenchmarkTest` the test directory
    We will have 5 dependent benchmarks with their results.