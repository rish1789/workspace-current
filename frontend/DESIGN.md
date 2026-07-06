---
name: Workspace
description: A precise, blue-instrumented Kanban workspace for tracking work across teams, boards, and cards.
colors:
  accent: "#2563eb"
  accent-hover: "#1d4ed8"
  accent-soft: "#eff6ff"
  header-deep: "#1e3a8a"
  app-bg: "#eef2fa"
  board-bg: "#e6ebf5"
  lane-bg: "#dbe2f0"
  ink: "#0f172a"
  muted: "#64748b"
  surface: "#ffffff"
  border: "#e2e8f0"
  border-strong: "#cbd5e1"
  success-bg: "#f0fdf4"
  success-text: "#15803d"
  danger-bg: "#fef2f2"
  danger-text: "#dc2626"
typography:
  headline:
    fontFamily: "ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "1.875rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "normal"
  title:
    fontFamily: "ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: "normal"
  subtitle:
    fontFamily: "ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 600
    lineHeight: 1.35
    letterSpacing: "normal"
  body:
    fontFamily: "ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "normal"
  label:
    fontFamily: "ui-sans-serif, system-ui, -apple-system, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: "normal"
rounded:
  sm: "6px"
  md: "8px"
  lg: "12px"
  full: "9999px"
spacing:
  xs: "6px"
  sm: "10px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.accent}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  button-primary-hover:
    backgroundColor: "{colors.accent-hover}"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "10px 16px"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.muted}"
    rounded: "{rounded.md}"
    padding: "8px 14px"
  field-input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    padding: "8px 12px"
  tile:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "20px"
  badge-accent:
    backgroundColor: "{colors.accent-soft}"
    textColor: "{colors.accent}"
    rounded: "{rounded.full}"
    padding: "2px 10px"
---

# Design System: Workspace

## 1. Overview

**Creative North Star: "The Command Deck"**

Workspace reads as an instrument, not a decoration. The palette is a single
disciplined blue — used the way a control room uses status color, to mark
what's active, actionable, or yours — set against cool ink-tinted neutrals
that never compete for attention. Structure carries the design: consistent
containers, predictable button hierarchy, restrained motion. Nothing about
the interface should look generated or default; every screen (including
empty and error states) is built with the same care as the happy path,
because this project doubles as a portfolio artifact.

This system explicitly rejects the generic AI-SaaS look: no cream or beige
backgrounds, no gradient text, no uppercase eyebrow labels stacked above
every section, no numbered 01/02/03 scaffolding, no identical repeated card
grids, no colored side-stripe borders. Confidence comes from precision and
consistency, not from visual volume.

**Key Characteristics:**
- One accent color (`#2563eb`), used deliberately rather than everywhere
- Cool, faintly blue-tinted neutrals instead of warm/cream defaults
- Soft, low-elevation shadows that respond to interaction, not resting on every surface
- Deep navy-to-blue gradient reserved for two moments only: the app header and the auth branding panel
- Compact, workflow-dense layouts (boards, lanes, cards) that stay legible at a glance

## 2. Colors

A one-accent system: a single instrumentation blue against cool neutrals, with deep navy reserved for two branded surfaces.

### Primary
- **Instrument Blue** (`#2563eb`): the one accent. Primary buttons, links, active states, focus rings, icons that mean "this is actionable or yours."
- **Instrument Blue, Pressed** (`#1d4ed8`): hover/active state for the primary accent.
- **Instrument Blue, Whisper** (`#eff6ff`): the accent at its quietest — badge backgrounds, selected-row tints, icon chips. Never used for large surfaces.

