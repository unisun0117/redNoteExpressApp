## ADDED Requirements

### Requirement: Selectable article style templates

The system SHALL provide a dropdown list of article style templates including at minimum: retro style (复古风), minimalist style (简约风), humorous style (幽默风), and deep-review style (深度测评风). Selecting a style SHALL influence the tone, vocabulary, and formatting of the generated article.

#### Scenario: User selects a style template

- **WHEN** user selects "复古风" from the template dropdown and generates an article
- **THEN** the generated article uses vintage/retro vocabulary, classical phrasing patterns, and nostalgic emotional tone

#### Scenario: Style template changes between generations

- **WHEN** user switches from "简约风" to "幽默风" and re-generates with the same image and keywords
- **THEN** the two generated articles differ significantly in tone and word choice while preserving the same factual content

### Requirement: Selectable content track (赛道)

The system SHALL provide a dropdown list of content categories (赛道) including: food (美食), sports (运动), photography (摄影), pets (萌宠), home (家居), beauty (美妆), tech (数码), baby/parenting (母婴), and dining (餐饮). Selecting a category SHALL adjust the content angle and target reader persona.

#### Scenario: Same image, different track

- **WHEN** user uploads a photo of a plate of pasta and selects "美食" track
- **THEN** article focuses on taste, ingredients, and culinary experience
- **WHEN** same photo is used with "家居" track
- **THEN** article focuses on plating aesthetics, table setting, and home dining atmosphere

### Requirement: Toggle for viral section subtitles

The system SHALL provide a toggle option to include or exclude "爆款小标题" (viral section subtitles) before each of the three body paragraphs.

#### Scenario: Subtitles enabled

- **WHEN** user enables "爆款小标题" toggle and generates an article
- **THEN** each of the three body sections begins with a bold, attention-grabbing subtitle

#### Scenario: Subtitles disabled

- **WHEN** user disables "爆款小标题" toggle and generates an article
- **THEN** the three body sections flow as continuous paragraphs without separated subtitles

### Requirement: Style configuration persists during session

The system SHALL retain the user's selected style, track, emoji toggle, and subtitle toggle within the generation session so they do not need to re-select for each generation.

#### Scenario: Settings persist across multiple generations

- **WHEN** user sets style to "幽默风", track to "美食", emoji ON, subtitles ON, then generates article A
- **THEN** when user uploads new image and generates article B without changing settings, article B uses the same configuration
