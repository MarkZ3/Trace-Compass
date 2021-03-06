/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils.QuarkIterator;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterators;

/**
 * Test the {@link StateSystemUtils} class
 *
 * @author Geneviève Bastien
 */
public class StateSystemUtilsTest {

    private static final long START_TIME = 1000L;
    private static final @NonNull String DUMMY_STRING = "test";

    private ITmfStateSystemBuilder fStateSystem;

    /**
     * Build a small test state system in memory
     */
    @Before
    public void setupStateSystem() {
        try {
            IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(DUMMY_STRING, START_TIME);
            fStateSystem = StateSystemFactory.newStateSystem(backend);
            int quark = fStateSystem.getQuarkAbsoluteAndAdd(DUMMY_STRING);

            fStateSystem.modifyAttribute(1200L, TmfStateValue.newValueInt(10), quark);
            fStateSystem.modifyAttribute(1500L, TmfStateValue.newValueInt(20), quark);
            fStateSystem.closeHistory(2000L);
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Clean-up
     */
    @After
    public void tearDown() {
        fStateSystem.dispose();
    }

    /**
     * Test the {@link StateSystemUtils#queryUntilNonNullValue} method.
     */
    @Test
    public void testQueryUntilNonNullValue() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        int quark;
        try {
            quark = ss.getQuarkAbsolute(DUMMY_STRING);

            /* Should return null if requested range is not within range */
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 0, 999L));
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 2001L, 5000L));

            /*
             * Should return null if request within range, but condition is
             * false
             */
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 1000L, 1199L));

            /*
             * Should return the right interval if an interval is within range,
             * even if the range starts or ends outside state system range
             */
            ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1000L, 1300L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(10, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 800L, 2500L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(10, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1300L, 1800L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(10, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1500L, 1800L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(20, interval.getStateValue().unboxInt());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1800L, 2500L);
            assertNotNull(interval);
            assertEquals(ITmfStateValue.Type.INTEGER, interval.getStateValue().getType());
            assertEquals(20, interval.getStateValue().unboxInt());

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }

    }

    /**
     * Test that iterator returns the correct intervals:
     * <ul>
     * <li>intervals for the correct quark</li>
     * <li>ordered intervals</li>
     * <li>intervals covering the correct time range</li>
     * </ul>
     */
    @Test
    public void testIteratorOverQuark() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        for (int quark = 0; quark < ss.getNbAttributes(); quark++) {
            QuarkIterator iterator = new QuarkIterator(ss, quark, Long.MIN_VALUE);
            ITmfStateInterval prevInterval = null;
            ITmfStateInterval currInterval = null;

            while (iterator.hasNext()) {
                currInterval = iterator.next();

                assertEquals(quark, currInterval.getAttribute());
                if (prevInterval == null) {
                    /* This is the first interval for this attribute */
                    assertEquals(currInterval.getStartTime(), ss.getStartTime());
                } else {
                    assertEquals(prevInterval.getEndTime() + 1, currInterval.getStartTime());
                }

                prevInterval = currInterval;
            }

            assertNotNull("Iterator should have returned at least one interval", currInterval);
            assertEquals(ss.getCurrentEndTime(), currInterval.getEndTime());
        }
    }

    /**
     * Test that getIteratorOverQuark returns the correct intervals for a range
     * included in the state system range
     */
    @Test
    public void testIteratorOverQuarkSubrange() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        int quark;
        try {
            quark = ss.getQuarkAbsolute(DUMMY_STRING);

            QuarkIterator iterator = new QuarkIterator(ss, quark, 1800L);

            /* There should be one interval ranging from 1500L to 2000L */
            assertTrue(iterator.hasNext());
            ITmfStateInterval interval = iterator.next();
            assertNotNull(interval);
            assertEquals(1500L, interval.getStartTime());
            assertEquals(2000L, interval.getEndTime());

            /* There should not be a next interval */
            assertFalse(iterator.hasNext());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * With the query end > ss.end, query some intervals, then add a few
     * intervals to the ss such that end now becomes < ss.end and make sure
     * those new intervals are picked up
     */
    @Test
    public void testIteratorOverQuarkAddMoreIntervals() {
        try {
            IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(DUMMY_STRING, START_TIME);
            ITmfStateSystemBuilder ss = StateSystemFactory.newStateSystem(backend);
            int quark = ss.getQuarkAbsoluteAndAdd(DUMMY_STRING);

            ss.modifyAttribute(1200L, TmfStateValue.newValueInt(10), quark);
            ss.modifyAttribute(1500L, TmfStateValue.newValueInt(20), quark);

            /* We should have 2 intervals if we iterate at this point */
            QuarkIterator iterator = new QuarkIterator(ss, quark, 0);
            assertEquals(2, Iterators.size(iterator));

            /* Reset the iterator */
            iterator = new QuarkIterator(ss, quark, 0);

            /* Add an interval, updating the ss.end time */
            ss.closeHistory(2000L);

            /* We should have 3 intervals if we iterate at this point */
            assertEquals(3, Iterators.size(iterator));
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test that the reverse iterator returns the correct intervals:
     * <ul>
     * <li>intervals for the correct quark</li>
     * <li>ordered intervals</li>
     * <li>intervals covering the correct time range</li>
     * </ul>
     */
    @Test
    public void testIteratorOverQuarkReversed() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        for (int quark = 0; quark < ss.getNbAttributes(); quark++) {
            QuarkIterator iterator = new QuarkIterator(ss, quark, Long.MAX_VALUE);
            ITmfStateInterval prevInterval = null;
            ITmfStateInterval currInterval = null;

            while (iterator.hasPrevious()) {
                currInterval = iterator.previous();

                assertEquals(quark, currInterval.getAttribute());
                if (prevInterval == null) {
                    /* This is the first interval for this attribute */
                    assertEquals(currInterval.getEndTime(), ss.getCurrentEndTime());
                } else {
                    assertEquals(prevInterval.getStartTime() - 1, currInterval.getEndTime());
                }

                prevInterval = currInterval;
            }

            assertNotNull("Iterator should have returned at least one interval", currInterval);
            assertEquals(ss.getStartTime(), currInterval.getStartTime());
        }
    }
}
