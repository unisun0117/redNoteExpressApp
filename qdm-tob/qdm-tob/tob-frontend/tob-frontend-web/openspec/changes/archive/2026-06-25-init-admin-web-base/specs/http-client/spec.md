## ADDED Requirements

### Requirement: Axios instance with multi-environment baseURL
The system SHALL create an Axios instance that automatically selects `baseURL` from `import.meta.env.VITE_API_BASE_URL` based on the current environment.

#### Scenario: Development env uses local API
- **WHEN** app runs in development mode
- **THEN** all API requests use the baseURL from `.env.development`

#### Scenario: Production env uses production API
- **WHEN** app runs in production mode
- **THEN** all API requests use the baseURL from `.env.production`

### Requirement: Request interceptor injects Authorization token
The system SHALL include a request interceptor that reads the token from localStorage (key: `TOKEN_KEY`) and injects it into the `Authorization` header as `Bearer <token>`.

#### Scenario: Token exists in localStorage
- **WHEN** an API request is initiated AND a valid token exists in localStorage
- **THEN** the request includes the `Authorization: Bearer <token>` header

#### Scenario: No token in localStorage
- **WHEN** an API request is initiated AND no token exists in localStorage
- **THEN** the request proceeds without an `Authorization` header

### Requirement: Response interceptor handles business errors
The system SHALL include a response interceptor that checks the response status and data structure, displaying error messages via `ElMessage.error` for non-200 responses.

#### Scenario: API returns business error code
- **WHEN** an API response has `code !== 200` (or equivalent business success code)
- **THEN** the system displays the error message using `ElMessage.error` and rejects the promise

#### Scenario: API returns network error
- **WHEN** an API request fails due to network error
- **THEN** the system displays a generic network error message using `ElMessage.error`

### Requirement: 401 response clears token and redirects to login
The system SHALL detect HTTP 401 responses in the response interceptor — it MUST clear the token from localStorage and redirect the browser to `/login` using Vue Router.

#### Scenario: API returns 401 Unauthorized
- **WHEN** an API response has HTTP status 401
- **THEN** the system clears the `TOKEN_KEY` from localStorage and navigates to the `/login` route

#### Scenario: 401 on login page does not redirect loop
- **WHEN** an API response has HTTP status 401 AND the current route is already `/login`
- **THEN** the system clears the token but does NOT redirect again to `/login`

### Requirement: Request configurable timeout and headers
The Axios instance SHALL be created with a configurable timeout (default 30 seconds) and default `Content-Type: application/json` header.

#### Scenario: Request exceeds timeout
- **WHEN** an API request takes longer than the configured timeout
- **THEN** the request is aborted and a timeout error message is displayed
