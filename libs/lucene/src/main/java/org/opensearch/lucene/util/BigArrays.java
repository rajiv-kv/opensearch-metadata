package org.opensearch.lucene.util;

import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RamUsageEstimator;
import org.opensearch.common.Nullable;
import org.opensearch.common.annotation.PublicApi;
import org.opensearch.common.lease.Releasable;
import org.opensearch.common.lease.Releasables;
import org.opensearch.common.recycler.Recycler;
import org.opensearch.core.common.breaker.CircuitBreaker;
import org.opensearch.core.common.breaker.CircuitBreakingException;
import org.opensearch.core.common.util.BigArray;
import org.opensearch.core.common.util.ByteArray;
import org.opensearch.core.indices.breaker.CircuitBreakerService;

import java.util.Arrays;

@PublicApi(since = "1.0.0")
public class BigArrays {

    public static final BigArrays NON_RECYCLING_INSTANCE = new BigArrays(null, null, CircuitBreaker.REQUEST);

    public static long overSize(long minTargetSize) {
        return overSize(minTargetSize, PageCacheRecycler.PAGE_SIZE_IN_BYTES / 8, 1);
    }

    public static long overSize(long minTargetSize, int pageSize, int bytesPerElement) {
        if (minTargetSize < 0) {
            throw new IllegalArgumentException("minTargetSize must be >= 0");
        }
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }
        if (bytesPerElement <= 0) {
            throw new IllegalArgumentException("bytesPerElement must be > 0");
        }

        long newSize;
        if (minTargetSize < pageSize) {
            newSize = Math.min(ArrayUtil.oversize((int) minTargetSize, bytesPerElement), pageSize);
        } else {
            final long pages = (minTargetSize + pageSize - 1) / pageSize;
            newSize = pages * pageSize;
        }

        return newSize;
    }

    static boolean indexIsInt(long index) {
        return index == (int) index;
    }

    final PageCacheRecycler recycler;
    private final CircuitBreakerService breakerService;
    private final boolean checkBreaker;
    private final BigArrays circuitBreakingInstance;
    private final String breakerName;

    public BigArrays(PageCacheRecycler recycler, @Nullable final CircuitBreakerService breakerService, String breakerName) {
        this(recycler, breakerService, breakerName, false);
    }

    protected BigArrays(
        PageCacheRecycler recycler,
        @Nullable final CircuitBreakerService breakerService,
        String breakerName,
        boolean checkBreaker
    ) {
        this.checkBreaker = checkBreaker;
        this.recycler = recycler;
        this.breakerService = breakerService;
        this.breakerName = breakerName;
        if (checkBreaker) {
            this.circuitBreakingInstance = this;
        } else {
            this.circuitBreakingInstance = new BigArrays(recycler, breakerService, breakerName, true);
        }
    }

    void adjustBreaker(final long delta, final boolean isDataAlreadyCreated) {
        if (this.breakerService != null) {
            CircuitBreaker breaker = this.breakerService.getBreaker(breakerName);
            if (this.checkBreaker) {
                if (delta > 0) {
                    try {
                        breaker.addEstimateBytesAndMaybeBreak(delta, "<reused_arrays>");
                    } catch (CircuitBreakingException e) {
                        if (isDataAlreadyCreated) {
                            breaker.addWithoutBreaking(delta);
                        }
                        throw e;
                    }
                } else {
                    breaker.addWithoutBreaking(delta);
                }
            } else {
                breaker.addWithoutBreaking(delta);
            }
        }
    }

    public BigArrays withCircuitBreaking() {
        return this.circuitBreakingInstance;
    }

    public CircuitBreakerService breakerService() {
        return this.circuitBreakingInstance.breakerService;
    }

    // Simplified implementation - full implementation would include all array types
    public ByteArray newByteArray(long size, boolean clearOnResize) {
        throw new UnsupportedOperationException("Implementation moved to opensearch-lucene");
    }

    public ByteArray newByteArray(long size) {
        return newByteArray(size, true);
    }
}