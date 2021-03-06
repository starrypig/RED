/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;
import java.util.Objects;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingCommentCommand extends EditorCommand {

    private final RobotSetting setting;

    private final String newComment;

    private String previousComment;

    public SetSettingCommentCommand(final IEventBroker eventBroker, final RobotSetting setting,
            final String comment) {
        this.eventBroker = eventBroker;
        this.setting = setting;
        this.newComment = comment;
    }

    public SetSettingCommentCommand(final RobotSetting setting, final String comment) {
        this.setting = setting;
        this.newComment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        previousComment = setting.getComment();
        if (Objects.equals(previousComment, newComment)) {
            return;
        }
        CommentServiceHandler.update((ICommentHolder) setting.getLinkedElement(),
                ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, newComment);
        setting.resetStored();

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, setting);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetSettingCommentCommand(setting, previousComment));
    }
}