### Neutral
- **Deep Harbor** (`#1e3a8a`): the dark end of the header/auth gradient. Used only in the two branded surfaces (app header bar, auth side panel), never as a general background.
- **App Canvas** (`#eef2fa`): the page background — a faint blue-gray, not a warm neutral. Sets the app apart from a plain white or cream canvas without competing with content.
- **Board Canvas** (`#e6ebf5`): one step deeper than App Canvas, used to recess the board/workspace detail surface behind its cards.
- **Lane Bed** (`#dbe2f0`): the resting surface for Kanban lanes — deep enough to read as a distinct "bed" for the white cards inside it.
- **Ink** (`#0f172a`): primary text color. Never pure black.
- **Muted Slate** (`#64748b`): secondary text — labels, meta info, placeholders under 4.5:1 contrast checks against white.
- **Surface White** (`#ffffff`): cards, modals, inputs, tiles — the "paper" the interface writes on.
- **Hairline** (`#e2e8f0`) / **Hairline Strong** (`#cbd5e1`): borders and dividers. Hairline for card/tile edges, Hairline Strong for input borders and rules that need to read at a glance.

### Feedback
- **Confirm Green** (bg `#f0fdf4`, text `#15803d`): success badges, positive counts.
- **Alert Red** (bg `#fef2f2`, text `#dc2626`): error banners, destructive-action text (e.g. delete/leave actions).

### Named Rules
**The One Voice Rule.** Instrument Blue is the only saturated color in the system. It appears on buttons, links, active/focus states, and small accent chips — never as a large background fill outside the header and auth panel. If a screen needs a second color to feel organized, that's a signal to fix hierarchy with type weight or spacing, not a second hue.

## 3. Typography

