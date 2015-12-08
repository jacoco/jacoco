/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ivan Hristov - Initial MongoDB connectivity
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import com.mongodb.MongoClient;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;

import java.io.IOException;

public class MongoDbOutput implements IAgentOutput {

    public static final String BUCKET_NAME = "jacoco";
    public static final int CHUNK_SIZE_1MB = 1048576;

    private RuntimeData data;
    private AgentOptions options;

    public void startup(AgentOptions options, RuntimeData data) throws Exception {
        this.options = options;
        this.data = data;
    }

    public void shutdown() throws Exception {

    }

    public void writeExecutionData(boolean reset) throws IOException {
        MongoClientURI mongoClientURI = new MongoClientURI(options.getDestfile());
        MongoClient mongoClient = new MongoClient(mongoClientURI);
        MongoDatabase jacocoDb = mongoClient.getDatabase(mongoClientURI.getDatabase());

        GridFSBucket gridFSBucket = GridFSBuckets.create(jacocoDb, BUCKET_NAME);

        // Create some custom options
        Document metadata = new Document("session_id", options.getSessionId());
        metadata.put("type", "jacoco/exec");
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(CHUNK_SIZE_1MB)
                .metadata(metadata);
        GridFSUploadStream jacocoStream = gridFSBucket.openUploadStream("jacoco.exec", options);
        try {
            final ExecutionDataWriter writer = new ExecutionDataWriter(jacocoStream);
            data.collect(writer, writer, reset);
        } finally {
            jacocoStream.close();
            mongoClient.close();
        }
    }
}
