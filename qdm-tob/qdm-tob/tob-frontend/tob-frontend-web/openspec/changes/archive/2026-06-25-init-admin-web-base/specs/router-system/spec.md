## ADDED Requirements

### Requirement: Vue Router installation with history mode
The system SHALL install and configure Vue Router with HTML5 History mode (`createWebHistory`) as the default routing strategy.

#### Scenario: App renders router-view content
- **WHEN** user navigates to any defined route
- **THEN** the corresponding page component renders inside the `<router-view>` element

### Requirement: Home page route
The system SHALL define a `/` route that lazily loads the Home page component and renders within the default layout.

#### Scenario: User visits root path
- **WHEN** user navigates to `/`
- **THEN** the Home page component is displayed within the default admin layout (sidebar + header + content area)

### Requirement: Login page route
The system SHALL define a `/login` route that lazily loads the Login page component and renders WITHOUT the default layout (full-screen standalone page).

#### Scenario: User visits login path
- **WHEN** user navigates to `/login`
- **THEN** the Login page component is displayed as a standalone full-screen page, without the admin layout wrapper

### Requirement: 404 catch-all route
The system SHALL define a catch-all route `/:pathMatch(.*)*` that displays a 404 Not Found page.

#### Scenario: User visits undefined path
- **WHEN** user navigates to a route that does not exist
- **THEN** a 404 Not Found page is displayed

### Requirement: Route-level lazy loading
All page components in the route definitions SHALL use dynamic `import()` for lazy loading to enable code splitting.

#### Scenario: Page component loaded on demand
- **WHEN** user navigates to a route for the first time
- **THEN** the corresponding page component chunk is loaded asynchronously from the server

### Requirement: Dynamic route slot for business modules
The router configuration SHALL support runtime route registration via `router.addRoute()` for business modules. Module route definitions in `src/router/modules/` SHALL be importable and registrable at app initialization.

#### Scenario: Business module registers its routes
- **WHEN** a business module route file is imported and added via `router.addRoute()`
- **THEN** the new routes become accessible without modifying the core router configuration

### Requirement: Navigation guard for authentication
The router SHALL include a global `beforeEach` navigation guard that checks for the presence of the auth token in localStorage. Non-authenticated users SHALL be redirected to `/login` when accessing protected routes.

#### Scenario: Unauthenticated user tries to access protected route
- **WHEN** user navigates to a protected route (non-login) without a valid token in localStorage
- **THEN** the navigation is redirected to `/login`

#### Scenario: Authenticated user accesses protected route
- **WHEN** user navigates to a protected route with a valid token in localStorage
- **THEN** the navigation proceeds normally

#### Scenario: Authenticated user visits login page
- **WHEN** user navigates to `/login` with a valid token already in localStorage
- **THEN** the navigation is redirected to `/` (home page)
