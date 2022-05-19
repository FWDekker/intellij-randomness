# Release checklist
## Documentation
* Bump the version number according to [Semantic Versioning](https://semver.org/).
* Update `README.md`, `change-notes.html`, and `description.html` if necessary.
  * Make sure to preview the change notes in the IDE by loading the plugin.
* Update screenshots and GIFs in `.github/img/` and on the plugin repository if necessary.
  * Distance between bottom of "Refresh" button and top of button bar at bottom is 50 pixels, or the original distance,
    whichever is smaller.
  * On Linux, the screen can be recorded using [peek](https://github.com/phw/peek).
* Ensure documentation generates without errors and push documentation to `gh-pages` branch.

## Verification
* Run tests and static analysis one more time.
* Try out the plugin yourself and check that old and new features work properly.
* Run the plugin verifier.
  * Make sure the latest IDE version is in the plugin verifier's configuration.
* Ensure settings from the previous version correctly load into the new version.
