# Changelog

All notable changes to this project will be documented in this file.

## [0.0.4-beta] - 2026-08-29

### Added
- **Online Multiplayer**: Real-time auction draft against another player over the network (Supabase-backed), with a lobby to create a match, browse open ones, or join via a shared invite code.
- **Turn Timer**: 15-second countdown per bid, resolved server-side on timeout (awarded to the leading bidder, or free to the opponent if nobody opened), with device clock-skew correction so the countdown stays accurate regardless of the phone's system clock.
- **Auction Result Buffer**: Each pick (won bid, pass, timeout, or automatic assignment) now pauses on a result screen showing the player and who got them, instead of jumping straight to the next round.
- **Post-Draft Scouting & Simulation**: Online matches now go through the same scouting report and quarter-by-quarter simulation as local mode before revealing the server-decided winner.
- **Lobby Expiration**: Waiting matches disappear from the lobby (and can no longer be joined) after 10 minutes with no opponent.
- **Rebranding**: App renamed from "AndroidIdea" to "Glorious 5" (launcher name and project name).

### Fixed
- **Progressive Reveal Online**: Player stats now reveal gradually with each bid during online auctions, matching local mode instead of showing everything immediately.
- **Bid Input Reset**: The bid amount you're entering is no longer silently wiped by a background refresh landing mid-edit.
- **Online Team Size**: Fixed at 5 players per team, removing an inconsistent "team size" option that had no equivalent in local mode.

## [0.0.3-beta] - 2026-08-26

### Added
- **Interactive Tutorial**: Comprehensive guide for new players with an overlay system, highlighting key UI elements (Auction, Teams, Scouting).
- **Auto-Recruitment**: Automated player attribution when a team is full or in uncontested scenarios.
- **Enhanced Scouting Analysis**: Improved player impact assessment with gauges and refined attack/dominance scoring logic.
- **Home Screen Redesign**: New visual polish and slogans for the "Glorious 5" rebranding.
- **Preview Support**: Added `@Preview` support for main screens to facilitate UI development.

### Fixed
- **Auction Timer**: Relocated timer logic to `LaunchedEffect` for better lifecycle management (stops timer when leaving the screen).
- **Budget Integrity**: Added checks to prevent negative budgets during intense bidding.
- **Draft Flow**: Protected "Pass" button to avoid accidental clicks and improved transitions between rounds.
- **UI Polish**: Removed redundant components (`StatusBar.kt`) and fixed layout issues on various screen sizes.
- **Scoring Formulas**: Updated attack and dominance calculation logic for more realistic match outcomes.

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
