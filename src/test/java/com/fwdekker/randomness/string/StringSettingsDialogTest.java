package com.fwdekker.randomness.string;

import com.intellij.openapi.ui.ValidationInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
            = new HashSet<>(Arrays.asList(Alphabet.ALPHABET, Alphabet.ALPHABET));

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
        frame.spinner("minLength").requireValue((long) DEFAULT_MIN_VALUE);
    }

    @Test
    public void testLoadSettingsMaxLength() {
        frame.spinner("maxLength").requireValue((long) DEFAULT_MAX_VALUE);
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
    public void testValidateMinLengthFloat() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(553.92f));

        frame.spinner("minLength").requireValue(553L);
    }

    @Test
    public void testValidateMinLengthNegative() {
        GuiActionRunner.execute(() -> frame.spinner("minLength").target().setValue(-161));

        final ValidationInfo validationInfo = stringSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("minLength").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value greater than or equal to 1.");
    }

    @Test
    public void testValidateMaxLengthFloat() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(796.01f));

        frame.spinner("maxLength").requireValue(796L);
    }

    @Test
    public void testValidateMaxLengthOverflow() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue((long) Integer.MAX_VALUE + 2L));

        final ValidationInfo validationInfo = stringSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("Please enter a value less than or equal to 2147483647.");
    }

    @Test
    public void testValidateMaxLengthGreaterThanMinLength() {
        GuiActionRunner.execute(() -> frame.spinner("maxLength").target().setValue(DEFAULT_MIN_VALUE - 1));

        final ValidationInfo validationInfo = stringSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.spinner("maxLength").target());
        assertThat(validationInfo.message).isEqualTo("The maximum should be no smaller than the minimum.");
    }

    @Test
    public void testValidateEmptyAlphabetSelection() {
        GuiActionRunner.execute(() -> frame.list("alphabets").target().clearSelection());

        final ValidationInfo validationInfo = stringSettingsDialog.doValidate();

        assertThat(validationInfo).isNotNull();
        assertThat(validationInfo.component).isEqualTo(frame.list("alphabets").target());
        assertThat(validationInfo.message).isEqualTo("Please select at least one option.");
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        final Set<Alphabet> newAlphabets = createAlphabetSet(Alphabet.DIGITS, Alphabet.ALPHABET, Alphabet.SPECIAL);

        GuiActionRunner.execute(() -> {
            frame.spinner("minLength").target().setValue(445);
            frame.spinner("maxLength").target().setValue(803);
            frame.radioButton("enclosureBacktick").target().setSelected(true);
            frame.list("alphabets").target().setSelectedIndices(toIndexForEach(newAlphabets));
        });

        stringSettingsDialog.saveSettings();

        assertThat(stringSettings.getMinLength()).isEqualTo(445);
        assertThat(stringSettings.getMaxLength()).isEqualTo(803);
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
