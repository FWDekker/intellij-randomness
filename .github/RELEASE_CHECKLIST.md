# Release checklist
## Documentation
* Bump the version number according to [Semantic Versioning](https://semver.org/).
* Update [`README.md`](../README.md), [`CHANGELOG.md`](../CHANGELOG.md), and
  [`description.html`](../src/main/resources/META-INF/description.html) if necessary.
    * Make sure to preview the change notes in the IDE by loading the plugin.
    * Make sure that even subsections with a single item use bullet points, otherwise the entry will not show in the IDE.
    * Make sure the list of acknowledgements is up-to-date.
* Update screenshots and GIFs in `.github/img/` and on the plugin repository if necessary.
    * Set the global UI scale to 200% before recording/screenshotting to ensure high-resolution images.
    * Use the project in `src/test/resources/screenshots/` to store code snippets in.
      Do not store `.idea/`, `.gradle`, and similar build files in this project.
    * Hide (inlay) hints and set font size to 20.
    * Distance between bottom of "Refresh" button and top of button bar at bottom is 50 pixels, or the original
      distance, whichever is smaller.
    * On Linux, the screen can be recorded using [peek](https://github.com/phw/peek) or
      [SimpleScreenRecorder](https://www.maartenbaert.be/simplescreenrecorder/).
    * Reducing GIF size is a difficult process.
      The following seems to work fine:
      1. `for f in ./*.webm; do ffmpeg -y -i "$f" -vf "fps=10,scale=768:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" -loop 0 "${f%.*}.gif"; done`
      2. Go to [ezgif](https://ezgif.com/optimize) and upload the GIF to reduce in size.
      3. Apply the following optimisations in question; after each result, you can click "Optimize" to apply another filter:
         1. "Color Reduction" to 64 colours
         2. "Optimize Transparency" with 2% fuzz
         3. "Lossy GIF" with compression level 30
* Ensure documentation generates without errors.
  After the release, run the `cd.yml` workflow to update the `gh-pages` branch.

## Verification
* Run tests and static analysis one more time.
* Try out the plugin yourself and check that old and new features work properly.
* Run the plugin verifier.
    * Make sure the latest IDE version is in the plugin verifier's configuration.
* Ensure settings from the previous version correctly load into the new version.
