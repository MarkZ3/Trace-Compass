/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.EventChainSegments;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

import com.google.common.collect.ImmutableList;

/**
 * Class to calculate simple segment store statistics (min, max, average)
 *
 * @author Bernd Hufmann
 */
public class SegmentStoreStatistics {
    private long fMin;
    private long fMax;
    private long fSum;
    private long fNbSegments;
    @Nullable private List<SegmentStoreStatistics> fChildren;

    /**
     * Constructor
     */
    public SegmentStoreStatistics() {
        this.fMin = Long.MAX_VALUE;
        this.fMax = Long.MIN_VALUE;
        this.fSum = 0;
        this.fNbSegments = 0;
    }

    /**
     * Get minimum value
     *
     * @return minimum value
     */
    public long getMin() {
        return fMin;
    }

    /**
     * Get maximum value
     *
     * @return maximum value
     */
    public long getMax() {
        return fMax;
    }

    /**
     * Get number of segments analyzed
     *
     * @return number of segments analyzed
     */
    public long getNbSegments() {
        return fNbSegments;
    }

    /**
     * Gets the arithmetic average
     *
     * @return arithmetic average
     */
    public double getAverage() {
        return ((double) fSum) / fNbSegments;
    }

    /**
     * Gets children SegmentStoreStatistics
     *
     * @return children SegmentStoreStatistics
     */
    public List<SegmentStoreStatistics> getChildren() {
        List<SegmentStoreStatistics> children = fChildren;
        if (children != null) {
            return NonNullUtils.checkNotNull(ImmutableList.copyOf(children));
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Update the statistics based on a given segment
     *
     * @param segment
     *            the segment used for the update
     */
    public void update (ISegment segment) {
        long value = segment.getLength();
        fMin = Math.min(fMin, value);
        fMax = Math.max(fMax, value);
        fSum += value;
        fNbSegments++;

        if (segment instanceof EventChainSegments) {
            List<SegmentStoreStatistics> children = fChildren;
            List<ISegment> subsegments = ((EventChainSegments) segment).getSubSegments();
            if (children == null) {
                children = new ArrayList<>();
                for (int i = 0; i < subsegments.size(); i++) {
                    children.add(new SegmentStoreStatistics());
                }
            }
            for (int i = 0; i < subsegments.size(); i++) {
                children.get(i).update(subsegments.get(i));
            }
            fChildren = children;
        }
    }
}
