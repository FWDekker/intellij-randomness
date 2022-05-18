<h1 align="center">
    <img src=".github/img/logo.png" width="150"/>
    <br />
    Randomness
</h1>

<p align="center">
    <a href="https://plugins.jetbrains.com/plugin/9836-randomness"><img src="https://img.shields.io/github/release/FWDekker/intellij-randomness.svg?style=for-the-badge" alt="Release" /></a>
    <a href="https://fwdekker.github.io/intellij-randomness/"><img src="https://img.shields.io/badge/documentation-ready-blue.svg?style=for-the-badge" alt="Documentation" /></a>
    <br />
    <a href="https://github.com/FWDekker/intellij-randomness/actions?query=branch%3Amaster"><img src="https://img.shields.io/github/workflow/status/FWDekker/intellij-randomness/CI?style=for-the-badge" alt="GitHub build status" /></a>
    <a href="https://codecov.io/gh/FWDekker/intellij-randomness"><img src="https://img.shields.io/codecov/c/github/FWDekker/intellij-randomness.svg?style=for-the-badge" alt="Line coverage" /></a>
</p>

Rather than going to [random.org](https://www.random.org/) or making up your own random data, you can now insert random
numbers, strings, and UUIDs using an IntelliJ action!

This plugin is also available on the [plugin repository](https://plugins.jetbrains.com/plugin/9836-randomness).


## 📖 How to use
<img align="right" width="450px" src=".github/img/live-sample.gif" alt="Animated sample usage of Randomness." />

To insert random data, press <kbd>Alt + R</kbd> (<kbd>⌥R</kbd>) and choose the type of data you want to insert.
A different value will be inserted at each caret.

You can modify this behavior by holding a key while selecting the type of data to insert:
* **Array**: Hold <kbd>Shift</kbd> to insert a whole array of values.
* **Settings**: Hold <kbd>Ctrl</kbd> to open the settings of that data type.
* **Repeat**: Hold <kbd>Alt</kbd> (<kbd>⌥</kbd>) to insert the same value at each caret.

You can also hold multiple modifier keys to combine their effects.

Randomness can also be found in the main menu under <kbd>Tools</kbd> or in <kbd>Code > Generate</kbd>.

## ✨ Features
* 🕸 **Data Types**<br />
  There are five types of data that can be inserted:
  1. **Integers**, such as `7,826,922`, in any base from binary to hexatrigesimal.
  2. **Decimals**, such as `8,816,573.10`, using customisable separators.
  3. **Strings**, such as `"PaQDQqSBEH"`, with custom symbol lists.
  4. **Words**, such as `"Bridge"`, with custom word lists.
  5. **UUIDs**, such as `0caa7b28-fe58-4ba6-a25a-9e5beaaf8f4b`, with or without dashes.

  In addition to these data types, it's also possible to generate entire **arrays** of a data type.
  For example, an array of integers might look like `[978, 881, 118, 286, 288]`.
* ⚙ **Settings**<br />
  The way the data is generated can be **adjusted to your demands**.
  You can customise the smallest integer to generate, the quotation marks to surround strings with, the number of
  elements to put in an array, the decimal separator to use, the capitalisation of strings and UUIDs, and much more.
* 👀 **Previews**<br />
  To **help you decide** what settings to choose, a preview of the data that is generated with your current settings is
  shown at the bottom of the settings window.
* 💨 **Shortcuts**<br />
  Instead of using up all your shortcuts, Randomness only uses the <kbd>Alt + R</kbd> (or <kbd>⌥R</kbd>) shortcut by
  default.
  However, you have the option to assign a unique shortcut to each individual data type and each settings window to
  **streamline your workflow**.
* 💬 **Symbol sets**<br />
  While Randomness comes with a varied selection of symbols to use for generating strings, it also gives you the option
  to **add your own symbols**, with support for Chinese symbols and emoji.
* 📚 **Dictionaries**<br />
  Randomness is bundled with a small English dictionary from which it chooses random words.
  However, you may want to add random words from another language or insert random movie quotes.
  You can **create your own dictionaries** by creating a text file and putting one option on each line, and saving the
  file with the `.dic` extension.
  Empty lines and lines that start with a `#` are ignored.

  <p align="center"><img src=".github/img/word-settings.png" alt="Dictionary settings" /></p>


## 💻 Development
This section contains instructions in case you want to build the plugin from source or want to help with development.
Please also check the [contribution guidelines](.github/CONTRIBUTING.md).

### 🔨 Build/run
```bash
$ gradlew runIde       # Open a sandbox IntelliJ instance running the plugin
$ gradlew buildPlugin  # Build an installable zip of the plugin
```

### 🧪 Quality assurance
```bash
$ gradlew test                # Run tests
$ gradlew test --tests X      # Run tests in class X
$ gradlew check               # Run tests and static analysis
$ gradlew jacocoTestReport    # Run tests and calculate coverage
$ gradlew runPluginVerifier   # Check for compatibility issues
```

### 📚 Documentation
```bash
$ gradlew dokkaHtml  # Generate documentation
```

### 🖼 Icons
The icons used by the plugin are found in the [.sketch](.github/img/icons.sketch) file.
You can open this file with [Sketch](https://www.sketch.com/) (macOS), [Lunacy](https://icons8.com/lunacy) (Windows), or
[Figma](https://github.com/Figma-Linux/figma-linux) (Linux).


## 🙏 Acknowledgements
In chronological order of contribution:
* Thanks to [Casper Boone](https://github.com/casperboone) for
  [reporting a bug](https://github.com/FWDekker/intellij-randomness/issues/25) and for
  [suggesting emoji support](https://github.com/FWDekker/intellij-randomness/issues/192)!
* Thanks to [Victor Tyazhelnikov](https://github.com/zenwarr) for
  [suggesting the array data type](https://github.com/FWDekker/intellij-randomness/issues/54)!
* Thanks to [Georgios Andreadis](https://github.com/gandreadis) for the
  [original logo](https://github.com/FWDekker/intellij-randomness/pull/86)!
* Thanks to [Oleksii K.](https://github.com/ok3141) for
  [suggesting the UUID data type](https://github.com/FWDekker/intellij-randomness/issues/88) and for
  [suggesting the hex symbol set](https://github.com/FWDekker/intellij-randomness/issues/89)!
* Thanks to [Meilina Reksoprodjo](https://github.com/meilinar) for help with macOS user testing!
* Thanks to [Wouter van Vliet](https://github.com/woutervanvliet) for
  [helping me clarify the "repeat" action](https://github.com/FWDekker/intellij-randomness/issues/307)!
* Thanks to [Paweł Lipski](https://github.com/PawelLipski) for
  [reporting two](https://github.com/FWDekker/intellij-randomness/issues/328)
  [bugs in the verification script](https://github.com/FWDekker/intellij-randomness/issues/332)!
* Thanks to [opticyclic](https://github.com/opticyclic) for
  [suggesting two](https://github.com/FWDekker/intellij-randomness/issues/338)
  [improvements to the verification script](https://github.com/FWDekker/intellij-randomness/issues/339)!
* Thanks to [Niraj Jadhav](https://github.com/niraj-toad) for
  [reporting a bug and demonstrating that the error reporter works](https://github.com/FWDekker/intellij-randomness/issues/343)!
* Thanks to [jrborases](https://github.com/jrborases) for
  [suggesting a configurable popup](https://github.com/FWDekker/intellij-randomness/issues/305#issuecomment-661530711)!
* Thanks to [Alex Pernot](https://github.com/AlexPernot) for
  [participating in the data type discussion](https://github.com/FWDekker/intellij-randomness/issues/305#issuecomment-662499058)!
* Thanks to [solonovamax](https://github.com/solonovamax) for
  [his contributions](https://github.com/FWDekker/intellij-randomness/issues/305#issuecomment-804415489)
  [to the data type discussion](https://github.com/FWDekker/intellij-randomness/issues/363#issuecomment-804976008)
  [in several places](https://github.com/FWDekker/intellij-randomness/issues/365#issuecomment-805052007),
  [suggesting the removal of the integer range limit](https://github.com/FWDekker/intellij-randomness/issues/367), and
  [suggesting the byte preset](https://github.com/FWDekker/intellij-randomness/issues/368)!
* Thanks to [Martin Kaspar van Laak](https://github.com/MartinKvL) for
  [reporting that symbol sets didn't get saved](https://github.com/FWDekker/intellij-randomness/issues/382)!
* Thanks to [Aleksey Bobyr](https://github.com/Alexsey) for
  [reporting a critical UI bug in WebStorm EAP](https://github.com/FWDekker/intellij-randomness/issues/418)!
* Thanks to [LukasAppleFan](https://github.com/LukasAppleFan) for
  [helping me find a bug in IntelliJ](https://github.com/FWDekker/intellij-randomness/issues/421)!
* Thanks to [Xiakitl](https://github.com/Xiakitl) for
  [reporting a bug with custom shortcuts](https://github.com/FWDekker/intellij-randomness/issues/423)!
* Thanks to [Rishi Maharaj](https://github.com/rshmhrj) for
  [suggesting to add prefix and postfix options to strings]!

If I should add, remove, or change anything here, just open an issue or email me!
