/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test class to test the {@link AbstractSegmentStatisticsAnalysis} class
 *
 * @author Matthew Khouzam
 */
public class AbstractStatsAnalysisTest {

    /**
     * Test execution with no trace
     *
     * @throws TmfAnalysisException
     *             should not happen
     */
    @Test
    public void testExecuteNoTrace() throws TmfAnalysisException {
        StubSegmentStatisticsAnalysis fixture = new StubSegmentStatisticsAnalysis();
        assertFalse(fixture.executeAnalysis(new NullProgressMonitor()));
        fixture.dispose();
    }

    /**
     * Test execution with no dependent analyses
     *
     * @throws TmfAnalysisException
     *             should not happen
     */
    @Test
    public void testExecuteNoDepend() throws TmfAnalysisException {
        StubSegmentStatisticsAnalysis fixture = new StubSegmentStatisticsAnalysis();
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        fixture.setTrace(trace);
        assertFalse(fixture.executeAnalysis(new NullProgressMonitor()));
        trace.dispose();
        fixture.dispose();
    }

    /**
     * Test good execution
     *
     * @throws TmfAnalysisException
     *             should not happen
     */
    @Test
    public void testExecute() throws TmfAnalysisException {
        StubSegmentStatisticsAnalysis fixture = new StubSegmentStatisticsAnalysis();
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        fixture.setTrace(trace);
        fixture.getDependentAnalyses();
        assertTrue(fixture.executeAnalysis(new NullProgressMonitor()));
        trace.dispose();
        fixture.dispose();
    }

    /**
     * Test total statistics
     *
     * @throws TmfAnalysisException
     *             should not happen
     *
     */
    @Test
    public void testTotalStats() throws TmfAnalysisException {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        StubSegmentStatisticsAnalysis fixture = getValidSegmentStats(trace);
        SegmentStoreStatistics totalStats = fixture.getTotalStats();
        assertNotNull(totalStats);
        // no need to test the content much as it is tested in the other test.
        assertEquals(StubSegmentStatisticsAnalysis.SIZE, totalStats.getNbSegments());
        trace.dispose();
        fixture.dispose();
    }

    /**
     * Test per-type statistics
     *
     * @throws TmfAnalysisException
     *             should not happen
     *
     */
    @Test
    public void testPerTypeStats() throws TmfAnalysisException {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        StubSegmentStatisticsAnalysis fixture = getValidSegmentStats(trace);
        Map<@NonNull String, @NonNull SegmentStoreStatistics> perTypeStats = fixture.getPerSegmentTypeStats();
        assertNotNull(perTypeStats);
        // no need to test the content much as it is tested in the other test.
        assertEquals(2, perTypeStats.size());
        assertEquals(ImmutableSet.<String> of("odd", "even"), perTypeStats.keySet());
        SegmentStoreStatistics segmentStoreStatistics = perTypeStats.get("even");
        assertNotNull(segmentStoreStatistics);
        // starts with 0  so size + 1
        assertEquals(StubSegmentStatisticsAnalysis.SIZE / 2 + 1, segmentStoreStatistics.getNbSegments());
        trace.dispose();
        fixture.dispose();
    }

    /**
     * Test the partial statistics
     *
     * @throws TmfAnalysisException
     *             should not happen
     *
     */
    @Test
    public void testPartialStats() throws TmfAnalysisException {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        StubSegmentStatisticsAnalysis fixture = getValidSegmentStats(trace);
        SegmentStoreStatistics totalStats = fixture.getTotalStatsForRange(100, 1100, new NullProgressMonitor());
        assertNotNull(totalStats);
        // no need to test the content much as it is tested in the other test.

        // 1051 = 1001 where start is between start and end + 50 overlapping
        // start
        assertEquals(1051, totalStats.getNbSegments());
        trace.dispose();
        fixture.dispose();
    }

    /**
     * Test the partial per type statistic
     *
     * @throws TmfAnalysisException
     *             should not happen
     *
     */
    @Test
    public void testPartialPerTypeStats() throws TmfAnalysisException {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        StubSegmentStatisticsAnalysis fixture = getValidSegmentStats(trace);
        Map<@NonNull String, @NonNull SegmentStoreStatistics> perTypeStats = fixture.getPerSegmentTypeStatsForRange(100, 1100, new NullProgressMonitor());
        assertNotNull(perTypeStats);
        // no need to test the content much as it is tested in the other test.
        assertEquals(2, perTypeStats.size());
        assertEquals(ImmutableSet.<String> of("odd", "even"), perTypeStats.keySet());
        SegmentStoreStatistics segmentStoreStatistics = perTypeStats.get("even");
        assertNotNull(segmentStoreStatistics);
        // 526 = 1051/2+1 = see explanation of 1051 in #testPartialStats
        assertEquals(526, segmentStoreStatistics.getNbSegments());
        trace.dispose();
        fixture.dispose();
    }

    /**
     * Test the cancel operation
     *
     * @throws TmfAnalysisException
     *             should not happen
     */
    @Test
    public void testPartialPerTypeStatsCancel() throws TmfAnalysisException {
        TmfXmlTraceStub trace = new TmfXmlTraceStub();
        StubSegmentStatisticsAnalysis fixture = getValidSegmentStats(trace);
        NullProgressMonitor monitor = new NullProgressMonitor();
        monitor.setCanceled(true);
        Map<@NonNull String, @NonNull SegmentStoreStatistics> perTypeStats = fixture.getPerSegmentTypeStatsForRange(100, 1100, monitor);
        assertEquals(Collections.emptyMap(), perTypeStats);
        trace.dispose();
        fixture.dispose();
    }

    private static StubSegmentStatisticsAnalysis getValidSegmentStats(@NonNull ITmfTrace trace) throws TmfAnalysisException {
        StubSegmentStatisticsAnalysis fixture = new StubSegmentStatisticsAnalysis();
        fixture.setTrace(trace);
        fixture.getDependentAnalyses();
        fixture.executeAnalysis(new NullProgressMonitor());
        return fixture;
    }

}
