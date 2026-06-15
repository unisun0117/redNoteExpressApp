## Purpose

B2B merchant bulk upload and batch article generation workflow.

## Requirements

### Requirement: Batch image upload for merchant users

The system SHALL allow VIP/merchant users to upload up to 50 images in a single batch operation. Upload progress SHALL be displayed with per-image preview thumbnails.

#### Scenario: Merchant uploads 50 store images

- **WHEN** merchant user selects 50 images and initiates upload
- **THEN** system shows upload progress bar, displays thumbnail previews as each image completes, and confirms when all 50 are ready

#### Scenario: Upload exceeds limit

- **WHEN** user attempts to upload more than 50 images
- **THEN** system rejects the excess files and displays a message indicating the 50-image limit

### Requirement: Batch article generation from multiple images

The system SHALL accept a batch of uploaded images, apply a shared style configuration (template, track, emoji toggle), and generate one unique article per image. All 50 articles SHALL differ in their content descriptions while maintaining consistent brand tone.

#### Scenario: Generate 50 unique articles

- **WHEN** merchant with 50 uploaded food images clicks "Batch Generate" with "美食" track and "幽默风" style
- **THEN** system generates 50 articles, each describing a different image with unique phrasing, and displays them in a scrollable result list

#### Scenario: Batch generation progress indication

- **WHEN** batch generation is in progress (50 articles may take several minutes)
- **THEN** system displays a progress indicator showing "X/50 articles generated" with estimated time remaining

### Requirement: Batch results export and management

The system SHALL present all generated articles in a list view with copy-to-clipboard per article and a "Copy All" / "Export All" option. Users SHALL be able to view each article's full text by expanding a list item.

#### Scenario: Merchant copies all generated articles

- **WHEN** batch generation completes and user clicks "Export All"
- **THEN** system copies all 50 articles to clipboard with separators, or provides a downloadable text file

#### Scenario: Store name and address auto-filled in batch

- **WHEN** merchant has a saved store profile with name and address
- **THEN** all batch-generated articles include the store's name and address in the final section without requiring per-article input
