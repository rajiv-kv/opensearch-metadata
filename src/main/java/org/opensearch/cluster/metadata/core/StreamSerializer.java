/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.cluster.metadata.core;

import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Interface for stream-based serialization operations
 */
public interface StreamSerializer<T> {
    
    void writeTo(T model, StreamOutput out) throws IOException;
    
    T readFrom(StreamInput in) throws IOException;
}