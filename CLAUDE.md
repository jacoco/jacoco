# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building JaCoCo
```bash
# Full build with tests (requires JDK 17)
cd org.jacoco.build
../mvnw clean verify

# Quick build without tests
../mvnw clean verify -DskipTests

# Build specific module
../mvnw clean verify -pl org.jacoco.core -am
```

### Running Tests
```bash
# Run all tests
../mvnw test

# Run tests for specific module
../mvnw test -pl org.jacoco.core.test

# Run a single test class
../mvnw test -Dtest=AnalyzerTest

# Run tests with specific JDK version (requires Maven Toolchains)
../mvnw test -Djdk.version=11
```

### Code Formatting
```bash
# Check code formatting
../mvnw spotless:check

# Apply code formatting
../mvnw spotless:apply
```

## High-Level Architecture

JaCoCo is a Java code coverage library with a two-phase architecture that minimizes runtime overhead:

### Phase 1: Runtime Data Collection
- The JaCoCo agent (`org.jacoco.agent.rt`) instruments bytecode as classes are loaded
- Coverage probes use **lock-free** boolean arrays: `probes[id] = true` is idempotent and thread-safe
- No synchronization needed during normal execution - benign data races are acceptable
- Execution data is written to `.exec` files at JVM shutdown or on demand

### Phase 2: Analysis and Reporting
- The analyzer (`org.jacoco.core`) processes execution data offline
- Original class files are analyzed to rebuild control flow graphs
- Coverage is computed by correlating execution data with class structure using CRC64 checksums
- CRC64 ignores debug info (line numbers, comments) but detects structural changes
- Reports are generated in HTML, XML, or CSV formats

### Core Modules

**org.jacoco.core** - Core functionality
- `Instrumenter`: Modifies bytecode to add coverage probes
- `Analyzer`: Computes coverage from execution data
- `ExecutionDataStore`: Manages runtime coverage data
- `CoverageBuilder`: Creates hierarchical coverage models

**org.jacoco.agent.rt** - Runtime agent
- Implements the Java agent that instruments classes at load time
- Provides multiple output modes: file, TCP server/client, JMX
- Manages runtime data collection with minimal overhead

**org.jacoco.report** - Reporting engine
- Generates HTML, XML, and CSV reports
- Implements coverage check rules and thresholds
- Maps execution data back to source files

**Integration modules**:
- `org.jacoco.ant` - Ant task integration
- `org.jacoco.cli` - Command line interface
- `jacoco-maven-plugin` - Maven plugin integration

### Key Design Patterns

1. **Class Identification**: Uses CRC64 checksums to ensure accurate correlation between instrumented and analyzed classes
2. **Visitor Pattern**: Extensively used for ASM-based bytecode manipulation and report generation
3. **Decoupled Architecture**: Core modules are independent of build tool integrations
4. **Data Flow Separation**: Runtime collection is separate from offline analysis to minimize overhead

### Module Dependencies

The build order reflects module dependencies:
1. `org.jacoco.core` (foundation)
2. `org.jacoco.report` (depends on core)
3. `org.jacoco.agent.rt` (runtime agent, depends on core)
4. `org.jacoco.agent` (packages the runtime agent)
5. Integration modules (depend on core components)

### Working with ASM

JaCoCo uses the ASM bytecode manipulation framework. When modifying instrumentation logic:
- Check the current ASM version in the parent POM
- Use the visitor pattern for bytecode transformations
- Ensure compatibility with the target bytecode version range (Java 5+)

## Performance Characteristics & Design Trade-offs

### Runtime Performance (Excellent)
- **Lock-free probe updates**: Simple `probes[id] = true` with no synchronization
- **Idempotent operations**: Multiple threads can safely write to the same probe
- **Minimal overhead**: Near-zero impact on application performance

### Dump/Reset Operations (Potential Bottleneck)
- Uses **coarse-grained synchronization**: `synchronized (store)` blocks all probe updates
- Reset iterates through all classes calling `Arrays.fill(probes, false)`
- Can cause pauses in applications with thousands of instrumented classes
- TCP server is single-threaded and can be blocked during large dumps

### Memory Considerations
- Each instrumented class maintains a `boolean[]` array for its entire lifetime
- No memory is released until JVM shutdown
- Applications with many classes can have significant memory overhead

### Virtual Thread Compatibility
- **Mostly compatible**: Lock-free probe updates work perfectly with virtual threads
- **Concern**: The `synchronized (store)` block during dumps can pin carrier threads
- **Recommendation**: Minimize dump frequency when using virtual threads

### Potential Performance Issues at Scale
- **False sharing**: Adjacent probes in the `boolean[]` array may cause cache line contention
- With millions of threads hitting adjacent probes, CPU cache coherency overhead can degrade performance
- This is a hardware-level effect that's difficult to diagnose

### Key Implementation Details
- **RuntimeData**: Located in `org.jacoco.core.runtime.RuntimeData`
- **ExecutionDataStore**: Thread-safe operations use synchronized blocks
- **ExecutionData**: Contains the actual `boolean[] probes` array
- **Dump mechanism**: See `RuntimeData.collect()` method for implementation