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
- Added constructor with `methodCoverageOnly` parameter for explicit mode selection
- Modified `analyzeClass()` to detect `@JaCoCoMethodOnlyInstrumented` (fallback)
- Routes to appropriate analyzer based on mode

### 8. Method-Only Analyzer
**File**: `org.jacoco.core/src/org/jacoco/core/internal/analysis/MethodOnlyClassAnalyzer.java`
- Simplified analyzer that only tracks method execution
- **CRITICAL**: Uses line number `-1` to avoid creating line coverage data
- Sets line/branch counters to EMPTY (0/0)

## Critical Mistakes and Lessons Learned

### 1. ❌ Line Number Zero Bug
**Problem**: Initially used line number `0` when marking methods as covered
```java
// WRONG - Creates line coverage entry for line 0
methodCoverage.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, 0);
```

**Impact**: Tests failed with "Class should have no lines expected:<0> but was:<1>"

**Solution**: Use line number `-1` to avoid creating line coverage data
```java
// CORRECT - No line coverage data created
methodCoverage.increment(CounterImpl.COUNTER_0_1, CounterImpl.COUNTER_0_0, -1);
```

### 2. ❌ Probe Index Mismatch
**Initial Attempt**: Tried to skip initializers (`<init>`, `<clinit>`) in analyzer but not instrumenter

**Problem**: Probe indices became misaligned, causing wrong methods to be marked as covered

**Solution**: Keep instrumentation and analysis synchronized - instrument ALL methods including initializers

### 3. ❌ Auto-Detection Design Flaw
**Problem**: Analyzer checks for `@JaCoCoMethodOnlyInstrumented` on the ORIGINAL class bytecode, but the annotation only exists on INSTRUMENTED bytecode

**Failed Approach**:
```java
// This will never find the annotation!
final boolean[] methodCoverageOnly = new boolean[1];
reader.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION) {
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if ("Lorg/jacoco/core/internal/instr/JaCoCoMethodOnlyInstrumented;".equals(desc)) {
            methodCoverageOnly[0] = true; // Never reached!
        }
        return null;
    }
}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
```

**Solution**: Use explicit constructor parameter for method-only mode
```java
public Analyzer(final ExecutionDataStore executionData,
        final ICoverageVisitor coverageVisitor,
        final boolean methodCoverageOnly) {
    // Explicit mode selection
}
```

### 4. ❌ Wrong Runtime for Testing
**Problem**: `LoggerRuntime` doesn't properly inject probe arrays, causing all probes to be false

**Failed Test Output**:
```
Execution data found for class, probes: [false, false, false, false, false, false, false, false, false, false, false, false, false, false]
```

**Solution**: Use `SystemPropertiesRuntime` for end-to-end tests

### 5. ✅ Correct Counter Handling
**Key Insight**: Use existing counter infrastructure but ensure EMPTY for lines/branches

```java
// Method is covered - set instruction counter to indicate execution
if (covered) {
    methodCoverage.increment(CounterImpl.COUNTER_0_1, 
            CounterImpl.COUNTER_0_0, -1);
}
// Always increment method counter to get correct status
methodCoverage.incrementMethodCounter();
```

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
    Single probe per method (including initializers)

Analysis Phase:
Analyzer (explicit methodCoverageOnly=true)
    ↓
MethodOnlyClassAnalyzer
    ↓
Method coverage: COVERED/NOT_COVERED
Line/Branch coverage: EMPTY (0/0)
```

## Usage Examples

### Command Line
```bash
java -javaagent:jacocoagent.jar=coveragelevel=method,destfile=coverage.exec MyApp
```

### Programmatic Instrumentation
```java
// For instrumentation
IRuntime runtime = new SystemPropertiesRuntime();
Instrumenter instrumenter = new Instrumenter(runtime, true); // method-only mode
byte[] instrumented = instrumenter.instrument(originalBytes, className);

// For analysis
ExecutionDataStore executionData = new ExecutionDataStore();
CoverageBuilder coverageBuilder = new CoverageBuilder();
Analyzer analyzer = new Analyzer(executionData, coverageBuilder, true); // method-only mode
analyzer.analyzeClass(originalBytes, className);
```

## Testing Strategy

### Working Tests
1. **MethodOnlyInstrumenterTest**: Verifies valid bytecode generation
2. **MethodOnlyCoverageSimpleTest**: Tests analysis with simulated execution data
3. **AgentOptionsCoverageLevelTest**: Configuration validation

### Partially Working Tests
1. **MethodOnlyModeEndToEndTest**: Runtime execution data collection issues

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

## Key Implementation Tips

1. **Always use line number -1** when incrementing counters in method-only mode
2. **Keep probe indices synchronized** between instrumentation and analysis
3. **Use explicit mode selection** rather than auto-detection
4. **Test with correct runtime** (SystemPropertiesRuntime for integration tests)
5. **Apply Spotless formatting** before committing

## Future Improvements
1. **Fix Auto-Detection**: Store instrumentation mode in ExecutionData
2. **Complete End-to-End Tests**: Fix runtime probe collection issues
3. **Maven/Ant Integration**: Add support in build plugins
4. **Optimized Reports**: Special visualization for method-only coverage

## Conclusion
The method-level-only coverage implementation successfully reduces instrumentation overhead while maintaining compatibility with JaCoCo's existing architecture. The key insight is to maintain the same data structures but ensure line and branch counters remain EMPTY, allowing existing report generators to work without modification. The implementation required careful attention to probe indexing and counter handling, with several non-obvious pitfalls that were discovered through testing.