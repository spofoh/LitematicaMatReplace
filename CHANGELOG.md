# Changelog

## [0.1.0]
### Added

- Added a Litematica-styled `Replace` button beside `Ignore` on supported
  schematic and placement material-list rows.
- Added a live fuzzy-search block picker with a scrollable grid of Minecraft's
  rendered 3D block-item models.
- Added the LMR mod icon to Fabric metadata.
- Added README walkthrough screenshots for the material list and replacement
  search.
- Added linked dependency and installation resources and reformatted the README
  into a shorter visual guide.
- Added a confirmation screen showing the source and target materials and the
  exact count of affected schematic positions.
- Added a mandatory per-operation choice to overwrite the original
  `.litematic` file or export a new file beside it.
- Added atomic save-before-live-mutation handling, default-state replacement
  across every matching region position, active-placement rebuilds, and
  automatic material-list regeneration.
- Added session-level crash isolation for the feature's row hook, screen
  lifecycle, rendering, inputs, and save/apply actions.
- Added separate Minecraft 26.1, 26.1.1, 26.1.2, and 26.2 Fabric build targets
  using current compatible Litematica and MaLiLib dependency lines.

### Fixed

- Fixed the block-picker search field failing to apply typed queries to the
  visible grid.
### Changed

- Material replacements can now be queued in one editing session and saved
  together, so the overwrite/export choice is only made once at the end.
- All actions use Minecraft's native button sprites and therefore follow the
  active resource pack.
- Material-list rows already present in the queue are marked as `Queued` and
  show their target block on hover; clicking them allows changing the target.
