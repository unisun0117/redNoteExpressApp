## Purpose

User registration, login, and account management with freemium tier differentiation.

## Requirements

### Requirement: User registration with email or WeChat OAuth

The system SHALL allow users to register using email/password combination or WeChat OAuth login. Registered users SHALL receive a free tier account with 5-10 free generation credits.

#### Scenario: Email registration

- **WHEN** user completes registration form with valid email and password
- **THEN** system creates account, sends verification email, and grants 10 free generation credits upon email verification

#### Scenario: WeChat OAuth registration

- **WHEN** user authenticates via WeChat OAuth for the first time
- **THEN** system creates account linked to WeChat OpenID and grants 10 free generation credits immediately

### Requirement: User login and session management

The system SHALL authenticate users via JWT access tokens (short-lived, 15 minutes) and refresh tokens (long-lived, 7 days). Protected API endpoints SHALL reject requests with missing or expired tokens.

#### Scenario: Successful login

- **WHEN** user provides valid credentials
- **THEN** system returns access token and refresh token, and user can access protected endpoints

#### Scenario: Expired access token

- **WHEN** user makes request with expired access token
- **THEN** system returns 401 Unauthorized, and client uses refresh token to obtain new access token

### Requirement: Tier-based feature access control

The system SHALL differentiate between free and VIP users. Free users SHALL have access to basic styles, mandatory emoji, and fixed generation count. VIP users SHALL have access to all style templates, emoji toggle, batch generation, viral analysis, and copy-with-formatting features.

#### Scenario: Free user attempts VIP feature

- **WHEN** free-tier user attempts to use batch generation or viral article analysis
- **THEN** system returns an error with a prompt to upgrade to VIP

#### Scenario: VIP user accesses all features

- **WHEN** VIP user uses any feature (batch generation, viral analysis, all styles, emoji toggle)
- **THEN** system allows access to all features without restriction
