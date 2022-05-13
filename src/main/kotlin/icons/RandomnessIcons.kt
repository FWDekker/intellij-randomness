package icons

import com.intellij.openapi.util.IconLoader


/**
 * All Randomness icons.
 *
 * @param base the path to the basic icon for the data type
 * @param array the path to the array variant of the basic icon
 * @param repeat the path to the repeat variant of the basic icon
 * @param repeatArray the path to the repeat array variant of the basic icon
 * @param settings the path to the settings variant of the basic icon
 * @param quickSwitchScheme the path to the quick switch scheme variant of the basic icon
 */
@Suppress("PropertyName", "VariableNaming") // Should look like an enum
enum class RandomnessIcons(
    base: kotlin.String,
    array: kotlin.String,
    repeat: kotlin.String,
    repeatArray: kotlin.String,
    settings: kotlin.String,
    quickSwitchScheme: kotlin.String
) {
    /**
     * Icons for decimals.
     */
    Decimal(
        "/icons/decimal.svg",
        "/icons/decimal-array.svg",
        "/icons/decimal-repeat.svg",
        "/icons/decimal-repeat-array.svg",
        "/icons/decimal-settings.svg",
        "/icons/decimal-quick-switch-scheme.svg"
    ),

    /**
     * Icons for integers.
     */
    Integer(
        "/icons/integer.svg",
        "/icons/integer-array.svg",
        "/icons/integer-repeat.svg",
        "/icons/integer-repeat-array.svg",
        "/icons/integer-settings.svg",
        "/icons/integer-quick-switch-scheme.svg"
    ),

    /**
     * Icons for strings.
     */
    String(
        "/icons/string.svg",
        "/icons/string-array.svg",
        "/icons/string-repeat.svg",
        "/icons/string-repeat-array.svg",
        "/icons/string-settings.svg",
        "/icons/string-quick-switch-scheme.svg"
    ),

    /**
     * Icons for words.
     */
    Word(
        "/icons/word.svg",
        "/icons/word-array.svg",
        "/icons/word-repeat.svg",
        "/icons/word-repeat-array.svg",
        "/icons/word-settings.svg",
        "/icons/word-quick-switch-scheme.svg"
    ),

    /**
     * Icons for UUIDs.
     */
    Uuid(
        "/icons/uuid.svg",
        "/icons/uuid-array.svg",
        "/icons/uuid-repeat.svg",
        "/icons/uuid-repeat-array.svg",
        "/icons/uuid-settings.svg",
        "/icons/uuid-quick-switch-scheme.svg"
    ),

    /**
     * Basic icons.
     */
    Data(
        "/icons/data.svg",
        "/icons/data-array.svg",
        "/icons/data-repeat.svg",
        "/icons/data-repeat-array.svg",
        "/icons/data-settings.svg",
        "/icons/data-quick-switch-scheme.svg"
    );


    /**
     * The basic icon for the data type.
     */
    val Base = IconLoader.findIcon(base, this.javaClass.classLoader)!!

    /**
     * The array variant of the basic icon.
     */
    val Array = IconLoader.findIcon(array, this.javaClass.classLoader)!!

    /**
     * The repeat variant of the basic icon.
     */
    val Repeat = IconLoader.findIcon(repeat, this.javaClass.classLoader)!!

    /**
     * The repeat array variant of the basic icon.
     */
    val RepeatArray = IconLoader.findIcon(repeatArray, this.javaClass.classLoader)!!

    /**
     * The settings variant of the basic icon.
     */
    val Settings = IconLoader.findIcon(settings, this.javaClass.classLoader)!!

    /**
     * The quick switch scheme variant of the basic icon.
     */
    val QuickSwitchScheme = IconLoader.findIcon(quickSwitchScheme, this.javaClass.classLoader)!!
}
