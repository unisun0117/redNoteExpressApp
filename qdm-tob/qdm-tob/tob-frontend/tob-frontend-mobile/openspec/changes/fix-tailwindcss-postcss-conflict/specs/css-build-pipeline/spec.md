## ADDED Requirements

### Requirement: Build output SHALL NOT contain unprocessed Tailwind directives

The uni-app mini program build pipeline SHALL process all `@tailwind` CSS directives through the `tailwindcss` v3 PostCSS plugin, such that the final `.wxss` output files contain only standard CSS that the WeChat mini program compiler can parse.

#### Scenario: WeChat mini program build produces valid WXSS

- **WHEN** running `pnpm build:mp-weixin:test` or `pnpm dev:mp-weixin`
- **THEN** the output `dist/build/mp-weixin/app.wxss` and `dist/dev/mp-weixin/app.wxss` SHALL NOT contain any uncommented `@tailwind` directives
- **AND** the WeChat DevTools SHALL successfully compile the project without WXSS errors

### Requirement: Only tailwindcss v3 shall be present in dependencies

The project SHALL depend only on `tailwindcss` v3.x for Tailwind CSS processing, and SHALL NOT include `@tailwindcss/postcss` v4.x which conflicts with the v3 configuration.

#### Scenario: No conflicting Tailwind packages installed

- **WHEN** running `pnpm install`
- **THEN** `node_modules/tailwindcss` SHALL be present with version 3.x
- **AND** `node_modules/@tailwindcss/postcss` SHALL NOT be present
- **AND** `package.json` devDependencies SHALL NOT contain `@tailwindcss/postcss`
