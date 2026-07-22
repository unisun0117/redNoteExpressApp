## ADDED Requirements

### Requirement: Initialize project with pnpm + Vite + Vue3 + TypeScript
The system SHALL provide a project scaffold initialized with pnpm as the package manager, using Vite as the build tool, Vue 3 with Composition API, and TypeScript for type safety.

#### Scenario: Project can be installed and started
- **WHEN** developer runs `pnpm install` followed by `pnpm dev`
- **THEN** the Vite dev server starts successfully on the configured port and a blank Vue3 app renders in the browser

#### Scenario: TypeScript compilation succeeds
- **WHEN** developer runs `pnpm build`
- **THEN** the project compiles without TypeScript errors and produces production-ready output in the `dist/` directory

### Requirement: Environment variable configuration
The system SHALL provide `.env.development`, `.env.test`, and `.env.production` configuration files with the `VITE_API_BASE_URL` variable for each environment.

#### Scenario: Development environment auto-detected
- **WHEN** developer runs `pnpm dev`
- **THEN** the app loads with `VITE_API_BASE_URL` pointing to the local development API

#### Scenario: Production build uses production API
- **WHEN** developer runs `pnpm build` followed by `pnpm preview`
- **THEN** the app uses the production `VITE_API_BASE_URL` for API requests

### Requirement: ESLint and Prettier integration
The system SHALL include ESLint with Vue3/TypeScript rules and Prettier for code formatting consistency.

#### Scenario: Lint check runs successfully
- **WHEN** developer runs `pnpm lint`
- **THEN** ESLint checks all `.vue`, `.ts`, `.tsx` files and reports issues
