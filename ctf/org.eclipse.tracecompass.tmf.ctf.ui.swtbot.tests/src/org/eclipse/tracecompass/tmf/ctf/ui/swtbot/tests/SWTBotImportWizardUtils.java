/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;

/**
 * Several SWTBot utility methods common to testing import test cases.
 */
public final class SWTBotImportWizardUtils {

    /**
     * While in the import wizard, select the specified directory as a source.
     *
     * @param bot
     *            the SWTBot
     * @param directoryPath
     *            the directory path to set as a source
     */
    public static void selectImportFromDirectory(SWTBot bot, String directoryPath) {
        SWTBotRadio button = bot.radio("Select roo&t directory:");
        button.click();

        SWTBotCombo sourceCombo = bot.comboBox();
        File traceFolderParent = new File(directoryPath);
        sourceCombo.setText(traceFolderParent.getAbsolutePath());

        SWTBotText text = bot.text();
        text.setFocus();
    }

    /**
     * While in the import wizard, select the specified archive as a source.
     *
     * @param bot
     *            the SWTBot
     * @param archivePath
     *            the archive path to set as a source
     */
    public static void selectImportFromArchive(SWTBot bot, String archivePath) {
        SWTBotRadio button = bot.radio("Select &archive file:");
        button.click();

        SWTBotCombo sourceCombo = bot.comboBox(1);

        sourceCombo.setText(new File(archivePath).getAbsolutePath());

        SWTBotText text = bot.text();
        text.setFocus();
    }

    /**
     * While in the import wizard, select a folder in the file selection tree.
     *
     * @param bot
     *            the SWTBot
     * @param check
     *            whether or not to check the folder item
     * @param treePath
     *            the path to the folder in the tree
     */
    public static void selectFolder(SWTBot bot, boolean check, String... treePath) {
        SWTBotTree tree = bot.tree();
        bot.waitUntil(Conditions.widgetIsEnabled(tree));
        SWTBotTreeItem folderNode = SWTBotUtils.getTreeItem(bot, tree, treePath);
        if (check) {
            folderNode.check();
        } else {
            folderNode.select();
        }
    }

    /**
     * While in the import wizard, select a file in the file selection tree.
     *
     * @param bot
     *            the SWTBot
     * @param fileName
     *            the name of the file to select
     * @param folderTreePath
     *            the path to the parent folder in the tree
     */
    public static void selectFile(SWTBot bot, String fileName, String... folderTreePath) {
        selectFolder(bot, false, folderTreePath);

        SWTBotTable fileTable = bot.table();
        bot.waitUntil(Conditions.widgetIsEnabled(fileTable));
        bot.waitUntil(ConditionHelpers.isTableItemAvailable(fileName, fileTable));
        SWTBotTableItem tableItem = fileTable.getTableItem(fileName);
        tableItem.check();
    }

    /**
     * While in the import wizard, set the various options (checkboxes, trace
     * type combo).
     *
     * @param bot
     *            the SWTBot
     * @param optionFlags
     *            options that affects mostly checkboxes, see
     *            {@link ImportTraceWizardPage#OPTION_CREATE_LINKS_IN_WORKSPACE}
     *            for example.
     * @param traceTypeName
     *            the trace type to select in the combobox, or null for
     *            auto-detect.
     */
    public static void setOptions(SWTBot bot, int optionFlags, String traceTypeName) {
        SWTBotCheckBox checkBox = bot.checkBox(Messages.ImportTraceWizard_CreateLinksInWorkspace);
        if (checkBox.isEnabled()) {
            if ((optionFlags & ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE) != 0) {
                checkBox.select();
            } else {
                checkBox.deselect();
            }
        }

        checkBox = bot.checkBox(Messages.ImportTraceWizard_PreserveFolderStructure);
        if ((optionFlags & ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        checkBox = bot.checkBox(Messages.ImportTraceWizard_ImportUnrecognized);
        if ((optionFlags & ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        checkBox = bot.checkBox(Messages.ImportTraceWizard_OverwriteExistingTrace);
        if ((optionFlags & ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        checkBox = bot.checkBox(Messages.ImportTraceWizard_CreateExperiment);
        if ((optionFlags & ImportTraceWizardPage.OPTION_CREATE_EXPERIMENT) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        SWTBotCombo comboBox = bot.comboBoxWithLabel(Messages.ImportTraceWizard_TraceType);
        if (traceTypeName != null && !traceTypeName.isEmpty()) {
            comboBox.setSelection(traceTypeName);
        } else {
            comboBox.setSelection(ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        }
    }

    /**
     * Test that the events editor
     *
     * @param bot
     *            the SWTBot
     * @param editorName
     *            the expected name of the editor
     * @param nbEvents
     *            the expected number of events
     * @param firstEventStr
     *            the first event timestamp in string form. This is used to see
     *            if the cell contains this text (String.contains()). Since
     *            there can be timezone issues with hours and days, this value
     *            should only specify minutes and more precise digits. For
     *            example: 04:32.650 993 664
     */
    public static void testEventsTable(SWTWorkbenchBot bot, String editorName, long nbEvents, String firstEventStr) {
        SWTBotEditor editor = SWTBotUtils.activeEventsEditor(bot, editorName);
        bot.waitUntil(ConditionHelpers.numberOfEventsInTrace(TmfTraceManager.getInstance().getActiveTrace(), nbEvents));

        if (nbEvents == 0 || firstEventStr == null || firstEventStr.isEmpty()) {
            return;
        }

        SWTBotTable table = editor.bot().table();
        bot.waitUntil(new DefaultCondition() {
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
                boolean ret = table.selection().rowCount() == 1 && table.selection().get(0).toString().contains(firstEventStr);
                if (!ret) {
                    // FIXME: Not sure why, sometimes the first select() ends up
                    // selecting an empty item. Retry selecting here.
                    table.getTableItem(1).select();
                }
                return ret;
            }

            @Override
            public String getFailureMessage() {
                return "First event not selected.";
            }
        });
    }
}
