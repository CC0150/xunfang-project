# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

讯方管理系统 (Xunfang Management System) — a Vue 3 admin dashboard built on the RuoYi-Cloud framework. It connects to a Spring Cloud backend via REST APIs with Bearer token authentication.

## Commands

```bash
npm run dev          # Dev server on port 80, proxies /dev-api → localhost:8080
npm run build:prod   # Production build (VITE_APP_BASE_API = /prod-api)
npm run build:stage  # Staging build (VITE_APP_BASE_API = /stage-api)
npm run preview      # Preview the production build
```

## Tech Stack

- **Framework**: Vue 3.5 (Composition API with `<script setup>`) + Vite 6.3
- **State**: Pinia 3 (`src/store/modules/`)
- **Routing**: Vue Router 4 with createWebHistory
- **UI**: Element Plus 2.9 (Chinese locale, size stored in cookies)
- **HTTP**: Axios with interceptors for auth tokens and error handling
- **CSS**: SCSS (variables in `src/assets/styles/variables.module.scss`, mixins in `mixin.scss`)
- **Other**: ECharts 5, Quill editor, vue-cropper, splitpanes, vuedraggable

## Build Plugins (`vite/plugins/`)

- `unplugin-auto-import` — auto-imports `vue`, `vue-router`, `pinia` APIs (no manual imports needed for `ref`, `computed`, `defineStore`, etc.)
- `unplugin-vue-setup-extend-plus` — allows `name` option in `<script setup>`
- `vite-plugin-svg-icons` — generates SVG sprite; registered as `virtual:svg-icons-register`; use via `<svg-icon icon-class="..." />`
- `vite-plugin-compression` — gzip/brotli compression (production only)

## Architecture

### Routing & Permission Flow

Routes are **partially backend-driven**. The flow:

1. `src/permission.js` — navigation guard checks for token, calls `getInfo()` (loads user roles/permissions), then calls `permissionStore.generateRoutes()`
2. `generateRoutes()` in `src/store/modules/permission.js` calls `getRouters()` API — the backend returns a component tree that is mapped to actual `.vue` files via `import.meta.glob('./../../views/**/*.vue')`
3. Frontend-defined `dynamicRoutes` (in `src/router/index.js`) are filtered against the user's permissions via `auth.hasPermiOr()` / `auth.hasRoleOr()` before being added
4. Route meta fields: `hidden` (hide from sidebar), `permissions` (required perms), `roles` (required roles), `activeMenu` (highlight a different sidebar item)

### Auth & Permissions

- Token stored in cookies as `Admin-Token`; sent as `Authorization: Bearer <token>` header
- `src/utils/auth.js` — get/set/remove token via js-cookie
- `src/plugins/auth.js` — `hasPermi()`, `hasPermiOr()`, `hasPermiAnd()`, `hasRole()`, `hasRoleOr()`, `hasRoleAnd()`; exposed as `$auth` global property
- `src/directive/permission/` — `v-hasRole` and `v-hasPermi` directives
- Super admin role is `admin`; wildcard permission is `*:*:*`

### Store Modules (Pinia)

| Module | File | Purpose |
|--------|------|---------|
| `user` | `src/store/modules/user.js` | Token, user info, roles, permissions; login/logout/getInfo actions |
| `permission` | `src/store/modules/permission.js` | Dynamic route generation from backend menu API |
| `app` | `src/store/modules/app.js` | Sidebar state, device type, component size |
| `settings` | `src/store/modules/settings.js` | Theme color, dark mode, layout options (tagsView, topNav, etc.) |
| `tagsView` | `src/store/modules/tagsView.js` | Opened page tabs state |
| `dict` | `src/store/modules/dict.js` | Cached dictionary data (label/value lookups) |

### Layout Structure

`src/layout/index.vue` composes: Sidebar → (Navbar + TagsView + AppMain + Settings drawer). Responsive: collapses to mobile mode below 992px.

### API Layer

- `src/utils/request.js` — Axios instance with base URL from `VITE_APP_BASE_API` env var
- Request interceptor: attaches Bearer token, serializes GET params, prevents duplicate submissions (1s window, sessionStorage-backed)
- Response interceptor: handles 401 (re-login prompt), 500, 601; for blob responses returns raw data
- API files organized by backend module: `src/api/system/`, `src/api/monitor/`, `src/api/tool/`
- `download()` utility for file downloads with loading indicator and blob validation
- FormData/multipart detection: strips Content-Type header to let browser set boundary

### Global Properties (available as `getCurrentInstance().proxy.*` in Composition API)

| Property | Source | Purpose |
|----------|--------|---------|
| `$auth` | `src/plugins/auth.js` | Permission checks |
| `$tab` | `src/plugins/tab.js` | Tab/page management |
| `$cache` | `src/plugins/cache.js` | sessionStorage/localStorage with JSON support |
| `$modal` | `src/plugins/modal.js` | Modal operations |
| `$download` | `src/plugins/download.js` | File download utilities |
| `useDict` | `src/utils/dict.js` | Fetch/cache dictionary data for a dict type |
| `parseTime` | `src/utils/ruoyi.js` | Date formatting |
| `handleTree` | `src/utils/ruoyi.js` | Build tree structure from flat array |
| `addDateRange` | `src/utils/ruoyi.js` | Append date range params to query |

### Custom Xunfang Utilities

`src/utils/xunfang.js` contains project-specific helpers used alongside the standard RuoYi utils.

### Global Components (registered in `main.js`)

`DictTag`, `Pagination`, `FileUpload`, `ImageUpload`, `ImagePreview`, `RightToolbar`, `Editor`

### Styles

- `src/assets/styles/element-ui.scss` — Element Plus theme overrides
- `src/assets/styles/ruoyi.scss` — application-specific styles
- `src/assets/styles/sidebar.scss` — sidebar styling
- `src/assets/styles/variables.module.scss` — SCSS variables (sidebar width, menu colors, etc.)
- `src/assets/styles/btn.scss` — button styles
- `src/assets/styles/transition.scss` — CSS transition utilities
- Dark mode via Element Plus dark CSS vars + `@vueuse/core` `useDark`/`useToggle`

### Env Files

| File | API Base | Purpose |
|------|----------|---------|
| `.env.development` | `/dev-api` | Proxied to localhost:8080 |
| `.env.production` | `/prod-api` | Production backend |
| `.env.staging` | `/stage-api` | Staging backend |
