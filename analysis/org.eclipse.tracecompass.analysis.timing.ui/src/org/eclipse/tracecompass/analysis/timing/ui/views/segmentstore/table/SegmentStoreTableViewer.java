/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Displays the segment store provider data in a column table
 *
 * @author Geneviève Bastien
 * @since 1.2
 */
public class SegmentStoreTableViewer extends AbstractSegmentStoreTableViewer {

    private final String fAnalysisId;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            Table viewer of the view
     * @param analysisId
     *            The ID of the analysis this viewer is for
     */
    public SegmentStoreTableViewer(TableViewer tableViewer, String analysisId) {
        super(tableViewer);
        fAnalysisId = analysisId;
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
        IAnalysisModule module = trace.getAnalysisModule(fAnalysisId);
        if (!(module instanceof ISegmentStoreProvider)) {
            return null;
        }
        return (ISegmentStoreProvider) module;
    }

}
