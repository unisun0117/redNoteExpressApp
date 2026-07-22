# Project Scaffold

## Purpose

使用 pnpm + Uni-app (Vue3 + TypeScript) 搭建微信小程序项目脚手架，集成 Pinia、uview-plus、Tailwind CSS，提供标准化的项目初始化和开发体验。

## Requirements

### Requirement: Project initialization with pnpm

The project SHALL be initialized using pnpm as the package manager with Uni-app (Vue3 + TypeScript) as the base framework targeting WeChat Mini Program.

#### Scenario: Project bootstrap completes successfully
- **WHEN** developer runs `pnpm install`
- **THEN** all dependencies SHALL be installed from `package.json`
- **AND** `node_modules/` SHALL be created with locked versions per `pnpm-lock.yaml`

#### Scenario: Dev server starts for WeChat Mini Program
- **WHEN** developer runs `pnpm dev:mp-weixin`
- **THEN** the project SHALL compile and start hot-reload watch mode
- **AND** the compiled output SHALL be ready for WeChat DevTools import

#### Scenario: Production build for WeChat Mini Program
- **WHEN** developer runs `pnpm build:mp-weixin`
- **THEN** the project SHALL produce an optimized production build under `dist/build/mp-weixin/`

### Requirement: Dependency management

The project SHALL include all required dependencies for the tech stack: Uni-app Vue3, TypeScript, Pinia, uview-plus, luch-request, and Tailwind CSS.

#### Scenario: All core dependencies are installable
- **WHEN** `pnpm install` is executed
- **THEN** `package.json` SHALL contain `pinia`, `uview-plus`, `luch-request`, `tailwindcss`, `@tailwindcss/postcss` as dependencies
- **AND** `package.json` SHALL contain `typescript`, `vite`, `@uni-helper/vite-plugin-uni-tailwind` as devDependencies

### Requirement: Tailwind CSS integration

Tailwind CSS SHALL be configured and integrated into the Uni-app project, with styles accessible from any page/component.

#### Scenario: Tailwind classes render correctly
- **WHEN** a component uses Tailwind utility classes (e.g., `class="flex items-center p-4"`)
- **THEN** the corresponding styles SHALL be applied at runtime

#### Scenario: Tailwind config is present
- **WHEN** checking project configuration
- **THEN** `tailwind.config.js` SHALL exist with proper content paths configured
- **AND** `postcss.config.js` SHALL include the Tailwind CSS plugin

### Requirement: Pinia state management setup

Pinia SHALL be globally registered and ready for store module creation.

#### Scenario: Pinia is usable in components
- **WHEN** a component imports and calls `useUserStore()`
- **THEN** the Pinia store instance SHALL be available
- **AND** state, getters, and actions SHALL work as expected

#### Scenario: Pinia instance is created at app startup
- **WHEN** the app initializes (`main.ts`)
- **THEN** `createPinia()` SHALL be called and passed to `app.use()`

### Requirement: uview-plus UI framework integration

uview-plus SHALL be globally registered with on-demand component loading via Uni-app easycom.

#### Scenario: uview-plus components are available
- **WHEN** a page uses `<u-button>` in its template
- **THEN** the uview-plus button component SHALL render without explicit import

#### Scenario: uview-plus global styles are applied
- **WHEN** the app starts
- **THEN** uview-plus base styles SHALL be available
- **AND** `uni.scss` SHALL import uview-plus theme variables
