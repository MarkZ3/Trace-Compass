/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.packageexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConfirmation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests.SWTBotImportWizardUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.ui.IPageLayout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * SWTBot test for testing Project Explorer Trace actions (context-menus,
 * keyboard)
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({"restriction", "javadoc"})
public class ProjectExplorerTracesFolderTest {

    private static final String LAST_MODIFIED_PROPERTY = "last modified";
    private static final String TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";

    private static @NonNull TestTraceInfo CUSTOM_TEXT_LOG = new TestTraceInfo("ExampleCustomTxt.log", "Custom Text : TmfGeneric", 10, "29:52.034");
    private static @NonNull TestTraceInfo CUSTOM_XML_LOG = new TestTraceInfo("ExampleCustomXml.xml", "Custom XML : Custom XML Log", 6, "22:01:20");
    private static @NonNull TestTraceInfo LTTNG_KERNEL_TRACE = new TestTraceInfo("kernel-overlap-testing", "Common Trace Format : Linux Kernel Trace", 1000, "04:32.650 993 664");
    private static @NonNull TestTraceInfo SIMPLE_SERVER1_UST_TRACE = new TestTraceInfo("simple_server-thread1", "Common Trace Format : LTTng UST Trace", 1000, "04:32.650 993 664");
    private static @NonNull TestTraceInfo SIMPLE_SERVER2_UST_TRACE = new TestTraceInfo("simple_server-thread2", "Common Trace Format : LTTng UST Trace", 1000, "04:32.650 993 664");
    private static @NonNull TestTraceInfo UST_OVERLAP_TESTING_UST_TRACE = new TestTraceInfo("ust-overlap-testing", "Common Trace Format : LTTng UST Trace", 1000, "04:32.650 993 664");


    private static @NonNull TestTraceInfo CLASHES_CUSTOM_TEXT_LOG = new TestTraceInfo("ExampleCustomTxt.log", "clashes/ExampleCustomTxt.log", "Custom Text : TmfGeneric", 10, "29:52.034");
    private static @NonNull TestTraceInfo CLASHES_CUSTOM_XML_LOG = new TestTraceInfo("ExampleCustomXml.xml", "clashes/ExampleCustomXml.xml", "Custom XML : Custom XML Log", 6, "22:01:20");
    private static @NonNull TestTraceInfo CLASHES_LTTNG_KERNEL_TRACE = new TestTraceInfo("kernel-overlap-testing", "clashes/kernel-overlap-testing", "Common Trace Format : Linux Kernel Trace", 1000, "04:32.650 993 664");
    private static @NonNull TestTraceInfo CLASHES_SIMPLE_SERVER1_UST_TRACE = new TestTraceInfo("simple_server-thread1", "clashes/simple_server-thread1", "Common Trace Format : LTTng UST Trace", 1000, "04:32.650 993 664");
    private static @NonNull TestTraceInfo CLASHES_SIMPLE_SERVER2_UST_TRACE = new TestTraceInfo("simple_server-thread2", "clashes/simple_server-thread2", "Common Trace Format : LTTng UST Trace", 1000, "04:32.650 993 664");
    private static @NonNull TestTraceInfo CLASHES_UST_OVERLAP_TESTING_UST_TRACE = new TestTraceInfo("ust-overlap-testing", "clashes/ust-overlap-testing", "Common Trace Format : LTTng UST Trace", 1000, "04:32.650 993 664");


    private static @NonNull TestTraceInfo LTTNG_KERNEL_TRACE_METADATA = new TestTraceInfo(LTTNG_KERNEL_TRACE.getTraceName(), LTTNG_KERNEL_TRACE.getTraceName() + "/metadata", LTTNG_KERNEL_TRACE.getTraceType(), LTTNG_KERNEL_TRACE.getNbEvents(),
            LTTNG_KERNEL_TRACE.getFirst());


    private static TestTraceInfo UNRECOGNIZED_LOG = new TestTraceInfo("unrecognized.log", "", 0, "");

    private static TestTraceInfo[] ALL_TRACEINFOS = new TestTraceInfo[] {
            CUSTOM_TEXT_LOG,
            CUSTOM_XML_LOG,
            LTTNG_KERNEL_TRACE,
            SIMPLE_SERVER1_UST_TRACE,
            SIMPLE_SERVER2_UST_TRACE,
            UST_OVERLAP_TESTING_UST_TRACE,

            CLASHES_CUSTOM_TEXT_LOG,
            CLASHES_CUSTOM_XML_LOG,
            CLASHES_LTTNG_KERNEL_TRACE,
            CLASHES_SIMPLE_SERVER1_UST_TRACE,
            CLASHES_SIMPLE_SERVER2_UST_TRACE,
            CLASHES_UST_OVERLAP_TESTING_UST_TRACE
    };

