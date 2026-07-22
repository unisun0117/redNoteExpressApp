## ADDED Requirements

### Requirement: Request instance creation with luch-request

The system SHALL create and export a pre-configured luch-request instance as the single HTTP client.

#### Scenario: Request instance is importable
- **WHEN** a module imports `import { http } from '@/api/request'`
- **THEN** the `http` instance SHALL be a configured luch-request object
- **AND** it SHALL support `http.get()`, `http.post()`, `http.put()`, `http.delete()` methods

#### Scenario: Default configuration is applied
- **WHEN** the request instance is created
- **THEN** `baseURL` SHALL be set from the environment configuration
- **AND** default timeout SHALL be set (e.g., 15000ms)
- **AND** default headers SHALL include `Content-Type: application/json`

### Requirement: Multi-environment domain switching

The HTTP client SHALL automatically use the correct API base URL based on the current build environment (development / test / production).

#### Scenario: Development environment uses local domain
- **WHEN** running in development mode (`pnpm dev:mp-weixin`)
- **THEN** all requests SHALL target the development base URL (e.g., `http://localhost:8080`)

#### Scenario: Test environment uses test domain
- **WHEN** building for test (`pnpm build:mp-weixin --mode test`)
- **THEN** all requests SHALL target the test base URL (e.g., `https://test-api.example.com`)

#### Scenario: Production environment uses production domain
- **WHEN** building for production (`pnpm build:mp-weixin --mode production`)
- **THEN** all requests SHALL target the production base URL (e.g., `https://api.example.com`)

### Requirement: Request interceptor

The HTTP client SHALL execute global request interceptors before each request is sent.

#### Scenario: Token is injected into request headers
- **WHEN** a request is about to be sent
- **AND** a valid token exists in the user store
- **THEN** the `Authorization` header SHALL be set to `Bearer <token>`

#### Scenario: Request proceeds without token when not authenticated
- **WHEN** a request is about to be sent
- **AND** no token exists in the user store
- **THEN** the request SHALL proceed without an `Authorization` header

#### Scenario: Loading indicator is shown
- **WHEN** a request is about to be sent
- **AND** the request config has `showLoading: true`
- **THEN** a loading indicator SHALL be displayed via `uni.showLoading()`

### Requirement: Response interceptor

The HTTP client SHALL execute global response interceptors to handle responses uniformly.

#### Scenario: Successful response is unwrapped
- **WHEN** a response is received with `statusCode: 200` and `data.code: 0`
- **THEN** the interceptor SHALL return `response.data` to the caller

#### Scenario: Business error triggers toast
- **WHEN** a response is received with `data.code !== 0`
- **THEN** the interceptor SHALL display a toast with the error message via `uni.showToast()`
- **AND** the Promise SHALL be rejected with the error

#### Scenario: 401 unauthorized redirects to login
- **WHEN** a response is received with `statusCode: 401`
- **THEN** the interceptor SHALL clear the token from store
- **AND** redirect to the login page via `uni.reLaunch({ url: '/pages/login/index' })`

#### Scenario: Network error is handled gracefully
- **WHEN** a request fails due to network error
- **THEN** the interceptor SHALL display "网络异常，请检查网络连接" toast
- **AND** the Promise SHALL be rejected with the network error

### Requirement: Modular API export

API modules SHALL be organized by domain and exported from a unified entry point for easy import.

#### Scenario: API module is importable by domain
- **WHEN** a page needs user-related APIs
- **THEN** it SHALL be able to `import { userApi } from '@/api'`
- **AND** call methods like `userApi.login(data)`, `userApi.getUserInfo()`

#### Scenario: New API module follows the same pattern
- **WHEN** a developer creates a new API module (e.g., `src/api/modules/order.ts`)
- **THEN** it SHALL be added to `src/api/index.ts` barrel export
- **AND** SHALL be accessible via `import { orderApi } from '@/api'`
