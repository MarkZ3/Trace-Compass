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

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
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
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;

public final class SWTBotImportWizardUtils {

    public static void selectImportFromDirectory(SWTBot bot, String directoryPath) {
        SWTBotRadio button = bot.radio("Select roo&t directory:");
        button.click();

        SWTBotCombo sourceCombo = bot.comboBox();
        File traceFolderParent = new File(directoryPath);
        sourceCombo.setText(traceFolderParent.getAbsolutePath());

        SWTBotText text = bot.text();
        text.setFocus();
    }

    public static void selectImportFromArchive(SWTBot bot, String archivePath) {
        SWTBotRadio button = bot.radio("Select &archive file:");
        button.click();

        SWTBotCombo sourceCombo = bot.comboBox(1);

        sourceCombo.setText(new File(archivePath).getAbsolutePath());

        SWTBotText text = bot.text();
        text.setFocus();
    }

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

    public static void selectFile(SWTBot bot, String fileName, String... folderTreePath) {
        selectFolder(bot, false, folderTreePath);

        SWTBotTable fileTable = bot.table();
        bot.waitUntil(Conditions.widgetIsEnabled(fileTable));
        bot.waitUntil(ConditionHelpers.isTableItemAvailable(fileName, fileTable));
        SWTBotTableItem tableItem = fileTable.getTableItem(fileName);
        tableItem.check();
    }

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
}