    private Set<TestTraceInfo> CLASHING_TRACEINFOS = ImmutableSet.of(
            CLASHES_CUSTOM_TEXT_LOG,
            CLASHES_CUSTOM_XML_LOG,
            CLASHES_LTTNG_KERNEL_TRACE,
            CLASHES_SIMPLE_SERVER1_UST_TRACE,
            CLASHES_SIMPLE_SERVER2_UST_TRACE,
            CLASHES_UST_OVERLAP_TESTING_UST_TRACE);


    private static final File TEST_TRACES_PATH = new File(new Path(TmfTraceManager.getTemporaryDirPath()).append("testtraces").toOSString());
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String MANAGE_CUSTOM_PARSERS_SHELL_TITLE = "Manage Custom Parsers";

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static String getPath(String relativePath) {
        return new Path(TEST_TRACES_PATH.getAbsolutePath()).append(relativePath).toOSString();
    }

    /**
     * Test Class setup
     * @throws IOException
     */
    @BeforeClass
    public static void init() throws IOException {
        TestDirectoryStructureUtil.generateTraceStructure(TEST_TRACES_PATH);

        SWTBotUtils.initialize();

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("Welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();

        /* Finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    private static void test3_01Preparation() {

        // FIXME: We can't use Manage Custom Parsers > Import because it uses a native dialog. We'll still check that they show up in the dialog
        CustomTxtTraceDefinition[] txtDefinitions = CustomTxtTraceDefinition.loadAll(getPath("customParsers/ExampleCustomTxtParser.xml"));
        txtDefinitions[0].save();
        CustomXmlTraceDefinition[] xmlDefinitions = CustomXmlTraceDefinition.loadAll(getPath("customParsers/ExampleCustomXmlParser.xml"));
        xmlDefinitions[0].save();

        SWTBotTreeItem traceFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        traceFolder.contextMenu("Manage Custom Parsers...").click();
        fBot.waitUntil(Conditions.shellIsActive(MANAGE_CUSTOM_PARSERS_SHELL_TITLE));
        SWTBotShell shell = fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE);
        SWTBot shellBot = shell.bot();

        // Make sure the custom text trace type is imported
        shellBot.list().select(CUSTOM_TEXT_LOG.getTraceType());

        // Make sure the custom xml trace type is imported
        shellBot.radio("XML").click();
        shellBot.list().select(CUSTOM_XML_LOG.getTraceType());
        shellBot.button("Close").click();
        shellBot.waitUntil(Conditions.shellCloses(shell));

    }

    /**
     * Test that the expected context menu items are there
     * <p>
     * Action : Trace Folder menu
     * <p>
     * Procedure :Select the Traces folder and open its context menu
     * <p>
     * Expected Results: Correct menu opens (Import, Refresh, etc)
     *
     */
    @Test
    public void test3_01ContextMenuPresence() {
        test3_01Preparation();

        SWTBotTreeItem traceItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);

        final List<String> EXPECTED_MENU_LABELS = ImmutableList.of(
                "Open Trace...",
                "",
                "Import...",
                "",
                "New Folder...",
                "Clear",
                "",
                "Import Trace Package...",
                "Fetch Remote Traces...",
                "",
                "Export Trace Package...",
                "",
                "Manage Custom Parsers...",
                "Manage XML analyses...",
                "",
                "Apply Time Offset...",
                "Clear Time Offset",
                "",
                "Refresh");

        List<String> menuLabels = traceItem.contextMenu().menuItems();
        assertEquals(EXPECTED_MENU_LABELS, menuLabels);

        fBot.closeAllEditors();
    }

    /**
     * Test that the trace import wizard appears
     * <p>
     * Action : Trace Import Wizard
     * <p>
     * Procedure : Select Import
     * <p>
     * Expected Results: Trace Import Wizard appears
     *
     */
    @Test
    public void test3_02Import() {
        SWTBotTreeItem traceItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);

        SWTBotShell shell = openTraceFoldersImport(traceItem);
        shell.bot().button("Cancel").click();
    }

