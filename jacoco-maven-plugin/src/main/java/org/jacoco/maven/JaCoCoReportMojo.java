/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.*;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates reports.
 * 
 * @goal report
 * @requiresProject true
 */
public class JaCoCoReportMojo extends AbstractMojo {

  /**
   * Output directory for the reports.
   * 
   * @parameter default-value="${project.reporting.outputDirectory}/jacoco"
   */
  protected File outputDirectory;

  /**
   * Encoding of the generated reports.
   * 
   * @parameter expression="${project.reporting.outputEncoding}" default-value="UTF-8" 
   */
  protected String outputEncoding;

  /**
   * Encoding of the source files.
   * 
   * @parameter expression="${project.build.sourceEncoding}" default-value="UTF-8"
   */
  protected String sourceEncoding;

  /**
   * File with execution data.
   * 
   * @parameter default-value="${project.build.directory}/jacoco.exec"
   */
  protected File dataFile;

  /**
   * @parameter expression="${project}"
   */
  private MavenProject project;

  private SessionInfoStore sessionInfoStore;
  private ExecutionDataStore executionDataStore;

  public void execute() throws MojoExecutionException, MojoFailureException {
    loadExecutionData();
    try {
      IReportVisitor visitor = createVisitor();
      visitor.visitInfo(sessionInfoStore.getInfos(), executionDataStore.getContents());
      createReport(visitor);
      visitor.visitEnd();
    } catch (Exception e) {
      throw new MojoExecutionException("Error while creating report", e);
    }
  }

  private void loadExecutionData() throws MojoExecutionException {
    sessionInfoStore = new SessionInfoStore();
    executionDataStore = new ExecutionDataStore();
    FileInputStream in = null;
    try {
      in = new FileInputStream(dataFile);
      ExecutionDataReader reader = new ExecutionDataReader(in);
      reader.setSessionInfoVisitor(sessionInfoStore);
      reader.setExecutionDataVisitor(executionDataStore);
      reader.read();
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to read execution data file " + dataFile, e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
    }
  }

  private void createReport(final IReportGroupVisitor visitor) throws IOException {
    final IBundleCoverage bundle = createBundle();
    final SourceFileCollection locator = new SourceFileCollection(getCompileSourceRoots(), sourceEncoding);
    checkForMissingDebugInformation(bundle);
    visitor.visitBundle(bundle, locator);
  }

  private void checkForMissingDebugInformation(ICoverageNode node) {
    if (node.getClassCounter().getTotalCount() > 0 && node.getLineCounter().getTotalCount() == 0) {
      getLog().warn("To enable source code annotation class files have to be compiled with debug information.");
    }
  }

  private IBundleCoverage createBundle() throws IOException {
    final CoverageBuilder builder = new CoverageBuilder();
    final Analyzer analyzer = new Analyzer(executionDataStore, builder);
    File classesDir = new File(project.getBuild().getOutputDirectory());
    analyzer.analyzeAll(classesDir);
    return builder.getBundle(project.getName());
  }

  private IReportVisitor createVisitor() throws IOException {
    List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();

    outputDirectory.mkdirs();

    final XMLFormatter xmlFormatter = new XMLFormatter();
    xmlFormatter.setOutputEncoding(outputEncoding);
    visitors.add(xmlFormatter.createVisitor(new FileOutputStream(new File(outputDirectory, "jacoco.xml"))));

    final CSVFormatter formatter = new CSVFormatter();
    formatter.setOutputEncoding(outputEncoding);
    visitors.add(formatter.createVisitor(new FileOutputStream(new File(outputDirectory, "jacoco.csv"))));

    final HTMLFormatter htmlFormatter = new HTMLFormatter();
    // formatter.setFooterText(footer);
    htmlFormatter.setOutputEncoding(outputEncoding);
    // formatter.setLocale(locale);
    visitors.add(htmlFormatter.createVisitor(new FileMultiReportOutput(outputDirectory)));

    return new MultiReportVisitor(visitors);
  }

  private static class SourceFileCollection implements ISourceFileLocator {

    private List<File> sourceRoots;
    private String encoding;

    public SourceFileCollection(List<File> sourceRoots, String encoding) {
      this.sourceRoots = sourceRoots;
      this.encoding = encoding;
    }

    public Reader getSourceFile(String packageName, String fileName) throws IOException {
      final String r;
      if (packageName.length() > 0) {
        r = packageName + '/' + fileName;
      } else {
        r = fileName;
      }
      for (File sourceRoot : sourceRoots) {
        File file = new File(sourceRoot, r);
        if (file.exists() && file.isFile()) {
          return new InputStreamReader(new FileInputStream(file), encoding);
        }
      }
      return null;
    }

    public int getTabWidth() {
      return 4;
    }
  }

  private File resolvePath(String path) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(project.getBasedir(), path);
    }
    return file;
  }

  private List<File> getCompileSourceRoots() {
    List<File> result = new ArrayList<File>();
    for (Object path : project.getExecutionProject().getCompileSourceRoots()) {
      result.add(resolvePath((String) path));
    }
    return result;
  }

}
