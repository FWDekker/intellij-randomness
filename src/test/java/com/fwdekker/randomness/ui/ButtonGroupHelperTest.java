package com.fwdekker.randomness.ui;

import org.junit.Before;
import org.junit.Test;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link ButtonGroupHelper}.
 */
public final class ButtonGroupHelperTest {
    private ButtonGroup group;


    @Before
    public void beforeEach() {
        group = new ButtonGroup();
    }


    @Test
    public void testForEachEmpty() {
        final int[] sum = {0};

        ButtonGroupHelper.forEach(group, button -> sum[0]++);

        assertThat(sum[0]).isEqualTo(0);
    }

    @Test
    public void testForEach() {
        final AbstractButton buttonA = new JButton();
        final AbstractButton buttonB = new JButton();
        final AbstractButton buttonC = new JButton();

        group.add(buttonA);
        group.add(buttonB);
        group.add(buttonC);

        final int[] sum = {0};

        ButtonGroupHelper.forEach(group, button -> sum[0]++);

        assertThat(sum[0]).isEqualTo(3);
    }


    @Test
    public void testGetValueEmpty() {
        assertThat(ButtonGroupHelper.getValue(group)).isNull();
    }

    @Test
    public void testGetValueNoneSelected() {
        final AbstractButton button = new JButton();

        group.add(button);

        assertThat(ButtonGroupHelper.getValue(group)).isNull();
    }

    @Test
    public void testGetValue() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("29zo4");
        final AbstractButton buttonB = new JButton();
        buttonB.setSelected(true);
        buttonB.setActionCommand("Y6ddy");

        group.add(buttonA);
        group.add(buttonB);

        assertThat(ButtonGroupHelper.getValue(group)).isEqualTo("Y6ddy");
    }


    @Test
    public void testSetValueEmpty() {
        assertThatThrownBy(() -> ButtonGroupHelper.setValue(group, "syWR#"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Could not find a button with action command `syWR#`.");
    }

    @Test
    public void testSetValueNotFound() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("*VgyA");
        final AbstractButton buttonB = new JButton();
        buttonB.setActionCommand("s8vOP");

        group.add(buttonA);
        group.add(buttonB);

        assertThatThrownBy(() -> ButtonGroupHelper.setValue(group, "OD>5&"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Could not find a button with action command `OD>5&`.");
    }

    @Test
    public void testSetValue() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("TRUaN");
        final AbstractButton buttonB = new JButton();
        buttonB.setActionCommand("2Y@2_");
        final AbstractButton buttonC = new JButton();
        buttonC.setActionCommand("#Oq%n");

        group.add(buttonA);
        group.add(buttonB);
        group.add(buttonC);

        ButtonGroupHelper.setValue(group, "2Y@2_");

        assertThat(buttonB.isSelected()).isTrue();
    }

    @Test
    public void testSetValueObject() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("iqGfVwJDLd");
        final AbstractButton buttonB = new JButton();
        buttonB.setActionCommand("ouzioKGsKi");
        final AbstractButton buttonC = new JButton();
        buttonC.setActionCommand("pKVEAoQzmr");

        group.add(buttonA);
        group.add(buttonB);
        group.add(buttonC);

        ButtonGroupHelper.setValue(group, new Object() {
            @Override
            public String toString() {
                return "ouzioKGsKi";
            }
        });

        assertThat(buttonB.isSelected()).isTrue();
    }
}
