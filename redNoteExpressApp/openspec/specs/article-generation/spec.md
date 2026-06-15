## Purpose

Core LLM-powered article generation from image + keyword input, producing structured Xiaohongshu-formatted output with title, intro, body sections, summary, and location info.

## Requirements

### Requirement: Generate article from image and keywords

The system SHALL accept one or more user-uploaded images and keyword text, analyze the image content via vision API, and generate a complete Xiaohongshu-formatted article with the constrained structure: title (18-20 characters), introduction (~50 characters), three body sections (each with subtitle + ~100 characters), summary (~50 characters), and store name/address (~50 characters). Total output SHALL be approximately 500 characters.

#### Scenario: User generates an article with an image and keywords

- **WHEN** user uploads a food photo and enters keywords "coffee shop brunch recommendation"
- **THEN** system returns a complete article with all 7 sections (title through store info), each section within the specified character limits

#### Scenario: Image upload fails or is invalid

- **WHEN** user uploads a file that is not a valid image format (JPG, PNG, WEBP)
- **THEN** system returns an error message indicating accepted image formats

### Requirement: Enforce article structure and word count constraints

The system SHALL enforce the following word count constraints on generated output:
- Title: 18-20 characters
- Introduction: ~50 characters
- Each body section subtitle + content: ~100 characters per section (×3)
- Summary: ~50 characters
- Store name/address: ~50 characters

#### Scenario: Generated article respects word count limits

- **WHEN** the LLM generates an article
- **THEN** each section's character count falls within ±15% of the target constraint

#### Scenario: Word count validation flags off-target output

- **WHEN** a generated section exceeds 150% of its target word count
- **THEN** system re-generates that specific section before returning the final article

### Requirement: Article must include live-person tone and emoji support

The system SHALL generate content with "活人感" (live-person, authentic tone) including conversational language, emotional expressions, and relevant emoji placement when emoji option is enabled.

#### Scenario: Generate article with emoji enabled

- **WHEN** user selects emoji toggle ON
- **THEN** generated body sections contain at least 2-3 relevant emoji each, integrated naturally into sentences

#### Scenario: Generate article with emoji disabled

- **WHEN** user selects emoji toggle OFF
- **THEN** generated article contains no emoji characters in any section
