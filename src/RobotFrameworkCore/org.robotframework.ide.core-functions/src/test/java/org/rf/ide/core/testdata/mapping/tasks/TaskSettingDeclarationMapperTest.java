/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;

public class TaskSettingDeclarationMapperTest {

    @Test
    public void settingDeclarationWithoutLeadingSeparatorCannotBeMapped() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create();
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isFalse();
    }

    @Test
    public void settingDeclarationAfterSingleSeparatorCanBeMapped_1() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create(Separator.spacesSeparator());
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isTrue();
    }

    @Test
    public void settingDeclarationAfterSingleSeparatorCanBeMapped_2() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create(Separator.tabSeparator());
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isTrue();
    }

    @Test
    public void settingDeclarationAfterTwoPipeSeparatorCanBeMapped() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create(Separator.pipeSeparator(), Separator.pipeSeparator());
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isTrue();
    }

    @Test
    public void settingDeclarationAfterTwoSpaceSeparatorsCannotBeMapped() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create(Separator.spacesSeparator(), Separator.spacesSeparator());
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isFalse();
    }

    @Test
    public void settingDeclarationAfterTwoTabSeparatorsCannotBeMapped() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create(Separator.tabSeparator(), Separator.tabSeparator());
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isFalse();
    }

    @Test
    public void settingDeclarationAfterTokenCannotBeMapepd() {
        final Stack<ParsingState> state = stack(ParsingState.TASK_DECLARATION);

        final RobotLine currentLine = RobotLine.create(RobotToken.create("something"));
        final RobotToken currentToken = RobotToken.create("[Teardown]", RobotTokenType.TASK_SETTING_TEARDOWN);

        final TaskSettingDeclarationMapper mapper = teardownSettingMapper();

        final boolean canBeMapped = mapper.checkIfCanBeMapped(null, currentLine, currentToken, "[Teardown]", state);
        assertThat(canBeMapped).isFalse();
    }

    private static TaskSettingDeclarationMapper teardownSettingMapper() {
        return new TaskSettingDeclarationMapper(RobotTokenType.TASK_SETTING_TEARDOWN,
                ParsingState.TASK_SETTING_TEARDOWN, ModelType.TASK_TEARDOWN);
    }

    @SafeVarargs
    private static <T> Stack<T> stack(final T... elemsFromBottomToTop) {
        final Stack<T> stack = new Stack<>();
        for (final T elem : elemsFromBottomToTop) {
            stack.push(elem);
        }
        return stack;
    }
}
