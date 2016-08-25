/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.packageexplorer;

public class TestTraceInfo {
    private final String fTraceName;
    private final String fTracePath;
    private final String fTraceType;
    private final long fNbEvents;
    private final String fFirst;

    public TestTraceInfo(String traceName, String traceType, long nbEvents, String first) {
        this(traceName, traceName, traceType, nbEvents, first);
    }

    public TestTraceInfo(String traceName, String tracePath, String traceType, long nbEvents, String first) {
        fTraceName = traceName;
        fTracePath = tracePath;
        fTraceType = traceType;
        fNbEvents = nbEvents;
        fFirst = first;
    }

    public String getTraceName() {
        return fTraceName;
    }

    public String getTracePath() {
        return fTracePath;
    }

    public String getTraceType() {
        return fTraceType;
    }

    public long getNbEvents() {
        return fNbEvents;
    }

    public String getFirst() {
        return fFirst;
    }
}