# Env Config

## Purpose

管理 Uni-app 微信小程序的多环境配置（本地/测试/生产），通过 Vite 环境变量机制实现构建时的域名自动切换和类型安全的环境变量访问。

## Requirements

### Requirement: Environment variable files

The project SHALL contain three environment variable files for development, test, and production environments.

#### Scenario: Development env file exists
- **WHEN** checking the project root
- **THEN** `.env.development` SHALL exist with variables like `VITE_API_BASE_URL`, `VITE_APP_ENV`

#### Scenario: Test env file exists
- **WHEN** checking the project root
- **THEN** `.env.test` SHALL exist with test-specific values

#### Scenario: Production env file exists
- **WHEN** checking the project root
- **THEN** `.env.production` SHALL exist with production-specific values

### Requirement: Environment utility module

The project SHALL provide a `src/utils/env.ts` utility that exports typed environment configuration values.

#### Scenario: API base URL is accessible
- **WHEN** code imports `import { env } from '@/utils/env'`
- **THEN** `env.apiBaseUrl` SHALL return the correct URL for the current environment

#### Scenario: Current environment name is accessible
- **WHEN** code imports `import { env } from '@/utils/env'`
- **THEN** `env.mode` SHALL return `'development'`, `'test'`, or `'production'`

#### Scenario: isDevelopment helper works
- **WHEN** `env.isDevelopment` is accessed in development mode
- **THEN** it SHALL return `true`

### Requirement: Build-time environment injection

Vite SHALL inject the correct environment variables at build time based on the `--mode` flag.

#### Scenario: Dev build uses development variables
- **WHEN** `pnpm dev:mp-weixin` is run (default mode: development)
- **THEN** `import.meta.env.VITE_APP_ENV` SHALL equal `'development'`

#### Scenario: Test build uses test variables
- **WHEN** `pnpm build:mp-weixin --mode test` is run
- **THEN** `import.meta.env.VITE_APP_ENV` SHALL equal `'test'`

#### Scenario: Prod build uses production variables
- **WHEN** `pnpm build:mp-weixin --mode production` is run
- **THEN** `import.meta.env.VITE_APP_ENV` SHALL equal `'production'`

### Requirement: Package.json scripts for each environment

The `package.json` SHALL define scripts for dev and build targeting each environment.

#### Scenario: Dev script is defined
- **WHEN** checking `package.json` scripts
- **THEN** `"dev:mp-weixin"` SHALL run Uni-app dev mode for WeChat Mini Program

#### Scenario: Build scripts for each env are defined
- **WHEN** checking `package.json` scripts
- **THEN** `"build:mp-weixin:test"` SHALL build with `--mode test`
- **AND** `"build:mp-weixin:prod"` SHALL build with `--mode production`
