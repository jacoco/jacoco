/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Julian Gamble - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.maven.domain.LineContentDiffInfo;
import org.jacoco.maven.domain.LineDiffInfo;
import org.jacoco.maven.util.JGitUtils;
import org.jacoco.report.IReportGroupVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

/**
 * Creates a code coverage report showing uncovered lines in each commit
 *
 */
@Mojo(name = "report-on-commit-coverage", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class ReportOnCommitCoverageMojo extends AbstractReportMojo {

	private String PROJECT_DIR;

	private String GIT_PATH;

	@Parameter(property = "jacoco.daysBackToCheck", defaultValue = "60")
	private int daysBackToCheck;

	private DiffFormatter DIFF_FORMATTER = new DiffFormatter(DisabledOutputStream.INSTANCE);

	private Set<LineContentDiffInfo> coverageDiffs;

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 */
	@Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco")
	private File outputDirectory;

	/**
	 * File with execution data.
	 */
	@Parameter(property = "jacoco.dataFile", defaultValue = "${project.build.directory}/jacoco.exec")
	private File dataFile;

	/**
	 * Target directory.
	 */
	@Parameter(property = "jacoco.targetDirectory", defaultValue = "${project.build.directory}/")
	private File targetDir;

	/**
	 * Report file
	 */
	private FileOutputStream fos;

	@Override
	boolean canGenerateReportRegardingDataFiles() {
		return dataFile.exists();
	}

	@Override
	boolean canGenerateReportRegardingClassesDirectory() {
		return new File(getProject().getBuild().getOutputDirectory()).exists();
	}

	@Override
	void loadExecutionData(final ReportSupport support) throws IOException {
		support.loadExecutionData(dataFile);
	}

	@Override
	void addFormatters(final ReportSupport support, final Locale locale)
			throws IOException {
	}

	@Override
	void createReport(final IReportGroupVisitor visitor,
			final ReportSupport support) throws IOException {
		List<RevCommit> commits = getListOfCommits();
		setupOutputFile();

		Collections.reverse(commits); // show the most recent commit at the end (opposite of git log)
		System.out.println("----");
		for (RevCommit commit:commits) {
			System.out.println("Commit: " + commit.getName() + " - " + commit.getAuthorIdent().getWhen() + " - " + commit.getAuthorIdent().getName() + " - " + commit.getFullMessage());
			Set<LineContentDiffInfo> unionDiffs = getCoverageOnCommitLines(commit);
		}
		fos.close();
	}

	private void setupOutputFile() {
		try {
			File f = new File(outputDirectory,"coverage-per-commit.txt");
			fos = new FileOutputStream(f);
			//we will want to print in standard "System.out" and in "file"
			TeeOutputStream myOut=new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(myOut, true); //true - auto-flush after println
			System.setOut(ps);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	@Override
	public void setReportOutputDirectory(final File reportOutputDirectory) {
		if (reportOutputDirectory != null
				&& !reportOutputDirectory.getAbsolutePath().endsWith("jacoco")) {
			outputDirectory = new File(reportOutputDirectory, "jacoco");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}

	public String getOutputName() {
		return "jacoco/index";
	}

	public String getName(final Locale locale) {
		return "JaCoCo";
	}

	private void loadTargetDir() {

		this.PROJECT_DIR = getProject().getBasedir().getAbsolutePath();
		this.GIT_PATH = PROJECT_DIR + "/.git";
	}

	private List<RevCommit> getListOfCommits() {
		loadTargetDir();

		List<RevCommit> result = new ArrayList<RevCommit>();

		FileRepositoryBuilder builder = new FileRepositoryBuilder();

		Repository repository = null;
		try {
			repository = builder.setGitDir(new File(GIT_PATH))
					.readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.build();

			Date date = getDateNDaysAgo(daysBackToCheck);

			String startingCommit = null;//no starting commits
			result.addAll(JGitUtils.getRevLog(repository, startingCommit, date));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (repository != null) {
				try {
					repository.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		return result;
	}

	public int getDaysBackToCheck() {
		return daysBackToCheck;
	}

	public void setDaysBackToCheck(int daysBackToCheck) {
		this.daysBackToCheck = daysBackToCheck;
	}

	private Date getDateNDaysAgo(int days) {
		long DAY_IN_MS = 1000 * 60 * 60 * 24;
		return new Date(System.currentTimeMillis() - (days * DAY_IN_MS));
	}

	private Set<LineContentDiffInfo> getCoverageOnCommitLines(RevCommit commit) {
		String commitHash = commit.getName();

		Set<LineDiffInfo> UnFilteredCommitDiffs = getCommitLineDiffs(commitHash);

		Set<LineDiffInfo> commitDiffs = filterOutTestsInDiffs(UnFilteredCommitDiffs);

		//note externalised for caching
		if (coverageDiffs == null) {
			coverageDiffs = getCoverageLineDiffs(PROJECT_DIR);
		}

		Set<LineContentDiffInfo> lineDiffsIntersectingWithCoberturaLines = new HashSet(coverageDiffs);
		lineDiffsIntersectingWithCoberturaLines.retainAll(commitDiffs);
		System.out.println("Intersection of line changes with coverage (lines we care about): " + lineDiffsIntersectingWithCoberturaLines.size());

		Set<LineContentDiffInfo> lineDiffsFromCoberturaWithCoverage = getLineDiffsFromCoberturaWithCoverage(lineDiffsIntersectingWithCoberturaLines, true, true);
		System.out.println("covered lines: " + lineDiffsFromCoberturaWithCoverage.size());

		double coverage = ((double)lineDiffsFromCoberturaWithCoverage.size()) / lineDiffsIntersectingWithCoberturaLines.size();
		int coveragePercent = (int) Math.ceil(coverage * 100);

		System.out.println("Coverage for commit: " + coveragePercent + "%");
		Set<LineContentDiffInfo> lineDiffsFromCoberturaWithoutCoverage = getLineDiffsFromCoberturaWithCoverage(lineDiffsIntersectingWithCoberturaLines, false, true);
		System.out.println("Lines not covered: " + lineDiffsFromCoberturaWithoutCoverage.size());

		if (lineDiffsFromCoberturaWithoutCoverage.size() > 0) {
			printLineDiffs(lineDiffsFromCoberturaWithoutCoverage);
		}
		System.out.println("----");

		return lineDiffsIntersectingWithCoberturaLines;
	}

	private Set<LineDiffInfo> getCommitLineDiffs(String lastCommit) {
		Set<LineDiffInfo> result = new HashSet<>();

		Repository repo = getRepository(PROJECT_DIR);

		List<DiffEntry> diffs = getDiffEntries(repo, PROJECT_DIR, lastCommit);

		result = getDiffEntriesToLineDiffInfo(diffs);


		return result;
	}

	private Repository getRepository(String projectDir) {
		Git git = null;
		try {
			git = Git.open(new File(projectDir));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Repository repo = git.getRepository();
		return repo;
	}

	private List<DiffEntry> getDiffEntries(Repository repo, String path, String commit) {
		List<DiffEntry> diffs= new ArrayList<>();

		try {
			repo = new FileRepository(new File(path + "/.git"));
			DepthWalk.RevWalk rw = new DepthWalk.RevWalk(repo, 20);
			RevCommit revCommit = rw.parseCommit(repo.resolve(commit));
			RevCommit parent = null;
			if (revCommit.getParents().length != 0) {
				parent = rw.parseCommit(revCommit.getParent(0).getId());
			}
			DIFF_FORMATTER.setRepository(repo);
			DIFF_FORMATTER.setDiffComparator(RawTextComparator.DEFAULT);
			DIFF_FORMATTER.setDetectRenames(true);

			RevTree parentTree = parent == null ? null : parent.getTree();
			diffs = DIFF_FORMATTER.scan(parentTree, revCommit.getTree());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diffs;
	}

	private Set<LineDiffInfo> getDiffEntriesToLineDiffInfo(List<DiffEntry> diffs) {
		Set<LineDiffInfo> result = new HashSet();

		int linesAdded = 0;
		int linesDeleted = 0;

		for (DiffEntry diff : diffs) {
			try {
				for (Edit edit : DIFF_FORMATTER.toFileHeader(diff).toEditList()) {
					linesDeleted += edit.getEndA() - edit.getBeginA();
					linesAdded += edit.getEndB() - edit.getBeginB();
					for (int i = edit.getBeginB(); i<=edit.getEndB();i++) {
						result.add(new LineDiffInfo(i, diff.getNewPath()));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private  Set<LineDiffInfo> filterOutTestsInDiffs(Set<LineDiffInfo> commitDiffs) {
		Set<LineDiffInfo> result = new HashSet<>(commitDiffs);
		Set<LineDiffInfo> removalSet = new HashSet<>();
		for (LineDiffInfo lineDiffInfo: commitDiffs) {
			if (lineDiffInfo.getFilePackagePathName().contains("Test.java")) {
				removalSet.add(lineDiffInfo);
			}
		}
		result.removeAll(removalSet);
		return result;
	}

	private  Set<LineContentDiffInfo> getCoverageLineDiffs(String projectDir) {
		Set<LineContentDiffInfo> result = new HashSet<LineContentDiffInfo>();

		Collection<File> sourceFiles = getSourceFiles(projectDir);

		result = getLineDiffInfoFromSourceFiles(sourceFiles);

		return result;
	}

	private  Collection<File> getSourceFiles(String projectDir) {
		Collection<File> result = new ArrayList();
		File directory = new File(projectDir + "/src/main/");

		IOFileFilter fileFilter = new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.exists()) {
					return file.getName().contains(".java");
				}
				return false;
			}

			@Override
			public boolean accept(File dir, String name) {
				File file = new File(dir, name);
				if (file.exists()) {
					return file.getName().contains(".java");
				}
				return false;
			}
		};

		Collection<File> files = FileUtils.listFiles(directory, fileFilter, TrueFileFilter.INSTANCE);

		return files;
	}

	private Set<LineContentDiffInfo> getLineDiffInfoFromSourceFiles(Collection<File> sourceFiles) {
		ExecFileLoader execFileLoader = new ExecFileLoader();
		try {
			execFileLoader.load(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);

		File classesDirectory = new File(targetDir, "/classes");

		try {
			analyzer.analyzeAll(classesDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Set<LineContentDiffInfo> result = new HashSet<LineContentDiffInfo>();

		for (final IClassCoverage cc : coverageBuilder.getClasses()) {

			String className = cc.getName();
			String fileName = '/' + className.replace('.', '/') + ".java";
			System.out.println("source file name: " + fileName);
			File sourceFile = new File(PROJECT_DIR, "src/main/java" + fileName);

			Charset charset = Charset.defaultCharset();
			List<String> stringList = null;
			try {
				stringList = Files.readAllLines(sourceFile.toPath(), charset);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] stringArray = stringList.toArray(new String[]{});

			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				int lineNumber = i;
				String codeLine = stringArray[i-1];
				boolean isCovered = cc.getLine(i).getStatus() == ICounter.FULLY_COVERED;
				boolean isCoverageApplicable = cc.getLine(i).getStatus() != 0;
				if (isCoverageApplicable) {
					result.add(new LineContentDiffInfo(lineNumber, "src/main/java" + fileName, codeLine, isCovered, isCoverageApplicable));
				}
			}
		}
		return result;
	}

	private void printLineDiffs(Collection<? extends LineDiffInfo> lineDiffs) {
		for (LineDiffInfo lineDiff:lineDiffs) {
			System.out.println(lineDiff.toLineDifferenceString());
		}
		System.out.println("");
	}

	private Set<LineContentDiffInfo> getLineDiffsFromCoberturaWithCoverage(Set<LineContentDiffInfo> lineDiffsIntersectingWithCoberturaLines, boolean isCoveredVal, boolean isCoverageApplicableVal) {
		Set<LineContentDiffInfo> result = new HashSet<>();

		for (LineContentDiffInfo lineContentDiffInfo: lineDiffsIntersectingWithCoberturaLines) {
			if (lineContentDiffInfo.isCovered() == isCoveredVal && lineContentDiffInfo.isCoverageApplicable() == isCoverageApplicableVal) {
				result.add(lineContentDiffInfo);
			}
		}

		return result;
	}

}
