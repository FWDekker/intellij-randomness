package com.fwdekker.randomness;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import java.net.URL;
import org.junit.AfterClass;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for {@link InsertRandomSomething}.
 * <p>
 * Note that {@link LightPlatformCodeInsightFixtureTestCase} is a JUnit 3 test class.
 */
@SuppressWarnings("PMD.AddEmptyString") // These were added for readability
public final class InsertRandomSomethingIntegrationTest extends LightPlatformCodeInsightFixtureTestCase {
    /**
     * The recognizable string that is inserted by the insertion action.
     */
    private static final String RANDOM_STRING = "random_string";

    private InsertRandomSimple insertRandomSimple;
    private Document document;
    private CaretModel caretModel;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final VirtualFile file = myFixture.copyFileToProject("emptyFile.txt");

        myFixture.openFileInEditor(file);

        document = myFixture.getEditor().getDocument();
        caretModel = myFixture.getEditor().getCaretModel();
        insertRandomSimple = new InsertRandomSimple();
    }

    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();

        this.document = null;
        this.caretModel = null;
        this.insertRandomSimple = null;
    }

    @Override
    protected String getTestDataPath() {
        final URL url = getClass().getClassLoader().getResource("testData/");
        assert url != null;

        return url.getPath();
    }


    public void testInsertIntoEmpty() {
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo(RANDOM_STRING);
    }

    public void testInsertBefore() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> document.setText("RkpjkS9Itb"));

        caretModel.moveToOffset(0);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo(RANDOM_STRING + "RkpjkS9Itb");
    }

    public void testInsertAfter() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> document.setText("0aiMbK5hK5"));

        caretModel.moveToOffset(10);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo("0aiMbK5hK5" + RANDOM_STRING);
    }

    public void testInsertBetween() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> document.setText("U6jBDMh8Nq"));

        caretModel.moveToOffset(5);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo("U6jBD" + RANDOM_STRING + "Mh8Nq");
    }

    public void testReplaceAll() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> document.setText("fMhAajjDw6"));

        setSelection(0, 10);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo(RANDOM_STRING);
    }

    public void testReplacePart() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> document.setText("qZPGZDEcPS"));

        setSelection(3, 7);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo("qZP" + RANDOM_STRING + "cPS");
    }

    public void testInsertMultiple() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(),
                () -> document.setText("DCtD41lFOk\nOCnrdYk9gE\nn1HAPKotDq"));

        addCaret(11);
        addCaret(22);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo(""
                + RANDOM_STRING + "DCtD41lFOk\n"
                + RANDOM_STRING + "OCnrdYk9gE\n"
                + RANDOM_STRING + "n1HAPKotDq"
        );
    }

    public void testReplaceMultiple() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(),
                () -> document.setText("YXSncq4FC9\nG31Ybbn1c4\nTNCqAhqPnh"));

        setSelection(2, 4);
        addSelection(18, 23);
        addSelection(29, 29);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo(""
                + "YX" + RANDOM_STRING + "cq4FC9\n"
                + "G31Ybbn" + RANDOM_STRING
                + "NCqAhq" + RANDOM_STRING + "Pnh"
        );
    }

    public void testInsertAndReplace() {
        WriteCommandAction.runWriteCommandAction(myFixture.getProject(),
                () -> document.setText("XOppzVdZTj\nZhAaVfQynW\nk3kWemkdAg"));

        caretModel.moveToOffset(5);
        addSelection(6, 9);
        addCaret(15);
        addSelection(24, 28);
        myFixture.testAction(insertRandomSimple);

        assertThat(document.getText()).isEqualTo(""
                + "XOppz" + RANDOM_STRING + "V" + RANDOM_STRING + "j\n"
                + "ZhAa" + RANDOM_STRING + "VfQynW\n"
                + "k3" + RANDOM_STRING + "kdAg");
    }


    /**
     * Causes the first caret to select the given interval.
     *
     * @param fromOffset the start of the selected interval
     * @param toOffset   the end of the selected interval
     */
    private void setSelection(final int fromOffset, final int toOffset) {
        caretModel.getAllCarets().get(0).setSelection(fromOffset, toOffset);
    }

    /**
     * Adds a caret at the given offset.
     *
     * @param offset an offset
     */
    private void addCaret(final int offset) {
        final VisualPosition visualPosition = myFixture.getEditor().offsetToVisualPosition(offset);

        caretModel.addCaret(visualPosition);
    }

    /**
     * Adds a caret that selects the given interval.
     *
     * @param fromOffset the start of the selected interval
     * @param toOffset   the end of the selected interval
     */
    private void addSelection(final int fromOffset, final int toOffset) {
        final VisualPosition fromVisualPosition = myFixture.getEditor().offsetToVisualPosition(fromOffset);

        caretModel.addCaret(fromVisualPosition);

        final int caretCount = caretModel.getCaretCount();
        caretModel.getAllCarets().get(caretCount - 1).setSelection(fromOffset, toOffset);
    }


    /**
     * Simple implementation of {@code InsertRandomSomething}.
     */
    private static class InsertRandomSimple extends InsertRandomSomething {
        @Override
        protected String getName() {
            return "Insert Random Simple";
        }

        @Override
        protected String generateString() {
            return RANDOM_STRING;
        }
    }
}
