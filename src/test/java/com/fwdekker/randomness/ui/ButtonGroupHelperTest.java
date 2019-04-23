package com.fwdekker.randomness.ui;

import kotlin.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit tests for {@link ButtonGroupHelper}.
 */
final class ButtonGroupHelperTest {
    private ButtonGroup group;


    @BeforeEach
    void beforeEach() {
        group = new ButtonGroup();
    }


    @Test
    void testForEachEmpty() {
        final int[] sum = {0};

        ButtonGroupHelper.INSTANCE.forEach(group, button -> {
            sum[0]++;
            return Unit.INSTANCE;
        });

        assertThat(sum[0]).isEqualTo(0);
    }

    @Test
    void testForEach() {
        final AbstractButton buttonA = new JButton();
        final AbstractButton buttonB = new JButton();
        final AbstractButton buttonC = new JButton();

        group.add(buttonA);
        group.add(buttonB);
        group.add(buttonC);

        final int[] sum = {0};

        ButtonGroupHelper.INSTANCE.forEach(group, button -> {
            sum[0]++;
            return Unit.INSTANCE;
        });

        assertThat(sum[0]).isEqualTo(3);
    }


    @Test
    void testGetValueEmpty() {
        assertThat(ButtonGroupHelper.INSTANCE.getValue(group)).isNull();
    }

    @Test
    void testGetValueNoneSelected() {
        final AbstractButton button = new JButton();

        group.add(button);

        assertThat(ButtonGroupHelper.INSTANCE.getValue(group)).isNull();
    }

    @Test
    void testGetValue() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("29zo4");
        final AbstractButton buttonB = new JButton();
        buttonB.setSelected(true);
        buttonB.setActionCommand("Y6ddy");

        group.add(buttonA);
        group.add(buttonB);

        assertThat(ButtonGroupHelper.INSTANCE.getValue(group)).isEqualTo("Y6ddy");
    }


    @Test
    void testSetValueEmpty() {
        assertThatThrownBy(() -> ButtonGroupHelper.INSTANCE.setValue(group, "syWR#"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Could not find a button with action command `syWR#`.");
    }

    @Test
    void testSetValueNotFound() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("*VgyA");
        final AbstractButton buttonB = new JButton();
        buttonB.setActionCommand("s8vOP");

        group.add(buttonA);
        group.add(buttonB);

        assertThatThrownBy(() -> ButtonGroupHelper.INSTANCE.setValue(group, "OD>5&"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Could not find a button with action command `OD>5&`.");
    }

    @Test
    void testSetValue() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("TRUaN");
        final AbstractButton buttonB = new JButton();
        buttonB.setActionCommand("2Y@2_");
        final AbstractButton buttonC = new JButton();
        buttonC.setActionCommand("#Oq%n");

        group.add(buttonA);
        group.add(buttonB);
        group.add(buttonC);

        ButtonGroupHelper.INSTANCE.setValue(group, "2Y@2_");

        assertThat(buttonB.isSelected()).isTrue();
    }

    @Test
    void testSetValueObject() {
        final AbstractButton buttonA = new JButton();
        buttonA.setActionCommand("iqGfVwJDLd");
        final AbstractButton buttonB = new JButton();
        buttonB.setActionCommand("ouzioKGsKi");
        final AbstractButton buttonC = new JButton();
        buttonC.setActionCommand("pKVEAoQzmr");

        group.add(buttonA);
        group.add(buttonB);
        group.add(buttonC);

        ButtonGroupHelper.INSTANCE.setValue(group, new Object() {
            @Override
            public String toString() {
                return "ouzioKGsKi";
            }
        });

        assertThat(buttonB.isSelected()).isTrue();
    }
}
