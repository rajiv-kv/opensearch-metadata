package org.opensearch.lucene.util;

import org.apache.lucene.util.RamUsageEstimator;
import org.opensearch.common.annotation.ExperimentalApi;
import org.opensearch.common.recycler.AbstractRecyclerC;
import org.opensearch.common.recycler.Recycler;
// Simplified implementation without Settings dependencies
import org.opensearch.core.common.bytes.PagedBytesReference;
import org.opensearch.core.common.unit.ByteSizeValue;

import java.util.Arrays;
import java.util.Locale;

import static org.opensearch.common.recycler.Recyclers.concurrent;
import static org.opensearch.common.recycler.Recyclers.concurrentDeque;
import static org.opensearch.common.recycler.Recyclers.dequeFactory;
import static org.opensearch.common.recycler.Recyclers.none;

@ExperimentalApi
public class PageCacheRecycler {

    // Simplified constants
    public static final int DEFAULT_LIMIT = 1024 * 1024; // 1MB

    public static final int PAGE_SIZE_IN_BYTES = PagedBytesReference.PAGE_SIZE_IN_BYTES;
    public static final int OBJECT_PAGE_SIZE = PAGE_SIZE_IN_BYTES / RamUsageEstimator.NUM_BYTES_OBJECT_REF;
    public static final int LONG_PAGE_SIZE = PAGE_SIZE_IN_BYTES / Long.BYTES;
    public static final int INT_PAGE_SIZE = PAGE_SIZE_IN_BYTES / Integer.BYTES;
    public static final int BYTE_PAGE_SIZE = PAGE_SIZE_IN_BYTES;

    private final Recycler<byte[]> bytePage;
    private final Recycler<int[]> intPage;
    private final Recycler<long[]> longPage;
    private final Recycler<Object[]> objectPage;

    public static final PageCacheRecycler NON_RECYCLING_INSTANCE;

    static {
        NON_RECYCLING_INSTANCE = new PageCacheRecycler();
    }

    public PageCacheRecycler() {
        final Type type = Type.CONCURRENT;
        final long limit = DEFAULT_LIMIT;
        final int allocatedProcessors = Runtime.getRuntime().availableProcessors();

        final int maxPageCount = (int) Math.min(Integer.MAX_VALUE, limit / PAGE_SIZE_IN_BYTES);

        bytePage = build(type, maxPageCount / 4, allocatedProcessors, new AbstractRecyclerC<byte[]>() {
            @Override
            public byte[] newInstance() {
                return new byte[BYTE_PAGE_SIZE];
            }

            @Override
            public void recycle(byte[] value) {
                // nothing to do
            }
        });

        intPage = build(type, maxPageCount / 4, allocatedProcessors, new AbstractRecyclerC<int[]>() {
            @Override
            public int[] newInstance() {
                return new int[INT_PAGE_SIZE];
            }

            @Override
            public void recycle(int[] value) {
                // nothing to do
            }
        });

        longPage = build(type, maxPageCount / 4, allocatedProcessors, new AbstractRecyclerC<long[]>() {
            @Override
            public long[] newInstance() {
                return new long[LONG_PAGE_SIZE];
            }

            @Override
            public void recycle(long[] value) {
                // nothing to do
            }
        });

        objectPage = build(type, maxPageCount / 4, allocatedProcessors, new AbstractRecyclerC<Object[]>() {
            @Override
            public Object[] newInstance() {
                return new Object[OBJECT_PAGE_SIZE];
            }

            @Override
            public void recycle(Object[] value) {
                Arrays.fill(value, null);
            }
        });
    }

    public Recycler.V<byte[]> bytePage(boolean clear) {
        final Recycler.V<byte[]> v = bytePage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), (byte) 0);
        }
        return v;
    }

    public Recycler.V<int[]> intPage(boolean clear) {
        final Recycler.V<int[]> v = intPage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), 0);
        }
        return v;
    }

    public Recycler.V<long[]> longPage(boolean clear) {
        final Recycler.V<long[]> v = longPage.obtain();
        if (v.isRecycled() && clear) {
            Arrays.fill(v.v(), 0L);
        }
        return v;
    }

    public Recycler.V<Object[]> objectPage() {
        return objectPage.obtain();
    }

    private static <T> Recycler<T> build(Type type, int limit, int availableProcessors, Recycler.C<T> c) {
        final Recycler<T> recycler;
        if (limit == 0) {
            recycler = none(c);
        } else {
            recycler = type.build(c, limit, availableProcessors);
        }
        return recycler;
    }

    public enum Type {
        QUEUE {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int availableProcessors) {
                return concurrentDeque(c, limit);
            }
        },
        CONCURRENT {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int availableProcessors) {
                return concurrent(dequeFactory(c, limit / availableProcessors), availableProcessors);
            }
        },
        NONE {
            @Override
            <T> Recycler<T> build(Recycler.C<T> c, int limit, int availableProcessors) {
                return none(c);
            }
        };

        public static Type parse(String type) {
            try {
                return Type.valueOf(type.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("no type support [" + type + "]");
            }
        }

        abstract <T> Recycler<T> build(Recycler.C<T> c, int limit, int availableProcessors);
    }
}