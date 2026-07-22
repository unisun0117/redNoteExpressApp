## ADDED Requirements

### Requirement: Main package pages

The `pages.json` SHALL define the main package with only core pages: home page and login page.

#### Scenario: Main package contains only essential pages
- **WHEN** checking `pages.json` pages array
- **THEN** it SHALL contain exactly: `pages/index/index` (home) and `pages/login/index` (login)
- **AND** the first entry SHALL be `pages/index/index` as the app entry

#### Scenario: Main package has tabBar styling
- **WHEN** the app renders the home page
- **THEN** a tabBar SHALL be visible (if configured) or page style SHALL be set appropriately

### Requirement: SubPackages structure

The `pages.json` SHALL define `subPackages` array with independent sub-package entries for each business domain.

#### Scenario: Order sub-package is defined
- **WHEN** checking `pages.json` subPackages
- **THEN** an entry with `root: "sub-pages/order"` SHALL exist
- **AND** it SHALL contain placeholder page(s) for the order module

#### Scenario: User sub-package is defined
- **WHEN** checking `pages.json` subPackages
- **THEN** an entry with `root: "sub-pages/user"` SHALL exist

#### Scenario: Goods sub-package is defined
- **WHEN** checking `pages.json` subPackages
- **THEN** an entry with `root: "sub-pages/goods"` SHALL exist

#### Scenario: Setting sub-package is defined
- **WHEN** checking `pages.json` subPackages
- **THEN** an entry with `root: "sub-pages/setting"` SHALL exist

### Requirement: Preload rule for sub-packages

The `pages.json` SHALL configure preload rules so that certain sub-packages are preloaded when the main package is ready.

#### Scenario: Preload rule exists
- **WHEN** checking `pages.json` preloadRule
- **THEN** `pages/index/index` SHALL have a `preloadRule` that preloads at least the `sub-pages/user` sub-package

### Requirement: Placeholder pages in sub-packages

Each sub-package SHALL contain at least one placeholder page so the sub-package is valid and ready for development.

#### Scenario: Each sub-package has a placeholder
- **WHEN** navigating to any sub-package root directory
- **THEN** a placeholder `.gitkeep` or `index.vue` file SHALL exist
- **AND** the page path in `pages.json` SHALL point to the correct file location

### Requirement: Sub-package directory structure

Each sub-package directory SHALL follow a consistent structure with `pages/` subdirectory for its pages.

#### Scenario: Directory structure is consistent
- **WHEN** examining `src/sub-pages/order/`
- **THEN** `pages/` subdirectory SHALL exist for order-specific pages
- **AND** optional `components/` for order-specific components
- **AND** optional `api/` for order-specific API calls
