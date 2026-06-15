## Purpose

Token/points recharge system, VIP subscription tiers, and payment integration.

## Requirements

### Requirement: Credit/points system with free starter quota

The system SHALL implement a credit-based generation quota. Each article generation SHALL consume 1 credit. New users SHALL receive 5-10 free credits upon registration. Users SHALL see their remaining credit balance in the UI.

#### Scenario: New user generates with free credits

- **WHEN** new user with 10 credits generates an article
- **THEN** credit balance decreases to 9 and generation succeeds

#### Scenario: User exhausts all credits

- **WHEN** user with 0 credits attempts to generate an article
- **THEN** system prompts user to recharge credits or subscribe to VIP

### Requirement: Credit recharge via payment

The system SHALL provide credit recharge packages purchasable via integrated payment providers. Minimum viable packages: 100 credits for ¥9.9, monthly unlimited for ¥29.9/month, quarterly unlimited for ¥79.9/quarter.

#### Scenario: User purchases credit package

- **WHEN** user selects "100 credits / ¥9.9" package and completes payment
- **THEN** system adds 100 credits to user's balance and records the transaction

### Requirement: VIP subscription with recurring billing

The system SHALL support monthly and quarterly VIP subscription plans with auto-renewal. VIP status SHALL unlock all premium features. Subscription status SHALL be checked on each authenticated request.

#### Scenario: User subscribes to monthly VIP

- **WHEN** user subscribes to ¥29.9/month VIP plan and payment succeeds
- **THEN** user account is upgraded to VIP tier immediately, and subscription end date is set to 30 days from now

#### Scenario: VIP subscription expires

- **WHEN** user's VIP subscription reaches end date without renewal
- **THEN** user account reverts to free tier and premium features become inaccessible

### Requirement: Transaction history and receipts

The system SHALL record all credit purchases and subscription payments as transactions with provider, amount, status, and timestamp. Users SHALL be able to view their transaction history.

#### Scenario: User views transaction history

- **WHEN** VIP user navigates to billing history page
- **THEN** system displays chronological list of all transactions with amounts, dates, and statuses
