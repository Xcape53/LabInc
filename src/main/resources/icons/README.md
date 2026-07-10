# LabInc Icon Resources

Place game interface icons in this directory as SVG or PNG files. The resource loader prefers SVG and falls back to PNG.

## Expected files

| File | Suggested size | Purpose |
| --- | ---: | --- |
| `mining.svg` or `.png` | 24x24 | Mining view |
| `factory.svg` or `.png` | 24x24 | Factory view |
| `market.svg` or `.png` | 24x24 | Market view |
| `achievements.svg` or `.png` | 24x24 | Achievements view |
| `settings.svg` or `.png` | 20x20 | Settings |
| `save.svg` or `.png` | 20x20 | Manual save |
| `help.svg` or `.png` | 20x20 | Help |
| `logo.svg` or `.png` | 48x48 | Application logo |
| `money.svg` or `.png` | 18x18 | Current balance |
| `income.svg` or `.png` | 18x18 | Income rate |
| `prestige.svg` or `.png` | 18x18 | Prestige |
| `sound_on.svg` or `.png` | 16x16 | Sound enabled |
| `sound_off.svg` or `.png` | 16x16 | Sound disabled |
| `autosave.svg` or `.png` | 16x16 | Autosave status |

## Format guidelines

- Prefer SVG with an explicit `viewBox`.
- Use transparent PNG files when SVG is not available.
- Keep the design flat and readable at the target size.
- Avoid embedded raster images inside SVG files.
- Confirm the license of every third-party icon before adding it.

The loader checks packaged resources first and source-tree resources as a development fallback.
