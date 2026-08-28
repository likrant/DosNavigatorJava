# Legacy DN UI Reference

This bundle is the Java UI-test baseline for the Russian legacy DN fixture.

- `screenshots/forms`: validated form captures.
- `screenshots/menus`: top-level menu captures.
- `screen-index.json`: image dimensions, sampled RGB palette, expected 160x50 text grid, and the complete menu/dialog index.
- `resources/russian-ui-resources.dnr`: UTF-8 decoded authoritative resource source. `MENU` and `DIALOG` entries provide labels, hotkeys, pseudographics, and text-cell coordinates; `~x~` marks a mnemonic.

The `logicalClient` rectangle in each screen excludes Windows chrome. `screenContracts` links each captured form to its DN command and, where statically declared, to the exact `DIALOG` id and text-cell origin. The renderer uses the DOS frame primitives from the resource/UI implementation; compare the resulting glyphs as cells, not anti-aliased pixels.

Do not OCR the screenshots during Java testing. Assert the Java text grid, component bounds, colors, and resource-derived labels against `screen-index.json` and `russian-ui-resources.dnr`; use PNGs only for pixel-level visual regression.