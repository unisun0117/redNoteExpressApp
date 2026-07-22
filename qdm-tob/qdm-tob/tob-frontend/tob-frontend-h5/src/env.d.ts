/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_ENV: 'development' | 'test' | 'production'
  readonly VITE_APP_TITLE: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_SLS_ENABLED: 'true' | 'false'
  readonly VITE_WX_CORP_ID: string
  readonly VITE_WX_AGENT_ID: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