**Body Font:** ui-sans-serif / system-ui stack (Tailwind's default sans stack; no custom webfont loaded).

**Character:** A single, neutral system-font voice carries the whole app — weight and size do the work of hierarchy, not font pairing. This matches the "precise, unshowy" personality: nothing about the typography should call attention to itself.

### Hierarchy
- **Headline** (700, 1.875rem / 30px, line-height 1.2): auth page welcome copy ("Welcome back"-style moments). Used sparingly, at most once per screen.
- **Title** (600, 1.25rem / 20px, line-height 1.3): page-level titles inside the app shell (e.g. "Workspaces", a board's name in its header).
- **Subtitle** (600, 1.125rem / 18px, line-height 1.35): section and card-group headings ("Your workspaces", "Overview").
- **Body** (400, 0.875rem / 14px, line-height 1.5, max ~70ch): the default text size for the entire app — form fields, card copy, list rows, descriptions.
- **Label** (500, 0.75rem / 12px, letter-spacing normal): meta text, timestamps, counts, badge text.

### Named Rules
**The Two-Weight Rule.** Every screen uses semibold/bold (600–700) for structure (titles, emphasis, counts) and regular (400) for content. No in-between weights, no italics for emphasis.

## 4. Elevation

Workspace is mostly flat at rest — surfaces are separated by tone (Surface White on App/Board Canvas) rather than shadow. Shadow appears only as a response to hierarchy or interaction: modals and floating panels get a resting shadow because they sit above the page; cards and tiles gain a slightly stronger shadow and a small upward translate on hover, signaling they're clickable.

### Shadow Vocabulary
- **Resting Card** (`box-shadow: 0 1px 3px rgba(15,23,42,0.08)`): default state for tiles and cards — just enough separation to read as a surface, not a decoration.
- **Hover Lift** (`box-shadow: 0 8px 20px rgba(15,23,42,0.1)` + `translateY(-2px)`): interactive tiles/cards on hover — signals "clickable," pairs with the upward nudge, never used on static content.
- **Header Shadow** (`box-shadow: 0 4px 12px rgba(29,78,216,0.25)`): tinted with the accent, not neutral gray — used only under the app header to keep it feeling anchored above page content.
- **Modal Shadow** (`box-shadow: 0 20px 25px -5px rgba(0,0,0,0.15), 0 0 0 1px rgba(15,23,42,0.04)`): the deepest shadow in the system, reserved for the one truly overlaid surface (modals).

### Named Rules
**The Shadow-Means-Interactive Rule.** If a shadow is present and it's not the app header or a modal, it should mean "you can act on this." Don't add decorative shadow to static content.

## 5. Components

Buttons, cards, and inputs feel tactile and confident: clear affordances, a decisive hover response (color shift or a small lift), and states that never leave the user guessing whether an action registered.

### Buttons
- **Shape:** rounded corners (8px / `rounded-lg`).
- **Primary:** Instrument Blue fill, white text, semibold, `10px 16px` padding; on hover, deepens to Pressed Blue and lifts 1px with a tinted shadow; disabled drops to 50% opacity and blocks pointer events.
- **Secondary:** white fill, Ink text, a 1px Hairline Strong ring instead of a border, hovers to a faint slate tint.
- **Ghost:** transparent, Muted Slate text, hovers to a faint dark tint and Ink text — used for lower-emphasis actions ("Logout", "Back").
- **On the header/auth surfaces:** dedicated white or translucent-white variants (`btn-header-cta`, `btn-header-glass`, `btn-header-ghost`) so buttons stay legible against the dark gradient — never reuse the light-surface button styles there.

### Badges / Pills
- **Style:** fully rounded (`rounded-full`), small type (12px, medium weight), `2px 10px` padding.
- **Variants:** accent (Whisper Blue bg / Blue text) for neutral counts, green for positive/success states, gray (Hairline-tinted) for inactive/default, white-translucent for badges placed on the dark header.

### Cards / Tiles
- **Corner Style:** 12px radius (`rounded-xl`).
- **Background:** Surface White, always — cards are the one place white is guaranteed regardless of the page's canvas tone.
- **Shadow Strategy:** Resting Card at rest, Hover Lift on interactive tiles (see Elevation).
- **Border:** 1px Hairline.
- **Internal Padding:** 20px (`p-5`) for clickable tiles (workspace/board cards); tighter for compact list rows.

### Inputs / Fields
- **Style:** white background, 1px Hairline Strong border, 6px radius (`rounded-md`), 14px text.
- **Focus:** border shifts to Instrument Blue plus a 2px blue focus ring — the same blue used everywhere else, never a separate "focus color."
- **Error:** paired with an `alert-error` banner (Alert Red bg/text) above or below the field rather than a red border on the field itself.

### Navigation
- **App header:** a sticky, full-bleed bar using the Deep Harbor → Instrument Blue gradient, white text, translucent-white ghost/CTA buttons, tinted shadow beneath it. This is the one place in the app where color dominates the surface.
- **Empty states:** dashed Hairline Strong border, `Surface White` at 60% opacity, centered muted text — deliberately quiet so it doesn't compete with populated states elsewhere on the same page.

### Modal
A single floating surface pattern used app-wide (settings, card detail): white panel, 12px radius, deepest shadow in the system, with a thin gradient accent bar (`from-blue-600 to-indigo-500`) as the only decorative flourish in the entire component system — reserved for this one moment.

## 6. Do's and Don'ts

### Do:
- **Do** keep Instrument Blue (`#2563eb`) as the only saturated accent; everything else is neutral or feedback color.
- **Do** use tone-on-tone neutrals (App Canvas → Board Canvas → Lane Bed → Surface White) to convey depth instead of shadow on static content.
- **Do** reserve the Deep Harbor gradient for the app header and auth panel only.
- **Do** give every interactive card/tile a hover lift + shadow response so affordance is never ambiguous.
- **Do** build empty, loading, and error states with the same visual care as populated states — this project is judged as a portfolio piece as much as a product.

### Don't:
- **Don't** introduce a cream/beige/warm-neutral background anywhere; canvases in this system are cool blue-grays, never warm.
- **Don't** use gradient text, uppercase eyebrow labels above sections, or numbered 01/02/03 scaffolding — none of that exists in this system and it shouldn't be added.
- **Don't** use a colored `border-left`/`border-right` stripe as an accent on cards or list rows.
- **Don't** add a second saturated hue "for variety" — differentiate with weight, spacing, or the existing feedback colors (green/red) instead.
- **Don't** apply shadow to non-interactive, static surfaces — shadow means "you can act on this" everywhere except the header and modals.
