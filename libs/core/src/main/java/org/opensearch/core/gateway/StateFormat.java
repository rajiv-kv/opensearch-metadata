package org.opensearch.core.gateway;

import java.io.IOException;
import java.nio.file.Path;

public interface StateFormat<T> {
    void write(T obj, Path file) throws IOException;
    T read(Path file) throws IOException;
}