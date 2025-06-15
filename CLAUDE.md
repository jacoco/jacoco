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
- Coverage probes track execution using boolean arrays with minimal performance impact
- Execution data is written to `.exec` files at JVM shutdown or on demand

### Phase 2: Analysis and Reporting
- The analyzer (`org.jacoco.core`) processes execution data offline
- Original class files are analyzed to rebuild control flow graphs
- Coverage is computed by correlating execution data with class structure
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