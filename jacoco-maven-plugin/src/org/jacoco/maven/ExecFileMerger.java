/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mads Mohr Christensen - implementation of MergeMojo
 *    John Oliver - Refactor into separate file
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExecFileMerger {

  private static final String MSG_SKIPPING = "Skipping JaCoCo merge execution due to missing execution data files";

  private final List<FileSet> fileSets;
  private final File destFile;
  private final Log log;

  public ExecFileMerger(List<FileSet> fileSets, File destFile, Log log) {
    this.fileSets = fileSets;
    this.destFile = destFile;
    this.log = log;
  }

  private boolean canMergeReports() {
    if (fileSets == null || fileSets.isEmpty()) {
      log.info(MSG_SKIPPING);
      return false;
    }
    return true;
  }

  public void merge() throws MojoExecutionException {
    if (!canMergeReports()) {
      return;
    }

    final ExecFileLoader loader = new ExecFileLoader();
    load(loader);
    save(loader);
  }

  private void load(final ExecFileLoader loader)
          throws MojoExecutionException {
    final FileSetManager fileSetManager = new FileSetManager(log);
    for (final FileSet fileSet : fileSets) {
      for (final String includedFilename : fileSetManager
              .getIncludedFiles(fileSet)) {
        final File inputFile = new File(fileSet.getDirectory(),
                includedFilename);
        if (inputFile.isDirectory()) {
          continue;
        }
        try {
          log.info(
                  "Loading execution data file "
                          + inputFile.getAbsolutePath());
          loader.load(inputFile);
        } catch (final IOException e) {
          throw new MojoExecutionException("Unable to read "
                  + inputFile.getAbsolutePath(), e);
        }
      }
    }
  }

  private void save(final ExecFileLoader loader)
          throws MojoExecutionException {
    if (loader.getExecutionDataStore().getContents().isEmpty()) {
      log.info(MSG_SKIPPING);
      return;
    }
    log.info(
            "Writing merged execution data to "
                    + destFile.getAbsolutePath());
    try {
      loader.save(destFile, false);
    } catch (final IOException e) {
      throw new MojoExecutionException("Unable to write merged file "
              + destFile.getAbsolutePath(), e);
    }
  }
}
