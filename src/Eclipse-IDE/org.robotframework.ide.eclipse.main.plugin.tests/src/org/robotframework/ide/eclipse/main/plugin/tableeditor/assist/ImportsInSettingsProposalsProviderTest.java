/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class ImportsInSettingsProposalsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            ImportsInSettingsProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("kw_based_settings.robot",
                "*** Settings ***",
                "Suite Setup",
                "Suite Teardown",
                "Test Setup",
                "Test Teardown",
                "Test Template",
                "Task Setup",
                "Task Teardown",
                "Task Template");
        projectProvider.createFile("non_kw_based_settings.robot",
                "*** Settings ***",
                "Library",
                "Resource",
                "Variables",
                "Metadata",
                "Test Timeout",
                "Task Timeout",
                "Force Tags",
                "Default Tags");
        projectProvider.createFile("kw_based_settings_with_resource.robot",
                "*** Settings ***",
                "Suite Setup",
                "Suite Teardown",
                "Test Setup",
                "Test Teardown",
                "Test Template",
                "Task Setup",
                "Task Teardown",
                "Task Template",
                "*** Settings ***",
                "Resource  res.robot");
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenColumnIsDifferentThanSecond() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            for (int row = 0; row < settings.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                if (column == 1) {
                    assertThat(provider.shouldShowProposals(context)).isTrue();
                } else {
                    assertThat(provider.shouldShowProposals(context)).isFalse();
                }
            }
        }
    }

    @Test
    public void proposalsShouldNotBeShown_whenSettingIsNotKeywordBased() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(projectProvider.getFile("non_kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.shouldShowProposals(context)).isFalse();
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.getProposals("foo", 1, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("rrr");

        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(projectProvider.getFile("kw_based_settings_with_resource.robot"));

        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size() - 1; row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.getProposals(text.getText(), 1, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("res.");
        }
    }

    private static IRowDataProvider<Object> prepareSettingsProvider(final List<RobotKeywordCall> settings) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        for (int i = 0; i < settings.size(); i++) {
            final Map<String, Object> map = new HashMap<>();
            map.put(settings.get(i).getName(), settings.get(i));
            final Entry<String, Object> entry = map.entrySet().iterator().next();

            when(dataProvider.getRowObject(i)).thenReturn(entry);
        }
        return dataProvider;
    }
}
