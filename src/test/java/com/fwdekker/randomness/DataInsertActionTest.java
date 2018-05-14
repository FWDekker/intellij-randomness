package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link DataInsertAction}.
 */
final class DataInsertActionTest {
    /**
     * The recognizable string that is inserted by the insertion action.
     */
    private static final String RANDOM_STRING = "random_string";

    private DataInsertAction dataInsertAction;


    @BeforeEach
    void beforeEach() {
        dataInsertAction = new SimpleInsertAction();
    }


    /**
     * Tests that no further actions are taken when the editor is {@code null}.
     */
    @Test
    void testActionPerformedNullEditor() {
        final AnActionEvent event = mock(AnActionEvent.class);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(null);

        dataInsertAction.actionPerformed(event);

        verify(event, times(1)).getData(CommonDataKeys.EDITOR);
        verifyNoMoreInteractions(event);
    }

    /**
     * Tests that the action's presentation is disabled when the editor is null.
     */
    @Test
    void testUpdateDisabled() {
        final AnActionEvent event = mock(AnActionEvent.class);
        final Presentation presentation = spy(Presentation.class);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(null);
        when(event.getPresentation()).thenReturn(presentation);

        dataInsertAction.update(event);

        assertThat(presentation.isEnabled()).isFalse();
    }

    /**
     * Tests that the action's presentation is enabled when the editor is not null.
     */
    @Test
    void testUpdateEnabled() {
        final AnActionEvent event = mock(AnActionEvent.class);
        final Presentation presentation = spy(Presentation.class);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mock(Editor.class));
        when(event.getPresentation()).thenReturn(presentation);

        dataInsertAction.update(event);

        assertThat(presentation.isEnabled()).isTrue();
    }


    /**
     * Simple implementation of {@code DataInsertAction}.
     */
    private static class SimpleInsertAction extends DataInsertAction {
        @Override
        protected String getName() {
            return "Insert Simple";
        }

        @Override
        protected String generateString() {
            return RANDOM_STRING;
        }
    }
}
