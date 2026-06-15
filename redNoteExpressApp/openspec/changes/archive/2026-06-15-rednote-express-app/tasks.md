## 1. Project Setup

- [x] 1.1 Initialize React + Vite frontend project with TypeScript, React Router v6, and Ant Design Mobile component library
- [x] 1.2 Initialize Python FastAPI backend project with Poetry dependency management, Pydantic schemas, and project directory structure
- [x] 1.3 Set up PostgreSQL database with Docker Compose for local development, create initial migration framework (Alembic)
- [x] 1.4 Configure environment variables for LLM API keys, database URL, JWT secret, payment provider keys

## 2. Database Models and Core Infrastructure

- [x] 2.1 Create SQLAlchemy models: User, Generation, Subscription, Transaction, Template with migrations
- [x] 2.2 Implement credit balance tracking and atomic credit deduction utility
- [x] 2.3 Set up image upload endpoint with local/S3 storage, format validation (JPG, PNG, WEBP), and size limits
- [x] 2.4 Create shared API response/error handling middleware and Pydantic response schemas

## 3. User Authentication

- [x] 3.1 Implement email/password registration endpoint with bcrypt hashing and email verification flow
- [x] 3.2 Implement WeChat OAuth login endpoint (OAuth redirect → code exchange → OpenID lookup/create)
- [x] 3.3 Implement JWT access/refresh token generation, validation middleware, and token refresh endpoint
- [x] 3.4 Implement tier-based authorization middleware (free vs VIP access control per endpoint)
- [x] 3.5 Create user registration and login pages in frontend (email form + WeChat QR/redirect button)
- [x] 3.6 Grant 10 free credits on new user registration, display credit balance in UI header

## 4. LLM Integration and Article Generation

- [x] 4.1 Build LLM provider abstraction layer with OpenAI GPT-4o Vision as primary and configurable provider switching
- [x] 4.2 Implement image analysis stage: upload image → vision API → extract subjects, mood, and content keywords
- [x] 4.3 Implement structured prompt builder: assemble generation prompt with word count constraints (20+50+100×3+50+50) per section
- [x] 4.4 Implement multi-stage generation pipeline: title → intro → body section 1 → body section 2 → body section 3 → summary → store info
- [x] 4.5 Add word count validation: re-generate any section exceeding ±50% of target before returning final article
- [x] 4.6 Implement emoji injection control: include/exclude emoji based on user toggle, ensure natural placement
- [x] 4.7 Create generation API endpoint: POST /api/generate accepting images + keywords + style config, returning structured article JSON

## 5. Style System

- [x] 5.1 Create Template model seed data: retro (复古风), minimalist (简约风), humorous (幽默风), deep-review (深度测评风) with corresponding prompt modifiers
- [x] 5.2 Create track/category seed data: 美食, 运动, 摄影, 萌宠, 家居, 美妆, 数码, 母婴, 餐饮 with target persona prompt modifiers
- [x] 5.3 Implement style configuration API: GET /api/styles returning available templates and tracks
- [x] 5.4 Implement frontend StylePanel component with dropdown selectors for template, track, emoji toggle, and subtitle toggle
- [x] 5.5 Implement session-persistent style config: retain selections across generations within same browser session

## 6. Viral Article Analysis

- [x] 6.1 Implement viral article input UI: textarea for pasting URL or raw text, "Analyze Style" button
- [x] 6.2 Implement URL fetching endpoint: server-side fetch with timeout, text extraction from HTML, graceful fallback to user-pasted text
- [x] 6.3 Implement style analysis prompt: LLM extracts tone, sentence length, emoji patterns, hook structures from reference text
- [x] 6.4 Implement style profile merge: apply extracted viral style to next generation, taking precedence over manual style selection

## 7. Frontend Core UI

- [x] 7.1 Create GeneratorPage: main layout with image upload zone (drag-and-drop + file picker), keyword input field, and style panel
- [x] 7.2 Create image preview component with thumbnail display, remove/replace functionality, and upload progress indicator
- [x] 7.3 Create ResultView component: rendered article preview with section-by-section display, copy-to-clipboard per section and full article
- [x] 7.4 Create UserDashboard page: credit balance display, usage statistics, subscription status, transaction history list
- [x] 7.5 Implement responsive mobile-first layout with navigation (Generator, Dashboard, Settings tabs)
- [x] 7.6 Implement auth guards: redirect unauthenticated users to login, show VIP upgrade prompt for premium features

## 8. Subscription and Billing

- [x] 8.1 Implement credit recharge endpoint: select package → create order → payment provider integration (WeChat Pay JSAPI stub for v1)
- [x] 8.2 Implement VIP subscription endpoint: select plan (monthly/quarterly) → create subscription → payment → activate VIP tier
- [x] 8.3 Implement subscription expiration check: cron job or middleware that reverts VIP to free tier on expiry
- [x] 8.4 Implement transaction recording: log all purchases with provider, amount, status, timestamp
- [x] 8.5 Create billing frontend: recharge packages display, subscription plan comparison, payment flow, transaction history list

## 9. Batch Generation (B2B)

- [x] 9.1 Implement batch image upload endpoint: accept up to 50 images, return upload status and preview URLs
- [x] 9.2 Implement batch generation endpoint: accept array of image IDs + shared style config, queue 50 generation jobs, return progress tracker
- [x] 9.3 Implement batch progress API: GET /api/batch/{batch_id}/progress returning X/50 status with ETA
- [x] 9.4 Create MerchantBatchPage frontend: multi-image upload with preview grid, batch generate button, progress bar
- [x] 9.5 Create batch results view: scrollable article list with expand/collapse per article, "Copy All" button, download as text file
- [x] 9.6 Implement merchant store profile: saved store name and address auto-filled into all batch-generated articles

## 10. Content Safety and Polish

- [x] 10.1 Implement sensitive word filtering middleware for generated Chinese text before returning to user
- [x] 10.2 Add rate limiting on generation endpoints (free: 10/hour, VIP: 100/hour)
- [x] 10.3 Add loading states, skeleton screens, error toasts across all frontend components
- [x] 10.4 Add PWA manifest and service worker for mobile install capability

## 11. Testing and Deployment

- [x] 11.1 Write backend unit tests for auth flow, credit deduction, and generation pipeline
- [x] 11.2 Write API integration tests for all endpoints with mock LLM responses
- [x] 11.3 Write frontend component tests for GeneratorPage, StylePanel, and ResultView
- [x] 11.4 Create Dockerfile for backend and frontend, docker-compose for full-stack deployment
- [x] 11.5 Set up CI pipeline (GitHub Actions) for linting, type-checking, and tests