    /**
     * Test that the trace import wizard can import a single custom text trace
     * <p>
     * Action : Import single custom text trace (link to workspace)
     * <p>
     * <pre>
     * Procedure : 1) Browse to directory ${local}/traces/import/
     *             2) Select trace ExampleCustomTxt.log
     *             3) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning" and select "Create Links to workspace" and
     *             4) press Finish
     * </pre>
     * <p>
     * Expected Results: Trace Import Wizard appears
     *
     */
    @Test
    public void test3_03SingleCustomTextTrace() {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(CUSTOM_TEXT_LOG, optionFlags);
    }

    @Test
    public void test3_04SingleCustomXmlTrace() {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(CUSTOM_XML_LOG, optionFlags);
    }

    @Test
    public void test3_05SingleCtfTrace() {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(LTTNG_KERNEL_TRACE, optionFlags);
    }

    @Test
    public void test3_06RenameCopyImport() {
        testRenameCopyImport(CUSTOM_TEXT_LOG);
        testRenameCopyImport(CUSTOM_XML_LOG);
        testRenameCopyImport(LTTNG_KERNEL_TRACE);
    }

    private static void testRenameCopyImport(TestTraceInfo traceInfo) {
        importTrace(traceInfo.getTraceName(), ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES, ImportConfirmation.RENAME);
        String renamed = toTwo(traceInfo.getTraceName());
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), renamed);
        checkTraceType(traceItem, traceInfo.getTraceType());
        openTrace(traceItem);
        testEventsTable(renamed, traceInfo.getNbEvents(), traceInfo.getFirst());
        checkTraceLinked(traceItem, false);
    }

    @Test
    public void test3_07OverwriteCopyImport() {
        testOverwriteCopyImport(CUSTOM_TEXT_LOG);
        testOverwriteCopyImport(CUSTOM_XML_LOG);
        testOverwriteCopyImport(LTTNG_KERNEL_TRACE);
    }

    private static void testOverwriteCopyImport(TestTraceInfo traceInfo) {
        String traceName = traceInfo.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        String lastModified = getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY);
        importTrace(traceName, ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES, ImportConfirmation.OVERWRITE);
        traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        checkTraceType(traceItem, traceInfo.getTraceType());
        openTrace(traceItem);
        testEventsTable(traceName, traceInfo.getNbEvents(), traceInfo.getFirst());
        checkTraceLinked(traceItem, false);
        assertNotEquals(lastModified, getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY));
    }

    @Test
    public void test3_08SkipImport() {
        testSkipImport(CUSTOM_TEXT_LOG);
        testSkipImport(CUSTOM_XML_LOG);
        testSkipImport(LTTNG_KERNEL_TRACE);
    }

    private static void testSkipImport(TestTraceInfo traceInfo) {
        String traceName = traceInfo.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        String lastModified = getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY);
        importTrace(traceName, ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES, ImportConfirmation.SKIP);
        traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        checkTraceType(traceItem, traceInfo.getTraceType());
        openTrace(traceItem);
        testEventsTable(traceName, traceInfo.getNbEvents(), traceInfo.getFirst());
        checkTraceLinked(traceItem, false);
        assertEquals(lastModified, getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY));
    }

    @Test
    public void test3_09OverwriteOptionImport() {
        testOverwriteOptionImport(CUSTOM_TEXT_LOG);
        testOverwriteOptionImport(CUSTOM_XML_LOG);
        testOverwriteOptionImport(LTTNG_KERNEL_TRACE);
    }

    private static void testOverwriteOptionImport(TestTraceInfo traceInfo) {
        String traceName = traceInfo.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        String lastModified = getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY);
        importTrace(traceName, ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, ImportConfirmation.CONTINUE);
        traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        checkTraceType(traceItem, traceInfo.getTraceType());
        openTrace(traceItem);
        testEventsTable(traceName, traceInfo.getNbEvents(), traceInfo.getFirst());
        checkTraceLinked(traceItem, false);
        assertNotEquals(lastModified, getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY));
    }

    @Test
    public void test3_10ImportUnrecognized() {
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        importTrace(traceName, ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        checkTraceType(traceItem, UNRECOGNIZED_LOG.getTraceType());
        checkTraceLinked(traceItem, true);
        openTrace(traceItem);
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, traceName));
        SWTBotEditor editor = fBot.editorByTitle(traceName);
        assertEquals(TEXT_EDITOR_ID, editor.getReference().getId());
    }

    @Test
    public void test3_11ImportUnrecognizedIgnore() {
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        int numTraces = tracesFolderItem.getItems().length;

        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
        String lastModified = getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY);
        importTrace(traceName, ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE);
        traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);

        assertEquals(lastModified, getTraceProperty(traceItem, LAST_MODIFIED_PROPERTY));
        assertEquals(numTraces, tracesFolderItem.getItems().length);
    }

    @Test
    public void test3_12ImportCtfWithMetadataSelection() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(LTTNG_KERNEL_TRACE_METADATA, optionFlags);
    }

    @Test
    public void test3_13ImportRecursiveAutoRenameAll() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace("", optionFlags, ImportConfirmation.RENAME_ALL);

        for (TestTraceInfo info : ALL_TRACEINFOS) {
            String traceName = info.getTraceName();
            if (CLASHING_TRACEINFOS.contains(info)) {
                traceName = toTwo(traceName);
            }
            SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
            checkTraceType(traceItem, info.getTraceType());
            openTrace(traceItem);
            testEventsTable(traceName, info.getNbEvents(), info.getFirst());
        }

        // Also check unrecognized file
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        checkTraceType(traceItem, UNRECOGNIZED_LOG.getTraceType());
        openTrace(traceItem);
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, traceName));
        SWTBotEditor editor = fBot.editorByTitle(traceName);
        assertEquals(TEXT_EDITOR_ID, editor.getReference().getId());
    }

    @Test
    public void test3_14ImportRecursiveAutoOverwriteAll() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace("", optionFlags, ImportConfirmation.OVERWRITE_ALL);

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        for (TestTraceInfo info : CLASHING_TRACEINFOS) {
            String traceName = info.getTraceName();

            SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
            checkTraceType(traceItem, info.getTraceType());
            openTrace(traceItem);
            testEventsTable(traceName, info.getNbEvents(), info.getFirst());
        }

        // All traces should have clashed/overwritten plus the unrecognized trace
        assertEquals(CLASHING_TRACEINFOS.size() + 1, tracesFolderItem.getItems().length);

        // Also check unrecognized file
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
        checkTraceType(traceItem, UNRECOGNIZED_LOG.getTraceType());
        openTrace(traceItem);
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, traceName));
        SWTBotEditor editor = fBot.editorByTitle(traceName);
        assertEquals(TEXT_EDITOR_ID, editor.getReference().getId());
    }

    /**
     * TODO: Need a way to differentiate skip and overwrite results
     */
    @Test
    public void test3_15ImportRecursiveAutoSkipAll() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace("", optionFlags, ImportConfirmation.SKIP_ALL);

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        for (TestTraceInfo info : CLASHING_TRACEINFOS) {
            String traceName = info.getTraceName();

            SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
            checkTraceType(traceItem, info.getTraceType());
            openTrace(traceItem);
            testEventsTable(traceName, info.getNbEvents(), info.getFirst());
        }

        // All traces should have clashed/overwritten plus the unrecognized trace
        assertEquals(CLASHING_TRACEINFOS.size() + 1, tracesFolderItem.getItems().length);

        // Also check unrecognized file
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
        checkTraceType(traceItem, UNRECOGNIZED_LOG.getTraceType());
        openTrace(traceItem);
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, traceName));
        SWTBotEditor editor = fBot.editorByTitle(traceName);
        assertEquals(TEXT_EDITOR_ID, editor.getReference().getId());
    }

    /**
     * TODO: Need a way to differentiate skip and overwrite results
     */
