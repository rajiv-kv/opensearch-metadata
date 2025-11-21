package org.opensearch.lucene.io;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.opensearch.common.Nullable;
import org.opensearch.common.annotation.PublicApi;
import org.opensearch.lucene.util.BigArrays;
import org.opensearch.lucene.util.PageCacheRecycler;
import org.opensearch.core.common.bytes.BytesArray;
import org.opensearch.core.common.bytes.BytesReference;
import org.opensearch.core.common.io.stream.BytesStream;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.common.util.ByteArray;

import java.io.IOException;

@PublicApi(since = "1.0.0")
public class BytesStreamOutput extends BytesStream {

    protected final BigArrays bigArrays;

    @Nullable
    protected ByteArray bytes;
    protected int count;

    public BytesStreamOutput() {
        this(0);
    }

    public BytesStreamOutput(int expectedSize) {
        this(expectedSize, BigArrays.NON_RECYCLING_INSTANCE);
    }

    protected BytesStreamOutput(int expectedSize, BigArrays bigArrays) {
        this.bigArrays = bigArrays;
        if (expectedSize != 0) {
            this.bytes = bigArrays.newByteArray(expectedSize, false);
        }
    }

    @Override
    public long position() {
        return count;
    }

    @Override
    public void writeByte(byte b) {
        ensureCapacity(count + 1L);
        bytes.set(count, b);
        count++;
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) {
        if (length == 0) {
            return;
        }

        if (b.length < (offset + length)) {
            throw new IllegalArgumentException("Illegal offset " + offset + "/length " + length + " for byte[] of length " + b.length);
        }

        ensureCapacity(((long) count) + length);
        bytes.set(count, b, offset, length);
        count += length;
    }

    @Override
    public void reset() {
        // Simplified reset
        count = 0;
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void seek(long position) {
        ensureCapacity(position);
        count = (int) position;
    }

    public void skip(int length) {
        seek(((long) count) + length);
    }

    @Override
    public void close() {
        // empty for now.
    }

    public int size() {
        return count;
    }

    @Override
    public BytesReference bytes() {
        if (bytes == null) {
            return BytesArray.EMPTY;
        }
        return BytesReference.fromByteArray(bytes, count);
    }

    public BytesReference copyBytes() {
        final byte[] keyBytes = new byte[count];
        int offset = 0;
        final BytesRefIterator iterator = bytes().iterator();
        try {
            BytesRef slice;
            while ((slice = iterator.next()) != null) {
                System.arraycopy(slice.bytes, slice.offset, keyBytes, offset, slice.length);
                offset += slice.length;
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return new BytesArray(keyBytes);
    }

    public long ramBytesUsed() {
        return bytes.ramBytesUsed();
    }

    void ensureCapacity(long offset) {
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " cannot hold more than 2GB of data");
        }
        if (bytes == null) {
            this.bytes = bigArrays.newByteArray(Math.max(offset, PageCacheRecycler.PAGE_SIZE_IN_BYTES), false);
        }
        // Simplified - no resize for now
    }
}