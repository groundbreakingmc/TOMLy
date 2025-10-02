# TOMLy ![Last Release Version](https://img.shields.io/github/v/release/groundbreakingmc/TOMLy) ![Last Commit](https://img.shields.io/github/last-commit/groundbreakingmc/TOMLy)

A high-performance, fully compliant TOML v1.0.0 parser for Java with comment preservation and flexible configuration
options.

[![CodeFactor](https://www.codefactor.io/repository/github/groundbreakingmc/tomly/badge)](https://www.codefactor.io/repository/github/groundbreakingmc/tomly)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-brightgreen.svg)](https://openjdk.org/)
[![Wiki](https://img.shields.io/badge/docs-Wiki-informational.svg)](https://github.com/groundbreakingmc/TOMLy/wiki)
[![Build And Run Tests - TOMLy](https://github.com/groundbreakingmc/TOMLy/actions/workflows/ci.yml/badge.svg)](https://github.com/groundbreakingmc/TOMLy/actions/workflows/ci.yml)

## Features

- ✅ Full TOML v1.0.0 compliance - supports all data types and syntax features
- 🚀 High performance - optimized parsing with minimal memory overhead
- 💬 Comment preservation - optionally retain header and inline comments
- 🎯 Type-safe API - rich typed getters with default value support
- 🔄 Round-trip serialization - parse and write back to TOML format
- 📝 Detailed error reporting - precise line/column information in exceptions
- 🛠️ Flexible configuration - control formatting and preservation options
- 📘 Easy to learn – [Usage examples and guides in the Wiki](https://github.com/GroundbreakingMC/TOMLy/wiki)

## Performance

TOMLy consistently outperforms other popular Java TOML libraries:

*Benchmarks conducted on AMD Ryzen 9 7950X 16-Core with GraalVM 23*

### String Parsing Performance

| Library          | Average Time (ns/op) | Relative Performance |
|------------------|----------------------|----------------------|
| **TOMLy**        | **1,190**            | **1.0x (fastest)**   |
| TOMLy (raw mode) | 1,408                | 1.18x                |
| toml4j           | 1,352                | 1.14x                |
| toml4j (to map)  | 1,416                | 1.19x                |
| tomlj            | 18,346               | 15.4x                |
| tomlj (to map)   | 18,438               | 15.5x                |

### File Parsing Performance

| Library          | Average Time (ns/op) | Relative Performance |
|------------------|----------------------|----------------------|
| **TOMLy**        | **7,702**            | **1.0x (fastest)**   |
| TOMLy (raw mode) | 7,943                | 1.03x                |
| toml4j           | 7,940                | 1.03x                |
| toml4j (to map)  | 7,989                | 1.04x                |
| tomlj            | 24,252               | 3.15x                |
| tomlj (to map)   | 24,492               | 3.18x                |

## Installation

Add JitPack repository to your build configuration:

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.GroundbreakingMC:TOMLy:1.0.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.GroundbreakingMC:TOMLy:1.0.0'
}
```

### Maven

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.GroundbreakingMC</groupId>
        <artifactId>TOMLy</artifactId>
        <version>1.0.0</version>
    </dependency>
<dependencies>
```

## Benchmarks

<details>
<summary>Cold-State Benchmarks</summary>

*Benchmarks conducted on AMD Ryzen 9 7950X 16-Core with GraalVM 23 (single-shot mode)*

### String Parsing

| Library          | Time (ns/op) | Relative Performance |
|------------------|--------------|--------------------|
| **TOMLy**        | **4,122,371** | **1.0x (fastest)** |
| TOMLy (raw mode) | 4,437,693    | 1.08x              |
| toml4j           | 17,816,151   | 4.32x              |
| toml4j (to map)  | 14,598,718   | 3.54x              |
| tomlj            | 34,612,680   | 8.40x              |
| tomlj (to map)   | 39,973,694   | 9.70x              |

### File Parsing

| Library          | Time (ns/op) | Relative Performance |
|------------------|--------------|--------------------|
| **TOMLy**        | **4,572,034** | **1.0x (fastest)** |
| TOMLy (raw mode) | 4,733,316    | 1.03x              |
| toml4j           | 17,646,884   | 3.86x              |
| toml4j (to map)  | 15,440,536   | 3.38x              |
| tomlj            | 33,040,903   | 7.23x              |
| tomlj (to map)   | 34,509,476   | 7.55x              |

</details>

<details>
<summary>Warmup / Hot-State Benchmarks</summary>

*Benchmarks conducted on AMD Ryzen 9 7950X 16-Core with GraalVM 23*

### String Parsing

| Library          | Average Time (ns/op) | Relative Performance |
|------------------|----------------------|----------------------|
| **TOMLy**        | **858**              | **1.0x (fastest)**   |
| TOMLy (raw mode) | 985                  | 1.15x                |
| toml4j           | 1,329                | 1.55x                |
| toml4j (to map)  | 1,386                | 1.61x                |
| tomlj            | 18,160               | 21.2x                |
| tomlj (to map)   | 18,204               | 21.2x                |

### File Parsing

| Library          | Average Time (ns/op) | Relative Performance |
|------------------|----------------------|----------------------|
| **TOMLy**        | **6,435**            | **1.0x (fastest)**   |
| TOMLy (raw mode) | 6,585                | 1.02x                |
| toml4j           | 7,565                | 1.18x                |
| toml4j (to map)  | 7,609                | 1.18x                |
| tomlj            | 23,684               | 3.68x                |
| tomlj (to map)   | 23,595               | 3.67x                |

</details>

## API Overview

### Core Classes

- **`Tomly`**: Main entry point for parsing operations
- **`DocumentNode`**: Root node representing the entire TOML document
- **`TableNode`**: Represents TOML tables and inline tables
- **`ArrayNode`**: Represents TOML arrays
- **`StringNode`**, **`NumberNode`**, **`BooleanNode`**, **`DatetimeNode`**: Primitive value nodes

- For detailed usage examples and guides, see the [Wiki](https://github.com/GroundbreakingMC/TOMLy/wiki).

## Supported TOML Types

TOMLy supports all TOML v1.0.0 data types:

| TOML Type       | Java Type           | Example                                    |
|-----------------|---------------------|--------------------------------------------|
| String          | `String`            | `"hello"`, `'literal'`, `"""multi-line"""` |
| Integer         | `Long`              | `42`, `0x2A`, `0o52`, `0b101010`           |
| Float           | `Double`            | `3.14`, `1e6`, `inf`, `-nan`               |
| Boolean         | `Boolean`           | `true`, `false`                            |
| Local Date      | `LocalDate`         | `2025-08-23`                               |
| Local Time      | `LocalTime`         | `12:34:56.789`                             |
| Local DateTime  | `LocalDateTime`     | `2025-08-23T12:34:56`                      |
| Offset DateTime | `OffsetDateTime`    | `2025-08-23T12:34:56+02:00`                |
| Array           | `List<Node>`        | `[1, "mixed", true]`                       |
| Table           | `Map<String, Node>` | `{ x = 1, y = 2 }`                         |

## Debug Mode vs Production Mode

| Feature                  | Debug Mode (`true`) | Production Mode (`false`) |
|--------------------------|---------------------|---------------------------|
| **Line/Column Tracking** | ✅ Precise tracking  | ✅ Basic tracking          |
| **Memory Usage**         | Higher              | Lower                     |
| **Parsing Speed**        | Slightly slower     | Fastest                   |
| **Error Context**        | Detailed            | Basic                     |

## Performance Tips

1. Disable debug mode in production: Tomly.parse(toml, false, options)
2. Use section navigation: Access doc.getSection("path") once instead of repeated dot-notation lookups
3. Minimal preservation: Only enable preservation options you actually need
4. Stream large files: Use Tomly.parse(Path, ...) instead of loading entire file into memory
5. Efficient writing: Use appropriate maxLineLength for your use case

## Requirements

- Java 17 or higher
- Dependencies:
    - com.github.groundbreakingmc:fission (character source handling)
    - it.unimi.dsi:fastutil (efficient primitive collections)
    - org.jetbrains:annotations (nullability annotations)

## Comparison with Other Libraries

| Feature                      | TOMLy | toml4j | tomlj |
|------------------------------|-------|--------|-------|
| **Performance**              | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐   | ⭐⭐    |
| **Blank Lines Preservation** | ✅     | ❌      | ❌     |
| **Comment Preservation**     | ✅     | ❌      | ❌     |
| **TOML v1.0.0 Support**      | ✅     | ✅      | ✅     |
| **Error Reporting**          | ⭐⭐⭐⭐⭐ | ⭐⭐⭐    | ⭐⭐⭐   |
| **Memory Efficiency**        | ⭐⭐⭐⭐⭐ | ⭐⭐⭐    | ⭐⭐    |
| **API Design**               | ⭐⭐⭐⭐⭐ | ⭐⭐⭐    | ⭐⭐⭐   |

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please ensure all tests pass and code follows existing style conventions.

## Related Projects

- [TOML Specification](https://toml.io/)
- [TOML v1.0.0 Spec](https://toml.io/en/v1.0.0)

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/GroundbreakingMC/TOMLy/issues)
on GitHub.