//    @Test
//    public void test3_16ImportRecursiveAutoRenameOverwriteSkip() {
//        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);
//
//        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
//        importTrace("", optionFlags, ImportConfirmation.SKIP_ALL);
//
//        ImportConfirmation dialogConfirmationOrder[] = new ImportConfirmation[] { ImportConfirmation.RENAME, ImportConfirmation.OVERWRITE, ImportConfirmation.SKIP };
//
//        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
//        for (TestTraceInfo info : CLASHING_TRACEINFOS) {
//            String traceName = info.getTraceName();
//
//            SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
//            checkTraceType(traceItem, info.getTraceType());
//            openTrace(traceItem);
//            testEventsTable(traceName, info.getNbEvents(), info.getFirst());
//        }
//
//        // All traces should have clashed/overwritten plus the unrecognized trace
//        assertEquals(CLASHING_TRACEINFOS.size() + 1, tracesFolderItem.getItems().length);
//
//        // Also check unrecognized file
//        String traceName = UNRECOGNIZED_LOG.getTraceName();
//        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
//        checkTraceType(traceItem, UNRECOGNIZED_LOG.getTraceType());
//        openTrace(traceItem);
//        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, traceName));
//        SWTBotEditor editor = fBot.editorByTitle(traceName);
//        assertEquals(TEXT_EDITOR_ID, editor.getReference().getId());
//    }

    private static String toTwo(String traceName) {
        return traceName + "(2)";
    }

    private static void openTrace(SWTBotTreeItem traceItem) {
        traceItem.select();
        traceItem.doubleClick();
    }

    private static void testSingleTrace(TestTraceInfo traceInfo, int optionFlags) {
        importTrace(traceInfo.getTracePath(), optionFlags);

        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceInfo.getTraceName());
        checkTraceType(traceItem, traceInfo.getTraceType());

        openTrace(traceItem);
        testEventsTable(traceInfo.getTraceName(), traceInfo.getNbEvents(), traceInfo.getFirst());
    }

    private static void importTrace(String traceName, int optionFlags) {
        importTrace(traceName, optionFlags, ImportConfirmation.CONTINUE);
    }

    private static void importTrace(String tracePath, int optionFlags, ImportConfirmation confirmationMode) {
        importTrace(tracePath, optionFlags, () -> confirmationMode);
    }

    /**
     * @param tracePath relative to parent test traces folder
     */
    private static void importTrace(String tracePath, int optionFlags, Supplier<ImportConfirmation> confirmationSuplier) {
        SWTBotTreeItem traceFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);

        SWTBotShell shell = openTraceFoldersImport(traceFolder);
        SWTBot bot = shell.bot();
        final String importDirectoryRelativePath = "import";
        String importDirectoryFullPath = getPath(importDirectoryRelativePath);
        IPath somePath = new Path(importDirectoryRelativePath).append(tracePath);
        IPath fullParentPath = somePath.removeLastSegments(1);
        boolean isDirectory = new Path(importDirectoryFullPath).append(tracePath).toFile().isDirectory();

        SWTBotImportWizardUtils.selectImportFromDirectory(bot, importDirectoryFullPath);
        if (isDirectory) {
            SWTBotImportWizardUtils.selectFolder(fBot, true, somePath.segments());
        } else {
            SWTBotImportWizardUtils.selectFile(bot, new Path(tracePath).lastSegment(), fullParentPath.segments());
        }
        SWTBotImportWizardUtils.setOptions(bot, optionFlags, null);
        bot.button("Finish").click();
        if (confirmationSuplier.get() != ImportConfirmation.CONTINUE) {
            fBot.waitUntil(Conditions.shellIsActive("Confirmation"));
            SWTBotShell shell2 = fBot.activeShell();
            SWTBotButton button = shell2.bot().button(confirmationSuplier.get().getInName());
            button.click();
        }
        fBot.waitUntil(Conditions.shellCloses(shell));
    }

    private static void checkTraceType(SWTBotTreeItem traceItem, String traceType) {
        assertEquals(traceType, getTraceProperty(traceItem, "type"));
    }

    private static void checkTraceLinked(SWTBotTreeItem traceItem, boolean linked) {
        assertEquals(Boolean.toString(linked), getTraceProperty(traceItem, "linked"));
    }

    private static String getTraceProperty(SWTBotTreeItem traceItem, String property) {
        SWTBotUtils.openView(IPageLayout.ID_PROP_SHEET);
        SWTBotView view = fBot.viewById(IPageLayout.ID_PROP_SHEET);
        view.show();
        traceItem.select();
        SWTBotTreeItem traceTypeItem = SWTBotUtils.getTreeItem(view.bot(), view.bot().tree(), "Resource properties", property);
        return traceTypeItem.cell(1);
    }

    // TODO: resolve duplication with ProjectExplorerTraceActionsTest
    private static void testEventsTable(String editorName, long nbEvents, String firstEventStr) {
        SWTBotEditor editor = SWTBotUtils.activeEventsEditor(fBot, editorName);
        fBot.waitUntil(ConditionHelpers.numberOfEventsInTrace(TmfTraceManager.getInstance().getActiveTrace(), nbEvents));

        SWTBotTable table = editor.bot().table();
        fBot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return table.rowCount() > 1;
            }

            @Override
            public String getFailureMessage() {
                return "No items in table";
            }
        });
        // Select first event (skip filter/search row)
        table.getTableItem(1).select();

        editor.bot().waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return table.selection().rowCount() == 1 && table.selection().get(0).toString().contains(firstEventStr);
            }

            @Override
            public String getFailureMessage() {
                return "First event not selected";
            }
        });
    }

    private static SWTBotShell openTraceFoldersImport(SWTBotTreeItem traceItem) {
        traceItem.contextMenu().menu("Import...").click();
        fBot.waitUntil(Conditions.shellIsActive("Trace Import"));

        SWTBotShell shell = fBot.shell("Trace Import");
        return shell;
    }
}
