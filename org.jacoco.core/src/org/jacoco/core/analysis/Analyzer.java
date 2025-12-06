/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.diff.ClassInfoDto;
import org.jacoco.core.internal.diff.CodeDiffUtil;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An {@link Analyzer} instance processes a set of Java class files and
 * calculates coverage data for them. For each class file the result is reported
 * to a given {@link ICoverageVisitor} instance. In addition the
 * {@link Analyzer} requires a {@link ExecutionDataStore} instance that holds
 * the execution data for the classes to analyze. The {@link Analyzer} offers
 * several methods to analyze classes from a variety of sources.
 */
public class Analyzer {

    public ExecutionDataStore getExecutionData() {
        return executionData;
    }

    private final ExecutionDataStore executionData;

    private final ICoverageVisitor coverageVisitor;

    private final StringPool stringPool;

    private List<ClassInfoDto> classInfos;

    /**
     * Creates a new analyzer reporting to the given output.
     *
     * @param executionData   execution data
     * @param coverageVisitor the output instance that will coverage data for every analyzed
     *                        class
     */
    public Analyzer(final ExecutionDataStore executionData,
                    final ICoverageVisitor coverageVisitor) {
        this.executionData = executionData;
        this.coverageVisitor = coverageVisitor;
        this.stringPool = new StringPool();
    }

