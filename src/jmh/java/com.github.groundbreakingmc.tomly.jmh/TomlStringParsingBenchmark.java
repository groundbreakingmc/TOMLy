package com.github.groundbreakingmc.tomly.jmh;

import com.github.groundbreakingmc.tomly.Tomly;
import com.github.groundbreakingmc.tomly.nodes.TomlDocument;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.moandjiezana.toml.Toml;
import org.openjdk.jmh.annotations.*;
import org.tomlj.TomlParseResult;

import java.util.Map;

@State(Scope.Benchmark)
public class TomlStringParsingBenchmark {

    private String sampleToml;

    @Setup(Level.Iteration)
    public void setup() {
        this.sampleToml = """
                title = "TOML Example"
                active = true
                count = 42
                pi = 3.1415
                numbers = [1, 2, 3]
                point = { x = 1, y = 2 }
                """;
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
        return new Toml().read(this.sampleToml);
    }

    @Benchmark
    public Map<String, Object> toml4jParseToMap() {
        final Toml parsed = new Toml().read(this.sampleToml);
        return parsed.toMap();
    }

    // TOMLj
    @Benchmark
    public TomlParseResult tomljParse() {
        return org.tomlj.Toml.parse(this.sampleToml);
    }

    @Benchmark
    public Map<String, Object> tomljParseToMap() {
        org.tomlj.TomlParseResult parsed = org.tomlj.Toml.parse(this.sampleToml);
        return parsed.toMap();
    }
}
