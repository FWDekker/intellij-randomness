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
 */
@Suppress("PropertyName", "VariableNaming") // Should look like an enum
enum class RandomnessIcons(
    base: kotlin.String,
    array: kotlin.String,
    repeat: kotlin.String,
    repeatArray: kotlin.String,
    settings: kotlin.String
) {
    /**
     * Icons for decimals.
     */
    Decimal("/icons/decimal.svg",
        "/icons/decimal-array.svg",
        "/icons/decimal-repeat.svg",
        "/icons/decimal-repeat-array.svg",
        "/icons/decimal-settings.svg"),

    /**
     * Icons for integers.
     */
    Integer("/icons/integer.svg",
        "/icons/integer-array.svg",
        "/icons/integer-repeat.svg",
        "/icons/integer-repeat-array.svg",
        "/icons/integer-settings.svg"),

    /**
     * Icons for strings.
     */
    String("/icons/string.svg",
        "/icons/string-array.svg",
        "/icons/string-repeat.svg",
        "/icons/string-repeat-array.svg",
        "/icons/string-settings.svg"),

    /**
     * Icons for words.
     */
    Word("/icons/word.svg",
        "/icons/word-array.svg",
        "/icons/word-repeat.svg",
        "/icons/word-repeat-array.svg",
        "/icons/word-settings.svg"),

    /**
     * Icons for UUIDs.
     */
    Uuid("/icons/uuid.svg",
        "/icons/uuid-array.svg",
        "/icons/uuid-repeat.svg",
        "/icons/uuid-repeat-array.svg",
        "/icons/uuid-settings.svg"),

    /**
     * Basic icons.
     */
    Data("/icons/data.svg",
        "/icons/data-array.svg",
        "/icons/data-repeat.svg",
        "/icons/data-repeat-array.svg",
        "/icons/data-settings.svg");


    /**
     * The basic icon for the data type.
     */
    val Base = IconLoader.getIcon(base)
    /**
     * The array variant of the basic icon.
     */
    val Array = IconLoader.getIcon(array)
    /**
     * The repeat variant of the basic icon.
     */
    val Repeat = IconLoader.getIcon(repeat)
    /**
     * The repeat array variant of the basic icon.
     */
    val RepeatArray = IconLoader.getIcon(repeatArray)
    /**
     * The settings variant of the basic icon.
     */
    val Settings = IconLoader.getIcon(settings)
}
