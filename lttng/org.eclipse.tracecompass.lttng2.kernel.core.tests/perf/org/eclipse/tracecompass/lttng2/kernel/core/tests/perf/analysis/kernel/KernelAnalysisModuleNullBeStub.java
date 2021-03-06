/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.kernel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;

/**
 * This class is an extension of {@link KernelAnalysisModule} that uses a null
 * backend instead of the default one. This allows to benchmark this analysis
 * without benchmarking the insertions in the state system.
 *
 * @author Geneviève Bastien
 */
public class KernelAnalysisModuleNullBeStub extends KernelAnalysisModule {

    @Override
    protected @NonNull StateSystemBackendType getBackendType() {
        return StateSystemBackendType.NULL;
    }

}
