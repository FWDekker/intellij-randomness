package com.fwdekker.randomness.string;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.List;


/**
 * A dialog for creating or editing {@link SymbolSet}s.
 */
final class SymbolSetDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField name;
    private JTextField symbols;

    private final List<String> reservedNames;


    /**
     * Constructs an empty {@code SymbolSetDialog}.
     *
     * @param reservedNames a list of names that cannot be used for the new symbol set
     */
    SymbolSetDialog(final @NotNull List<String> reservedNames) {
        super(null);

        init();
        setTitle("Randomness - Add Symbol Set");

        this.reservedNames = reservedNames;
    }

    /**
     * Constructs a {@code SymbolSetDialog} to edit the given symbol set.
     *
     * @param reservedNames a list of names that cannot be used for the symbol set; the symbol set's current name can be
     *                      reserved as well to enforce that the name is change
     * @param symbolSet     a symbol set
     */
    SymbolSetDialog(final @NotNull List<String> reservedNames, final @NotNull SymbolSet symbolSet) {
        this(reservedNames);

        setTitle("Randomness - Edit Symbol Set");
        this.name.setText(symbolSet.getName());
        this.symbols.setText(symbolSet.getSymbols());
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (name.getText().trim().isEmpty()) {
            return new ValidationInfo("Enter a name.", name);
        }
        if (reservedNames.contains(name.getText())) {
            return new ValidationInfo("That name is used for another symbol set. Use a different name.", name);
        }
        if (symbols.getText().trim().isEmpty()) {
            return new ValidationInfo("Enter at least one symbol.", symbols);
        }

        return null;
    }


    /**
     * Returns the name currently in the text field.
     *
     * @return the name currently in the text field
     */
    @NotNull
    String getName() {
        return name.getText();
    }

    /**
     * Returns the symbols currently in the text field.
     *
     * @return the symbols currently in the text field
     */
    @NotNull
    String getSymbols() {
        return symbols.getText();
    }
}
