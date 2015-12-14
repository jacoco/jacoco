/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ivan Hristov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.data.ExecutionDataWriter;

/**
 * Output that connects to an HTTP port. This controller uses the following
 * agent options:
 * <ul>
 * <li>address</li>
 * <li>port</li>
 * <li>destfile</li>
 * <li>sessionid</li>
 * </ul>
 */
public class HttpClientOutput implements IAgentOutput {
    public static final int CHUNK_SIZE_IN_BYTES = 1024;
    private RuntimeData data;
    private HttpURLConnection connection;

    public void startup(AgentOptions options, RuntimeData data) throws Exception {
        this.data = data;

        URL url = new URL("http", options.getAddress(), options.getPort(), "/" + options.getDestfile() + "/" + options.getSessionId());
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setUseCaches(false);
        connection.setChunkedStreamingMode(CHUNK_SIZE_IN_BYTES);

    }

    public void shutdown() throws Exception {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public void writeExecutionData(boolean reset) throws IOException {
        OutputStream outputStream = connection.getOutputStream();
        try {
            ExecutionDataWriter writer = new ExecutionDataWriter(outputStream);
            data.collect(writer, writer, reset);
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }
}
