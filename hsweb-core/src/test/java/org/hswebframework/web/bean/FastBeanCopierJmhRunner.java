package org.hswebframework.web.bean;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FastBeanCopierJmhRunner {

    static final String INCLUDE_PROPERTY = "hsweb.fast-bean-copier.jmh.include";
    static final String FORKS_PROPERTY = "hsweb.fast-bean-copier.jmh.forks";

    public static void main(String[] args) throws RunnerException, IOException {
        Path moduleDir = Files.exists(Paths.get("hsweb-core", "pom.xml"))
            ? Paths.get("hsweb-core")
            : Paths.get(".");
        Path resultDir = moduleDir.resolve("target").resolve("jmh-results");
        Files.createDirectories(resultDir);
        Path resultFile = resultDir.resolve("fast-bean-copier.json");

        ChainedOptionsBuilder builder = new OptionsBuilder()
            .include(System.getProperty(INCLUDE_PROPERTY, FastBeanCopierJmhBenchmark.class.getSimpleName()))
            .result(resultFile.toString())
            .resultFormat(ResultFormatType.JSON);

        int forks = Integer.getInteger(FORKS_PROPERTY, -1);
        if (forks >= 0) {
            builder.forks(forks);
        }

        Options options = builder.build();

        new Runner(options).run();
    }
}
