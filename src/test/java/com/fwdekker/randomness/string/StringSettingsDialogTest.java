package com.fwdekker.randomness.string;

import com.intellij.openapi.ui.ValidationInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link StringSettingsDialog}.
 */
public final class StringSettingsDialogTest extends AssertJSwingJUnitTestCase {
    private static final int DEFAULT_MIN_VALUE = 144;
    private static final int DEFAULT_MAX_VALUE = 719;
    private static final String DEFAULT_ENCLOSURE = "\"";
    private static final Set<Alphabet> DEFAULT_ALPHABETS
            = new HashSet<>(Arrays.asList(Alphabet.UPPERCASE, Alphabet.LOWERCASE));

    private StringSettings decimalSettings;
    private StringSettingsDialog decimalSettingsDialog;
    private FrameFixture frame;


    @Override
    protected void onSetUp() {
        decimalSettings = new StringSettings();
        decimalSettings.setMinLength(DEFAULT_MIN_VALUE);
        decimalSettings.setMaxLength(DEFAULT_MAX_VALUE);
        decimalSettings.setEnclosure(DEFAULT_ENCLOSURE);
        decimalSettings.setAlphabets(DEFAULT_ALPHABETS);

        decimalSettingsDialog = GuiActionRunner.execute(() -> new StringSettingsDialog(decimalSettings));
        frame = showInFrame(robot(), decimalSettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultMinLength() {
        frame.spinner("minLength").requireValue(DEFAULT_MIN_VALUE);
    }

    @Test
    public void testDefaultMaxLength() {
        frame.spinner("maxLength").requireValue(DEFAULT_MAX_VALUE);
    }

    @Test
    public void testDefaultEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected();
        frame.radioButton("enclosureSingle").requireNotSelected();
        frame.radioButton("enclosureDouble").requireSelected();
        frame.radioButton("enclosureBacktick").requireNotSelected();
    }

    @Test
    public void testDefaultAlphabets() {
        final String[] expectedSelected = DEFAULT_ALPHABETS.stream()
                .map(Alphabet::toString)
                .toArray(String[]::new);

        frame.list("alphabets").requireSelectedItems(expectedSelected);
    }

    @Test
    public void testDefaultIsValid() {
        assertThat(decimalSettingsDialog.doValidate()).isNull();
    }

    @Test
    public void testMinLengthType() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(929.35f));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Minimum length must be an integer.");
    }

    @Test
    public void testMaxLengthType() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(585.95f));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Maximum length must be an integer.");
    }

    @Test
    public void testMaxLengthGreaterThanMinLength() {
        frame.spinner("maxLength").enterTextAndCommit(Integer.toString(DEFAULT_MIN_VALUE - 1));

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Maximum length cannot be smaller than minimum length.");
    }

    @Test
    public void testEmptyAlphabetSelection() {
        frame.list("alphabets").clearSelection();

        final ValidationInfo validationInfo = decimalSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.list("alphabets").target());
        assertThat(validationInfo.message).isEqualTo("Select at least one set of symbols.");
    }

    @Test
    public void testSaveSettings() {
        final Set<Alphabet> newAlphabets = createAlphabetSet(Alphabet.DIGITS, Alphabet.LOWERCASE, Alphabet.SPECIAL);

        frame.spinner("minLength").enterText("348");
        frame.spinner("maxLength").enterText("870");
        frame.radioButton("enclosureBacktick").check();
        frame.list("alphabets").selectItems(toStringForEach(newAlphabets));

        GuiActionRunner.execute(() -> decimalSettingsDialog.saveSettings());

        assertThat(decimalSettings.getMinLength()).isEqualTo(348);
        assertThat(decimalSettings.getMaxLength()).isEqualTo(870);
        assertThat(decimalSettings.getEnclosure()).isEqualTo("`");
        assertThat(decimalSettings.getAlphabets()).isEqualTo(newAlphabets);
    }


    /**
     * Creates a {@code HashSet} from the given elements.
     *
     * @param alphabets a number of alphabets
     * @return a {@code HashSet} from the given elements
     */
    private Set<Alphabet> createAlphabetSet(final Alphabet... alphabets) {
        return new HashSet<>(Arrays.asList(alphabets));
    }

    /**
     * Calls {@code toString} on each alphabet and returns the result as an array.
     *
     * @param alphabets a collection of alphabets
     * @return the {@code toString} of each alphabet
     */
    private String[] toStringForEach(final Collection<Alphabet> alphabets) {
        return alphabets.stream()
                .map(Object::toString)
                .toArray(String[]::new);
    }
}
