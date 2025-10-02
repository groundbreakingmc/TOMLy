package com.github.groundbreakingmc.tomly.jmh;

import com.github.groundbreakingmc.tomly.Tomly;
import com.github.groundbreakingmc.tomly.nodes.TomlDocument;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.moandjiezana.toml.Toml;
import org.openjdk.jmh.annotations.*;
import org.tomlj.TomlParseResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@State(Scope.Benchmark)
public class TomlFileParsingBenchmark {

    private Path sampleToml;
    private File sampleTomlFile;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        final Path tmp = Files.createTempFile("test", ".toml");
        tmp.toFile().deleteOnExit();

        final String tomlContent = """
                title = "TOML Example"
                active = true
                count = 42
                pi = 3.1415
                numbers = [1, 2, 3]
                point = { x = 1, y = 2 }
                """;
        Files.writeString(tmp, tomlContent, StandardCharsets.UTF_8);

        this.sampleToml = tmp;
        this.sampleTomlFile = tmp.toFile();
    }

    // TOMLy (this)
    @Benchmark
    public TomlDocument tomlyParse() {
        return Tomly.parse(this.sampleToml, false, PreserveOptions.defaultOptions());
    }

    @Benchmark
    public Map<String, Object> tomlyParseRaw() {
        final TomlDocument parsed = Tomly.parse(this.sampleToml, false, PreserveOptions.defaultOptions());
        return parsed.raw();
    }

    // TOML4j
    @Benchmark
    public Toml toml4jParse() {
        return new Toml().read(this.sampleTomlFile);
    }

    @Benchmark
    public Map<String, Object> toml4jParseToMap() {
        final Toml parsed = new Toml().read(this.sampleTomlFile);
        return parsed.toMap();
    }

    // TOMLj
    @Benchmark
    public TomlParseResult tomljParse() throws IOException {
        return org.tomlj.Toml.parse(this.sampleToml);
    }

    @Benchmark
    public Map<String, Object> tomljParseToMap() throws IOException {
        org.tomlj.TomlParseResult parsed = org.tomlj.Toml.parse(this.sampleToml);
        return parsed.toMap();
    }
}
