# Benchmarks

This is a set of basic [JMH](http://openjdk.java.net/projects/code-tools/jmh/) micro-benchmarks. The benchmarks build maps of various sizes and perform basic read and write operations. In case of the ShapeMap the benchmarks also read/write using an effective accessor, as well as an entirely ineffective one, which has been trained on four shapes irrelevant to the benchmarked map and needs to fall back on generic lookup.

The benchmarks are focused on reading/writing existing keys.

Below results on my laptop as of 2015-10-18 against JVM 8.

```bash
java -jar target/benchmarks.jar -wi 10 -i 10 -f 1 -bm avgt -tu ns -rf json
# JMH 1.11.1 (released 23 days ago)
# VM version: JDK 1.8.0_51, VM 25.51-b03
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_51.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 10 iterations, 1 s each
# Measurement: 10 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
...
...

# Run complete. Total time: 00:11:09

Benchmark                                                     Mode  Cnt   Score   Error  Units
BigMapBenchmark.putHashMap                                    avgt   10  13.308 ± 1.121  ns/op
BigMapBenchmark.putShapeMap                                   avgt   10   8.973 ± 0.141  ns/op
BigMapBenchmark.putShapeMapAccessor                           avgt   10   6.059 ± 0.701  ns/op
BigMapBenchmark.putShapeMapAccessorSetOnly                    avgt   10   5.245 ± 0.054  ns/op
BigMapBenchmark.readHashMap                                   avgt   10   8.677 ± 0.144  ns/op
BigMapBenchmark.readShapeMap                                  avgt   10   7.499 ± 0.104  ns/op
BigMapBenchmark.readShapeMapAccessor                          avgt   10   3.513 ± 0.038  ns/op
BigMapBenchmark.readShapeMapAccessorFallback                  avgt   10   8.485 ± 0.085  ns/op

MediumMapBenchmark.putHashMap                                 avgt   10  13.274 ± 0.714  ns/op
MediumMapBenchmark.putShapeMap                                avgt   10   9.732 ± 0.113  ns/op
MediumMapBenchmark.putShapeMapAccessor                        avgt   10   5.681 ± 0.044  ns/op
MediumMapBenchmark.putShapeMapAccessorSetOnly                 avgt   10   5.240 ± 0.044  ns/op
MediumMapBenchmark.readHashMap                                avgt   10   9.129 ± 0.107  ns/op
MediumMapBenchmark.readShapeMap                               avgt   10   8.269 ± 0.106  ns/op
MediumMapBenchmark.readShapeMapAccessor                       avgt   10   3.516 ± 0.037  ns/op
MediumMapBenchmark.readShapeMapAccessorFallback               avgt   10   8.829 ± 0.084  ns/op

SmallMapBenchmark.putHashMap                                  avgt   10  11.399 ± 0.201  ns/op
SmallMapBenchmark.putShapeMap                                 avgt   10   8.719 ± 0.279  ns/op
SmallMapBenchmark.putShapeMapAccessor                         avgt   10   5.644 ± 0.058  ns/op
SmallMapBenchmark.putShapeMapAccessorSetOnly                  avgt   10   5.240 ± 0.058  ns/op
SmallMapBenchmark.readHashMap                                 avgt   10   7.718 ± 0.073  ns/op
SmallMapBenchmark.readShapeMap                                avgt   10   7.003 ± 0.076  ns/op
SmallMapBenchmark.readShapeMapAccessor                        avgt   10   3.515 ± 0.051  ns/op
SmallMapBenchmark.readShapeMapAccessorFallback                avgt   10   8.177 ± 0.081  ns/op

SmallMapWithCollisionsBenchmark.putHashMap                    avgt   10  10.383 ± 0.118  ns/op
SmallMapWithCollisionsBenchmark.putShapeMap                   avgt   10  10.883 ± 0.123  ns/op
SmallMapWithCollisionsBenchmark.putShapeMapAccessor           avgt   10   5.662 ± 0.048  ns/op
SmallMapWithCollisionsBenchmark.putShapeMapAccessorSetOnly    avgt   10   5.242 ± 0.151  ns/op
SmallMapWithCollisionsBenchmark.readHashMap                   avgt   10   8.450 ± 0.070  ns/op
SmallMapWithCollisionsBenchmark.readShapeMap                  avgt   10   9.077 ± 0.080  ns/op
SmallMapWithCollisionsBenchmark.readShapeMapAccessor          avgt   10   3.525 ± 0.049  ns/op
SmallMapWithCollisionsBenchmark.readShapeMapAccessorFallback  avgt   10   9.573 ± 0.119  ns/op

```

Micro-benchmarking is hard, but the results do seem consistent with performance expectations regarding the inline caching accessor.


## Running the benchmarks

You must build the benchmark code then run it using the JMH command line.

```bash
mvn clean install
...
java -jar target/benchmarks.jar -h # shows help options
# running with 10 warm up cycles, 10 iterations, 1 fork, measure average time in ns, and write a json output file
java -jar target/benchmarks.jar -wi 10 -i 10 -f 1 -bm avgt -tu ns -rf json

```

You may want to grab the jmh-result.json file the benchmark creates and put it into [JMH-charts](http://nilskp.github.io/jmh-charts/) which will visualize the result.
