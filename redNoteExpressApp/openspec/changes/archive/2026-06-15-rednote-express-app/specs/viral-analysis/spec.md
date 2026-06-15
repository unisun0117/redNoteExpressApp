## ADDED Requirements

### Requirement: Accept viral article URL or text for style analysis

The system SHALL provide an input field where users can paste either a URL to a trending Xiaohongshu article or the full text of an article. The system SHALL extract the language style characteristics from the provided content.

#### Scenario: User pastes a viral article URL

- **WHEN** user pastes a valid Xiaohongshu article URL and clicks "Analyze Style"
- **THEN** system fetches the article content, extracts its text, and returns a style analysis summary

#### Scenario: User pastes raw article text

- **WHEN** user pastes article text directly into the input field and clicks "Analyze Style"
- **THEN** system analyzes the text and returns a style analysis summary without attempting URL fetch

#### Scenario: URL fetch fails

- **WHEN** user pastes a URL that is inaccessible or returns an error
- **THEN** system displays a graceful error message and prompts user to paste the article text directly instead

### Requirement: Extract and characterize writing style from reference article

The system SHALL analyze the provided article and extract a structured style profile including: tone (formal/casual/humorous/etc.), average sentence length, emoji density and patterns, hook/openings patterns, paragraph structure, and characteristic phrases or expressions.

#### Scenario: Style profile extraction

- **WHEN** system analyzes a viral food review article
- **THEN** the extracted style profile contains tone, sentence length distribution, emoji usage patterns, and identifiable hook structures from the reference

### Requirement: Apply extracted style to new article generation

The system SHALL merge the extracted viral article style profile with the user's image and keyword inputs to generate a new article that mimics the reference style while using the user's own content.

#### Scenario: Generate article in viral style

- **WHEN** user has analyzed a viral article (humorous tone, frequent emoji, short punchy sentences) and generates a new article with their own coffee shop image
- **THEN** the generated article adopts the humorous tone, similar emoji density, and short punchy sentence patterns while describing the user's coffee shop content

#### Scenario: Viral style overrides manual style selection

- **WHEN** user has analyzed a viral article AND the system has an active style profile
- **THEN** the viral style profile takes precedence over any manually selected style template for the next generation
