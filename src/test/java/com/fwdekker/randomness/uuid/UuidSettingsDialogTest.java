package com.fwdekker.randomness.uuid;

import com.intellij.openapi.ui.ValidationInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;


/**
 * GUI tests for {@link UuidSettingsDialog}.
 */
public final class UuidSettingsDialogTest extends AssertJSwingJUnitTestCase {
    private static final String DEFAULT_ENCLOSURE = "'";

    private UuidSettings uuidSettings;
    private UuidSettingsDialog uuidSettingsDialog;
    private FrameFixture frame;


    @Override
    protected void onSetUp() {
        uuidSettings = new UuidSettings();
        uuidSettings.setEnclosure(DEFAULT_ENCLOSURE);

        uuidSettingsDialog = GuiActionRunner.execute(() -> new UuidSettingsDialog(uuidSettings));
        frame = showInFrame(robot(), uuidSettingsDialog.createCenterPanel());
    }


    @Test
    public void testDefaultIsValid() {
        final ValidationInfo validationInfo = GuiActionRunner.execute(() -> uuidSettingsDialog.doValidate());

        assertThat(validationInfo).isNull();
    }


    @Test
    public void testLoadSettingsEnclosure() {
        frame.radioButton("enclosureNone").requireNotSelected();
        frame.radioButton("enclosureSingle").requireSelected();
        frame.radioButton("enclosureDouble").requireNotSelected();
        frame.radioButton("enclosureBacktick").requireNotSelected();
    }


    @Test
    public void testSaveSettingsWithoutParse() {
        GuiActionRunner.execute(() -> frame.radioButton("enclosureBacktick").target().setSelected(true));

        uuidSettingsDialog.saveSettings();

        assertThat(uuidSettings.getEnclosure()).isEqualTo("`");
    }
}
