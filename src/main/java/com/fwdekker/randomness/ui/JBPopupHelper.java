package com.fwdekker.randomness.ui;

import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.speedSearch.SpeedSearch;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.stream.IntStream;


/**
 * A collection of helper methods for dealing with {@link com.intellij.openapi.ui.popup.JBPopup}s.
 */
public final class JBPopupHelper {
    private static final int NINE = 9;


    /**
     * Private constructor to prevent instantiation.
     */
    private JBPopupHelper() {
        // Do nothing
    }


    /**
     * Disables speed search.
     *
     * @param popup the popup to disable speed search for
     */
    public static void disableSpeedSearch(final ListPopupImpl popup) {
        final SpeedSearch speedSearch = popup.getSpeedSearch();
        speedSearch.setEnabled(false);
        speedSearch.addChangeListener(event -> speedSearch.updatePattern(""));
    }

    /**
     * Registers actions such that actions can be selected while holding {@code Shift}.
     *
     * @param popup       the popup to enable selecting with {@code Shift} for
     * @param normalTitle the title of the popup while the {@code Shift} key is not pressed
     * @param shiftTitle  the title of the popup while the {@code Shift} key is pressed
     */
    public static void registerShiftActions(final ListPopupImpl popup, final String normalTitle,
                                            final String shiftTitle) {
        registerModifierActions(popup, ModifierKey.SHIFT, normalTitle, shiftTitle);
    }

    /**
     * Registers actions such that actions can be selected while holding {@code Ctrl} or {@code Cmd}.
     *
     * @param popup       the popup to enable selecting with {@code Ctrl}/{@code Cmd} for
     * @param normalTitle the title of the popup while the {@code Ctrl}/{@code Cmd} key is not pressed
     * @param ctrlTitle   the title of the popup while the {@code Ctrl}/{@code Cmd} key is pressed
     */
    public static void registerCtrlActions(final ListPopupImpl popup, final String normalTitle,
                                           final String ctrlTitle) {
        registerModifierActions(popup, ModifierKey.CTRL, normalTitle, ctrlTitle);
    }

    /**
     * Registers actions such that actions can be selected while holding {@code Ctrl} or {@code Cmd}.
     *
     * @param popup         the popup to enable selecting with the modifier key for
     * @param modifierKey   the modifier key for which actions should be registered
     * @param normalTitle   the title of the popup while the modifier key is not pressed
     * @param modifierTitle the title of the popup while the modifier key is pressed
     */
    private static void registerModifierActions(final ListPopupImpl popup, final ModifierKey modifierKey,
                                                final String normalTitle, final String modifierTitle) {
        final String lcShortModifierName = modifierKey.shortName.toLowerCase(Locale.getDefault());
        final String lcLongModifierName = modifierKey.longName.toLowerCase(Locale.getDefault());
        final String ucLongModifierName = modifierKey.longName.toUpperCase(Locale.getDefault());

        popup.registerAction(
            lcShortModifierName + "Released",
            KeyStroke.getKeyStroke("released " + ucLongModifierName),
            new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    popup.setCaption(normalTitle);
                }
            }
        );
        popup.registerAction(
            lcShortModifierName + "Pressed",
            KeyStroke.getKeyStroke(lcLongModifierName + " pressed " + ucLongModifierName),
            new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    popup.setCaption(modifierTitle);
                }
            }
        );
        popup.registerAction(
            lcShortModifierName + "InvokeAction",
            KeyStroke.getKeyStroke(lcLongModifierName + " ENTER"),
            new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    final KeyEvent keyEvent = new KeyEvent(
                        popup.getComponent(), event.getID(), event.getWhen(),
                        event.getModifiers(), KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED,
                        KeyEvent.KEY_LOCATION_UNKNOWN
                    );
                    popup.handleSelect(true, keyEvent);
                }
            }
        );

        IntStream.range(1, NINE).forEach(key -> popup
            .registerAction(
                lcShortModifierName + "Invoke" + key,
                KeyStroke.getKeyStroke(lcLongModifierName + " " + key),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        final KeyEvent keyEvent = new KeyEvent(
                            popup.getComponent(), event.getID(),
                            event.getWhen(), event.getModifiers(),
                            KeyEvent.VK_ENTER,
                            KeyEvent.CHAR_UNDEFINED,
                            KeyEvent.KEY_LOCATION_UNKNOWN
                        );
                        popup.getList().addSelectionInterval(key - 1, key - 1);
                        popup.handleSelect(true, keyEvent);
                    }
                }
            )
        );
    }

    /**
     * Displays a popup with a title and two messages.
     *
     * @param title    the title of the popup
     * @param messageA the first message
     * @param messageB the second message
     */
    public static void showMessagePopup(final String title, final String messageA, final String messageB) {
        final Runnable nop = () -> {
        };

        JBPopupFactory.getInstance().createConfirmation(
            title,
            messageA,
            messageB,
            nop,
            1
        ).showInFocusCenter();
    }


    /**
     * Pairs the short and long name of a modifier key together.
     */
    private enum ModifierKey {
        ALT("alt", "alt"),
        CTRL("ctrl", "control"),
        SHIFT("shift", "shift");


        private final String shortName;
        private final String longName;


        /**
         * Constructs a new modifier key.
         *
         * @param shortName the short name of the modifier key
         * @param longName  the long name of the modifier key
         */
        ModifierKey(final String shortName, final String longName) {
            this.shortName = shortName;
            this.longName = longName;
        }
    }
}
