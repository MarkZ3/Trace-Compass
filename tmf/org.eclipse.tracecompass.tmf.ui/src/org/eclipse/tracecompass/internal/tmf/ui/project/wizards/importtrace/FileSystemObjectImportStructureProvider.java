/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * An import provider that makes use of the IFileSystemObject abstraction
 * instead of using plain file system objects (File, TarEntry, ZipEntry, etc)
 */
@SuppressWarnings("restriction")
public class FileSystemObjectImportStructureProvider implements IImportStructureProvider {

    private IImportStructureProvider fImportProvider;
    private String fArchivePath;

    /**
     * Constructor
     *
     * @param importStructureProvider
     *            the {@link IImportStructureProvider}
     * @param archivePath
     *            the path of the archive file
     */
    public FileSystemObjectImportStructureProvider(IImportStructureProvider importStructureProvider, String archivePath) {
        fImportProvider = importStructureProvider;
        fArchivePath = archivePath;
    }

    /**
     * This orders by number of segments first then by name. So from the
     * shallowest path to the deepest then by lexical order
     */
//    private final class FileObjectPathComparator implements Comparator<IFileSystemObject> {
//        @Override
//        public int compare(IFileSystemObject o1, IFileSystemObject o2) {
//            String absolutePath = o1.getAbsolutePath();
//            IPath path = new Path(absolutePath);
//            String absolutePath2 = o2.getAbsolutePath();
//            IPath path2 = new Path(absolutePath2);
//            int segmentCount = path.segmentCount();
//            if (path.toFile().isDirectory()) {
//                segmentCount++;
//            }
//            int segmentCount2 = path2.segmentCount();
//            if (path2.toFile().isDirectory()) {
//                segmentCount2++;
//            }
//
//            // Order from
//            int compareTo = Integer.compare(segmentCount, segmentCount2);
//            if (compareTo != 0) {
//                return compareTo;
//            }
//
//            int compareTo2 = absolutePath.compareToIgnoreCase(absolutePath2);
//            return compareTo2;
//        }
//    }

    @Override
    public List<IFileSystemObject> getChildren(Object element) {
        @SuppressWarnings("rawtypes")
        List children = fImportProvider.getChildren(((IFileSystemObject) element).getRawFileSystemObject());
        List<IFileSystemObject> adapted = new ArrayList<>(children.size());
        for (Object o : children) {
            IFileSystemObject iFileSystemObject = getIFileSystemObject(o);
            adapted.add(iFileSystemObject);
//            System.out.println(iFileSystemObject.getAbsolutePath());
        }

//        adapted.sort(new FileObjectPathComparator());
        for (IFileSystemObject o : adapted) {
            System.out.println(o.getAbsolutePath());
        }
        System.out.println();
        return adapted;
    }

    /**
     * Get the IFileSystemObject corresponding to the specified raw object
     *
     * @param o
     *            the raw object
     * @return the corresponding IFileSystemObject
     */
    public IFileSystemObject getIFileSystemObject(Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof File) {
            return new FileFileSystemObject((File) o);
        } else if (o instanceof TarEntry) {
            return new TarFileSystemObject((TarEntry) o, fArchivePath);
        } else if (o instanceof ZipEntry) {
            return new ZipFileSystemObject((ZipEntry) o, fArchivePath);
        } else if (o instanceof GzipEntry) {
            return new GzipFileSystemObject((GzipEntry) o, fArchivePath);
        }

        throw new IllegalArgumentException("Object type not handled"); //$NON-NLS-1$
    }

    @Override
    public InputStream getContents(Object fileSystemObject) {
        return fImportProvider.getContents(((IFileSystemObject) fileSystemObject).getRawFileSystemObject());
    }

    @Override
    public String getFullPath(Object element) {
        return fImportProvider.getFullPath(((IFileSystemObject) element).getRawFileSystemObject());
    }

    @Override
    public String getLabel(Object element) {
        return fImportProvider.getLabel(((IFileSystemObject) element).getRawFileSystemObject());
    }

    @Override
    public boolean isFolder(Object element) {
        return fImportProvider.isFolder(((IFileSystemObject) element).getRawFileSystemObject());
    }

    /**
     * Disposes of the resources associated with the provider.
     */
    public void dispose() {
    }
}