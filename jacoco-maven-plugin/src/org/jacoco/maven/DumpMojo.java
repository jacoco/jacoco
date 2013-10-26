/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dojcsák Sándor - implementation of DumpMojo, port from ant implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Mojo for dumping execution data (jacoco.exec) into a single file from a remote jacoco agent.
 * 
 * @goal dump
 * @requiresProject true
 * @threadSafe
 */
public class DumpMojo extends AbstractJacocoMojo {

    private static final OutputStream NUL = new OutputStream() {

        @Override
        public void write(final int b) throws IOException {
            // nothing to do
        }
    };

    /**
     * Sets whether execution data should be downloaded from the remote host.
     */
    private final boolean dump = true;

    /**
     * Sets whether a reset command should be sent after the execution data has been copied.
     * 
     * @parameter expression="${jacoco.reset}"
     *            default-value="false"
     */
    private boolean reset = false;

    /**
     * Path to the output file for execution data.
     * 
     * @parameter expression="${jacoco.destFile}"
     *            default-value="${project.build.directory}/jacoco.exec"
     */
    private File destfile;

    /**
     * IP Address or hostname to connect to.
     * 
     * @parameter expression="${jacoco.address}"
     *            default-value="localhost"
     */
    private String address = AgentOptions.DEFAULT_ADDRESS;

    /**
     * Port number to connect to.
     * 
     * @parameter expression="${jacoco.port}"
     *            default-value="6300"
     */
    private int port = AgentOptions.DEFAULT_PORT;

    /**
     * If set to true and the execution data file already exists, coverage data
     * is appended to the existing file. If set to false, an existing execution
     * data file will be replaced.
     * 
     * @parameter expression="${jacoco.append}"
     */
    private boolean append;

    private OutputStream openOutputStream() throws IOException {
        if (dump) {
            getLog().info("Dumping execution data to " + destfile.getAbsolutePath());

            destfile.createNewFile();

            return new FileOutputStream(destfile, append);
        } else {
            return NUL;
        }
    }

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {

        if (port <= 0) {
            throw new MojoExecutionException("Invalid port value: " + port);
        }
        if (dump && destfile == null) {
            throw new MojoExecutionException("Destination file is required when dumping execution data");
        }

        OutputStream output = null;

        try {
            // 1. Open socket connection
            final Socket socket = new Socket(InetAddress.getByName(address), port);
            getLog().info("Connecting to " + socket.getRemoteSocketAddress());
            final RemoteControlWriter remoteWriter = new RemoteControlWriter(socket.getOutputStream());
            final RemoteControlReader remoteReader = new RemoteControlReader(socket.getInputStream());

            // 2. Open file output
            output = openOutputStream();
            final ExecutionDataWriter outputWriter = new ExecutionDataWriter(output);
            remoteReader.setSessionInfoVisitor(outputWriter);
            remoteReader.setExecutionDataVisitor(outputWriter);

            // 3. Request dump
            remoteWriter.visitDumpCommand(dump, reset);
            remoteReader.read();

            socket.close();

        } catch (final IOException e) {
            throw new MojoExecutionException("Unable to dump coverage data from " + address + ":" + port, e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (final IOException e) {
                    getLog().warn("Unable to close coverage data file.");
                }
            }
        }
    }
}
