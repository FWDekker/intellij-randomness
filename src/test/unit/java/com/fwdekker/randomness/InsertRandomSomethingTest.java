package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link InsertRandomSomething}.
 */
public final class InsertRandomSomethingTest {
    /**
     * The recognizable string that is inserted by the insertion action.
     */
    private static final String RANDOM_STRING = "random_string";

    private InsertRandomSomething insertRandomSomething;


    @Before
    public void beforeEach() {
        insertRandomSomething = new InsertRandomSimple();
    }


    /**
     * Tests that no further actions are taken when the editor is {@code null}.
     */
    @Test
    public void testActionPerformedNullEditor() {
        final AnActionEvent event = mock(AnActionEvent.class);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(null);

        insertRandomSomething.actionPerformed(event);

        verify(event, times(1)).getData(CommonDataKeys.EDITOR);
        verifyNoMoreInteractions(event);
    }

    /**
     * Tests that the action's presentation is disabled when the editor is null.
     */
    @Test
    public void testUpdateDisabled() {
        final AnActionEvent event = mock(AnActionEvent.class);
        final Presentation presentation = spy(Presentation.class);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(null);
        when(event.getPresentation()).thenReturn(presentation);

        insertRandomSomething.update(event);

        assertThat(presentation.isEnabled()).isFalse();
    }

    /**
     * Tests that the action's presentation is enabled when the editor is not null.
     */
    @Test
    public void testUpdateEnabled() {
        final AnActionEvent event = mock(AnActionEvent.class);
        final Presentation presentation = spy(Presentation.class);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mock(Editor.class));
        when(event.getPresentation()).thenReturn(presentation);

        insertRandomSomething.update(event);

        assertThat(presentation.isEnabled()).isTrue();
    }


    /**
     * Simple implementation of {@code InsertRandomSomething}.
     */
    private static class InsertRandomSimple extends InsertRandomSomething {
        @Override
        protected String generateString() {
            return RANDOM_STRING;
        }
    }
}
