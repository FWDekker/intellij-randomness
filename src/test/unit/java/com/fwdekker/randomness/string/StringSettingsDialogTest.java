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

    private StringSettings stringSettings;
    private StringSettingsDialog stringSettingsDialog;
    private FrameFixture frame;


    @Override
    protected void onSetUp() {
        stringSettings = new StringSettings();
        stringSettings.setMinLength(DEFAULT_MIN_VALUE);
        stringSettings.setMaxLength(DEFAULT_MAX_VALUE);
        stringSettings.setEnclosure(DEFAULT_ENCLOSURE);
        stringSettings.setAlphabets(DEFAULT_ALPHABETS);

        stringSettingsDialog = GuiActionRunner.execute(() -> new StringSettingsDialog(stringSettings));
        frame = showInFrame(robot(), stringSettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> stringSettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
    }


    @Test
    public void testLoadSettingsMinLength() {
        frame.spinner("minLength").requireValue(DEFAULT_MIN_VALUE);
    }

    @Test
    public void testLoadSettingsMaxLength() {
        frame.spinner("maxLength").requireValue(DEFAULT_MAX_VALUE);
    }

    @Test
    public void testLoadSettingsEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected();
        frame.radioButton("enclosureSingle").requireNotSelected();
        frame.radioButton("enclosureDouble").requireSelected();
        frame.radioButton("enclosureBacktick").requireNotSelected();
    }

    @Test
    public void testLoadSettingsAlphabets() {
        final String[] expectedSelected = DEFAULT_ALPHABETS.stream()
                .map(Alphabet::toString)
                .toArray(String[]::new);

        frame.list("alphabets").requireSelectedItems(expectedSelected);
    }


    @Test
    public void testValidateMinLengthString() {
        frame.spinner("minLength").enterText("foE");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> stringSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Minimum length must be a number.");
    }

    @Test
    public void testValidateMinLengthFloat() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("minLength").target().setValue(553.92f);
            return stringSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Minimum length must be a whole number.");
    }

    @Test
    public void testValidateMinLengthNegative() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("minLength").target().setValue(-161);
            return stringSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Minimum length must be a positive number.");
    }

    @Test
    public void testValidateMaxLengthString() {
        frame.spinner("maxLength").enterText("qsQ");

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> stringSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Maximum length must be a number.");
    }

    @Test
    public void testValidateMaxLengthFloat() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("maxLength").target().setValue(796.01f);
            return stringSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Maximum length must be a whole number.");
    }

    @Test
    public void testValidateMaxLengthOverflow() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("maxLength").target().setValue((long) Integer.MAX_VALUE + 2L);
            return stringSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Maximum length must not be greater than 2^31-1.");
    }

    @Test
    public void testValidateMaxLengthGreaterThanMinLength() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> {
            frame.spinner("maxLength").target().setValue(DEFAULT_MIN_VALUE - 1);
            return stringSettingsDialog.doValidate();
        });

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Maximum length cannot be smaller than minimum length.");
    }

    @Test
    public void testValidateEmptyAlphabetSelection() {
        frame.list("alphabets").clearSelection();

        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> stringSettingsDialog.doValidate());

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.list("alphabets").target());
        assertThat(validationInfo.message).isEqualTo("Select at least one set of symbols.");
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        final Set<Alphabet> newAlphabets = createAlphabetSet(Alphabet.DIGITS, Alphabet.LOWERCASE, Alphabet.SPECIAL);

        GuiActionRunner.execute(() -> {
            frame.spinner("minLength").target().setValue(445);
            frame.spinner("maxLength").target().setValue(803);
            frame.radioButton("enclosureBacktick").target().setSelected(true);
            frame.list("alphabets").target().setSelectedIndices(toIndexForEach(newAlphabets));
        });

        GuiActionRunner.execute(() -> stringSettingsDialog.saveSettings());

        assertThat(stringSettings.getMinLength()).isEqualTo(445);
        assertThat(stringSettings.getMaxLength()).isEqualTo(803);
        assertThat(stringSettings.getEnclosure()).isEqualTo("`");
        assertThat(stringSettings.getAlphabets()).isEqualTo(newAlphabets);
    }

    @Test
    public void testSaveSettingsWithParse() {
        final Set<Alphabet> newAlphabets = createAlphabetSet(Alphabet.DIGITS, Alphabet.LOWERCASE, Alphabet.SPECIAL);

        frame.spinner("minLength").enterTextAndCommit("348");
        frame.spinner("maxLength").enterTextAndCommit("870");
        frame.radioButton("enclosureBacktick").check();
        frame.list("alphabets").selectItems(toStringForEach(newAlphabets));

        GuiActionRunner.execute(() -> stringSettingsDialog.saveSettings());

        assertThat(stringSettings.getMinLength()).isEqualTo(348);
        assertThat(stringSettings.getMaxLength()).isEqualTo(870);
        assertThat(stringSettings.getEnclosure()).isEqualTo("`");
        assertThat(stringSettings.getAlphabets()).isEqualTo(newAlphabets);
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

    /**
     * Returns the enumeration index of each alphabet as an array.
     *
     * @param alphabets a collection of alphabets
     * @return the index of each alphabet
     */
    private int[] toIndexForEach(final Collection<Alphabet> alphabets) {
        return alphabets.stream()
                .mapToInt(Alphabet::ordinal)
                .toArray();
    }
}
