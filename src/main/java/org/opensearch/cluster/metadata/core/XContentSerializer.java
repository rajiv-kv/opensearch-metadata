/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.cluster.metadata.core;

import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;

import java.io.IOException;

/**
 * Interface for XContent-based serialization operations
 */
public interface XContentSerializer<T> {
    
    void toXContent(T model, XContentBuilder builder, ToXContent.Params params) throws IOException;
    
    T fromXContent(XContentParser parser) throws IOException;
}
