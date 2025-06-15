# Method-Level Coverage Implementation for JaCoCo

## Overview
This document describes the implementation of a method-level-only coverage feature for JaCoCo that minimizes runtime overhead for production use by tracking only whether methods were executed, without line or branch coverage details.

## Motivation
- **Goal**: Enable dead code detection in production environments with minimal performance impact
- **Trade-off**: Sacrifice line/branch coverage details for significantly reduced overhead
- **Use Case**: Long-running production applications where traditional coverage tools are too expensive

## Implementation Details

### 1. Configuration Layer
**File**: `org.jacoco.core/src/org/jacoco/core/runtime/AgentOptions.java`
- Added `COVERAGELEVEL` constant and `DEFAULT_COVERAGELEVEL = "full"`
- Added to `VALID_OPTIONS` list
- Implemented `getCoverageLevel()` and `setCoverageLevel()` methods
- Validates values: "full" (default) or "method"

### 2. Agent Integration
**File**: `org.jacoco.agent.rt/src/org/jacoco/agent/rt/internal/CoverageTransformer.java`
- Modified constructor to check `options.getCoverageLevel()`
- Passes `methodCoverageOnly` flag to `Instrumenter`

### 3. Instrumenter Changes
**File**: `org.jacoco.core/src/org/jacoco/core/instr/Instrumenter.java`
- Added `methodCoverageOnly` field
- Added constructor overload accepting coverage level
- Passes flag to `ClassInstrumenter`

### 4. Class Instrumentation
**File**: `org.jacoco.core/src/org/jacoco/core/internal/instr/ClassInstrumenter.java`
- Added `methodCoverageOnly` field
- In `visit()`: Adds `@JaCoCoMethodOnlyInstrumented` annotation when in method-only mode
- In `visitMethod()`: Uses `MethodOnlyProbesAdapter` instead of `MethodInstrumenter`

### 5. Marker Annotation
**File**: `org.jacoco.core/src/org/jacoco/core/internal/instr/JaCoCoMethodOnlyInstrumented.java`
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface JaCoCoMethodOnlyInstrumented {
}
```

### 6. Method-Only Probe Adapter
**File**: `org.jacoco.core/src/org/jacoco/core/internal/instr/MethodOnlyProbesAdapter.java`
- Extends `MethodProbesVisitor`
- Only inserts the first probe encountered (at method entry)
- Ignores all other probe insertion points (branches, switches, etc.)

### 7. Analysis Layer
**File**: `org.jacoco.core/src/org/jacoco/core/analysis/Analyzer.java`
- Modified `analyzeClass()` to detect `@JaCoCoMethodOnlyInstrumented`
- Routes to appropriate analyzer based on annotation presence

### 8. Method-Only Analyzer
**File**: `org.jacoco.core/src/org/jacoco/core/internal/analysis/MethodOnlyClassAnalyzer.java`
- Simplified analyzer that only tracks method execution
- Sets method counter to FULLY_COVERED or NOT_COVERED
- Sets line/branch counters to EMPTY (0/0)

## Architecture Flow

```
Agent Options (coveragelevel=method)
    ↓
CoverageTransformer
    ↓
Instrumenter (methodCoverageOnly=true)
    ↓
ClassInstrumenter
    ├→ Adds @JaCoCoMethodOnlyInstrumented annotation
    └→ Uses MethodOnlyProbesAdapter
         ↓
    Single probe per method

Analysis Phase:
Analyzer detects @JaCoCoMethodOnlyInstrumented
    ↓
MethodOnlyClassAnalyzer
    ↓
Method coverage: COVERED/NOT_COVERED
Line/Branch coverage: EMPTY (0/0)
```

## Usage

### Command Line
```bash
java -javaagent:jacocoagent.jar=coveragelevel=method,destfile=coverage.exec MyApp
```

### Maven Configuration
```xml
<configuration>
    <propertyName>jacocoArgLine</propertyName>
    <append>true</append>
    <coveragelevel>method</coveragelevel>
</configuration>
```

### Programmatic
```java
AgentOptions options = new AgentOptions();
options.setCoverageLevel("method");
options.setDestfile("coverage.exec");
```

## Performance Characteristics

### Runtime Overhead Reduction
1. **Probe Count**: 1 per method vs. multiple per method (branches, lines)
2. **Memory**: Smaller boolean arrays
3. **CPU**: No branch tracking logic
4. **Analysis**: No control flow graph reconstruction

### Trade-offs
- ✅ Minimal runtime overhead
- ✅ Suitable for production use
- ✅ Accurate method-level coverage
- ❌ No line coverage information
- ❌ No branch coverage information
- ❌ No code complexity metrics

## Testing Strategy

### Unit Tests
1. `AgentOptionsCoverageLevelTest` - Configuration validation
2. `InstrumenterTest` - Proper mode propagation
3. `MethodOnlyProbesAdapterTest` - Single probe behavior
4. `MethodOnlyClassAnalyzerTest` - Simplified analysis

### Integration Tests
1. Instrument sample classes with both modes
2. Execute and compare coverage data
3. Verify report generation with EMPTY counters
4. Performance benchmarks comparing modes

## Validation Approach

### Correctness
1. Verify only one probe per method in bytecode
2. Confirm marker annotation presence
3. Validate coverage data accuracy
4. Test with various Java versions

### Performance
1. Measure instrumentation time difference
2. Compare runtime overhead (memory, CPU)
3. Benchmark with large applications
4. Profile probe array access patterns

## Future Enhancements
1. **Selective Method Coverage**: Allow include/exclude patterns at method level
2. **Hybrid Mode**: Full coverage for specific packages, method-only for others
3. **Optimized Storage**: Bit-packed probe arrays for further memory reduction
4. **Report Enhancements**: Special visualization for method-only coverage

## Implementation Status
- ✅ Core implementation complete
- ✅ Agent option added and validated
- ✅ Instrumentation pipeline modified
- ✅ Analysis pipeline modified
- ✅ Unit tests created and passing
- ✅ Integration with existing test suite verified
- ✅ CLAUDE.md documentation updated
- ⏳ User documentation updates needed
- ⏳ Performance benchmarks needed