    /**
     * Creates an ASM class visitor for analysis.
     *
     * @param classid   id of the class calculated with {@link CRC64}
     * @param className VM name of the class
     * @return ASM visitor to write class definition to
     */
    private ClassVisitor createAnalyzingVisitor(final long classid,
                                                final String className, boolean onlyAnaly, ClassReader reader) {
        final ExecutionData data = executionData.get(classid);
        final boolean[] probes;
        final boolean noMatch;
        // 没有在 exec 报告中匹配到，就表示当前覆盖率为空
        if (data == null) {
            int probeCount = ProbeArrayStrategyFactory.getProbeCounter(reader).getCount();
            probes = new boolean[probeCount];
            ExecutionData addEmptyExecutionData = new ExecutionData(classid, className, probes);
            executionData.put(addEmptyExecutionData);
            noMatch = false;
        } else {
            probes = data.getProbes();
            noMatch = false;
        }
        final ClassCoverageImpl coverage = new ClassCoverageImpl(className,
                classid, noMatch);
        final ClassAnalyzer analyzer = new ClassAnalyzer(coverage, probes,
                stringPool, this.classInfos, onlyAnaly) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                // class级别的覆盖率，把instructions的覆盖率写入行SourceNodeImpl的行覆盖率，
                // 在生成报告时候通过指令行的覆盖率来染色
                // 这里有个模板方法模式的钩子方法，这里先定义，等后面类的方法解析完再调用此方法
                coverageVisitor.visitCoverage(coverage);
            }
        };
        return new ClassProbesAdapter(analyzer, false);
    }

    private void analyzeClass(final byte[] source) {
        // source 即为 class 字节码
        final long classId = CRC64.classId(source);
        final ClassReader reader = InstrSupport.classReaderFor(source);
        if ((reader.getAccess() & Opcodes.ACC_MODULE) != 0) {
            return;
        }
        if ((reader.getAccess() & Opcodes.ACC_SYNTHETIC) != 0) {
            return;
        }
        boolean isOnlyAnaly = false;
        if (this.coverageVisitor instanceof CoverageBuilder) {
            this.classInfos = ((CoverageBuilder) this.coverageVisitor).getClassInfos();
            isOnlyAnaly = ((CoverageBuilder) this.coverageVisitor).onlyAnaly;
        }
        // 字段不为空说明是增量覆盖
        if (null != this.classInfos
                && !this.classInfos.isEmpty()) {
            // 如果没有匹配到增量代码就无需解析类
            if (!CodeDiffUtil.checkClassIn(reader.getClassName(), this.classInfos)) {
                return;
            }
        }
        // visitor为ClassProbesAdapter，它的vistor是ClassAnalyzer，同时注册了个visitEnd的钩子
        // visitEnd钩子方法里面实现的是coverageVisitor.visitCoverage(coverage);
        // 所以先走的ClassAnalyzer的方法，在ClassAnalyzer调用visitEnd的时候调用coverageVisitor.visitCoverage(coverage);
        // ClassAnalyzer的CoverageBuilder builder最终分析指令覆盖级别信息，再推理方法更大的级别
        final ClassVisitor visitor = createAnalyzingVisitor(classId,
                reader.getClassName(), isOnlyAnaly, reader);
        // 重点，开始解析类里面的方法，逐个方法遍历
        reader.accept(visitor, 0);

    }

    /**
     * Analyzes the class definition from a given in-memory buffer.
     *
     * @param buffer   class definitions
     * @param location a location description used for exception messages
     * @throws IOException if the class can't be analyzed
     */
    public void analyzeClass(final byte[] buffer, final String location)
            throws IOException {
        try {
            analyzeClass(buffer);
        } catch (final RuntimeException cause) {
            throw analyzerError(location, cause);
        }
    }

    /**
     * Analyzes the class definition from a given input stream. The provided
     * {@link InputStream} is not closed by this method.
     *
     * @param input    stream to read class definition from
     * @param location a location description used for exception messages
     * @throws IOException if the stream can't be read or the class can't be analyzed
     */
    public void analyzeClass(final InputStream input, final String location)
            throws IOException {
        final byte[] buffer;
        try {
            buffer = InputStreams.readFully(input);
        } catch (final IOException e) {
            throw analyzerError(location, e);
        }
        analyzeClass(buffer, location);
    }

    private IOException analyzerError(final String location,
                                      final Exception cause) {
        final IOException ex = new IOException(
                String.format("Error while analyzing %s with JaCoCo %s/%s.",
                        location, JaCoCo.VERSION, JaCoCo.COMMITID_SHORT));
        ex.initCause(cause);
        return ex;
    }

    /**
     * Analyzes all classes found in the given input stream. The input stream
     * may either represent a single class file, a ZIP archive, a Pack200
     * archive or a gzip stream that is searched recursively for class files.
     * All other content types are ignored. The provided {@link InputStream} is
     * not closed by this method.
     *
     * @param input    input data
     * @param location a location description used for exception messages
     * @return number of class files found
     * @throws IOException if the stream can't be read or a class can't be analyzed
     */
    public int analyzeAll(final InputStream input, final String location)
            throws IOException {
        final ContentTypeDetector detector;
        try {
            detector = new ContentTypeDetector(input);
        } catch (final IOException e) {
            throw analyzerError(location, e);
        }
        switch (detector.getType()) {
            // 编译后的类
            case ContentTypeDetector.CLASSFILE:
                analyzeClass(detector.getInputStream(), location);
                return 1;
            case ContentTypeDetector.ZIPFILE:
                return analyzeZip(detector.getInputStream(), location);
            case ContentTypeDetector.GZFILE:
                return analyzeGzip(detector.getInputStream(), location);
            case ContentTypeDetector.PACK200FILE:
                return analyzePack200(detector.getInputStream(), location);
            default:
                return 0;
        }
    }

    /**
     * Analyzes all class files contained in the given file or folder. Class
     * files as well as ZIP files are considered. Folders are searched
     * recursively.
     *
     * @param file file or folder to look for class files
     * @return number of class files found
     * @throws IOException if the file can't be read or a class can't be analyzed
     */
    public int analyzeAll(final File file) throws IOException {
        int count = 0;
        // 如果是文件夹递归找到文件再进行解析
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                count += analyzeAll(f);
            }
        } else {
            final InputStream in = new FileInputStream(file);
            try {
                // 对编译后的class类进行分析即
                count += analyzeAll(in, file.getPath());
            } finally {
                in.close();
            }
        }
        return count;
    }

    /**
     * Analyzes all classes from the given class path. Directories containing
     * class files as well as archive files are considered.
     *
     * @param path    path definition
     * @param basedir optional base directory, if <code>null</code> the current
     *                working directory is used as the base for relative path
     *                entries
     * @return number of class files found
     * @throws IOException if a file can't be read or a class can't be analyzed
     */
    public int analyzeAll(final String path, final File basedir)
            throws IOException {
        int count = 0;
        final StringTokenizer st = new StringTokenizer(path,
                File.pathSeparator);
        while (st.hasMoreTokens()) {
            count += analyzeAll(new File(basedir, st.nextToken()));
        }
        return count;
    }

    private int analyzeZip(final InputStream input, final String location)
            throws IOException {
        final ZipInputStream zip = new ZipInputStream(input);
        ZipEntry entry;
        int count = 0;
        while ((entry = nextEntry(zip, location)) != null) {
            count += analyzeAll(zip, location + "@" + entry.getName());
        }
        return count;
    }

    private ZipEntry nextEntry(final ZipInputStream input,
                               final String location) throws IOException {
        try {
            return input.getNextEntry();
        } catch (final IOException e) {
            throw analyzerError(location, e);
        } catch (final IllegalArgumentException e) {
            // might be thrown in JDK versions below 23 - see
            // https://bugs.openjdk.org/browse/JDK-8321156
            // https://github.com/openjdk/jdk/commit/20c71ceacdcb791f5b70cda456bdc47bdd9acf6c
            throw analyzerError(location, e);
        }
    }

    private int analyzeGzip(final InputStream input, final String location)
            throws IOException {
        GZIPInputStream gzipInputStream;
        try {
            gzipInputStream = new GZIPInputStream(input);
        } catch (final IOException e) {
            throw analyzerError(location, e);
        }
        return analyzeAll(gzipInputStream, location);
    }

    private int analyzePack200(final InputStream input, final String location)
            throws IOException {
        InputStream unpackedInput;
        try {
            unpackedInput = Pack200Streams.unpack(input);
        } catch (final IOException e) {
            throw analyzerError(location, e);
        }
        return analyzeAll(unpackedInput, location);
    }

}
