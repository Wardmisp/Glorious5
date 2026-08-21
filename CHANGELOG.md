# Changelog

All notable changes to this project will be documented in this file.

## [0.0.2-beta] - 2026-08-21

### Added
- **Scouting Report Screen**: Detailed analysis before simulation including win probability, team comparisons (radar chart/bars), and positional matchup analysis.
- **LED Gym Clock**: Realistic 7-segment digital clock using Canvas API for match simulation.
- **Match Time Simulation**: Integrated timestamps into match actions (3 per quarter).
- **Difficulty Settings**: New slider in options to adjust starting budgets (Beginner/Normal/Difficult).
- **Sound Toggle**: Ability to enable/disable sound effects in the options menu.
- **App Icon**: Updated launcher icon with the new Glorious5 logo.
- **Audio**: Added whistle sound at the start of each quarter.

### Fixed
- **Name Normalization**: Fixed display of special characters in names (e.g., Jokic, Doncic).
- **Auction Logic**: Corrected price display and logic for "Pass" and "Collect" actions (minimum $1).
- **Build System**: Fixed SAXParseException caused by leading whitespace in XML resources.

## [0.0.1] - 2026-08-20

### Added
- Initial version of Basketball Draft Simulation.
- AI Opponent with strategic bidding logic.
- Percentile-based scoring system using 300 historic NBA seasons.
- Progressively revealed player stats during auctions.
- Sound effects for auction events and results.
- Dark and Light theme support with manual toggle in options.
- Dynamic team management and budget tracking.
- Win probability calculation based on team composition.

### Fixed
- Fixed issue where players were not added to teams correctly when passing.
- Fixed UI bugs in status bars and card clippings.
- Resolved various build and dependency issues.
