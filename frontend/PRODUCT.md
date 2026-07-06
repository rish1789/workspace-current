# Product

## Register

product

## Users

Primarily the developer themselves and anyone evaluating the project (hiring
managers, code reviewers) — this is a portfolio piece demonstrating full-stack
engineering skill (Spring Boot backend + React frontend). Secondarily, it's
built to function as a real lightweight Kanban tool: solo or small-team task
tracking through workspaces → boards → lanes → cards, in the spirit of
Trello. The job to be done on any given screen is a concrete workflow step
(log in, open a board, move/edit a card) — not browsing or being persuaded.

## Product Purpose

A Kanban-style project management app: users create workspaces, add boards
per workspace, organize work into lanes (columns), and track cards (tickets)
with descriptions, due dates, comments, and checklists. Success looks like a
tool that feels production-grade end to end — no screen reads as a tutorial
scaffold or placeholder. Since it doubles as a portfolio artifact, visual and
interaction polish is itself part of the product's purpose, not a nice-to-have.

## Brand Personality

Modern & confident. Three words: **precise, crafted, unshowy**. Confidence
here comes from restraint and consistency (a real token-driven design system,
deliberate spacing and motion) rather than loud visual flourish. It should
read as an app built by someone who sweats details, not as a generated
dashboard template.

## Anti-references

Avoid the generic AI-SaaS look: cream/beige backgrounds used as a default,
gradient text, tiny uppercase "eyebrow" labels above every section, numbered
01/02/03 scaffolding, identical repeated card grids, side-stripe colored
borders. No specific named anti-reference sites — the concern is category-
reflex genericness, not a particular competitor.

## Design Principles

- Workflow clarity over decoration — every screen supports the task
  (auth, workspace/board management, card editing) without adding friction.
- Read as shipped, not scaffolded — no placeholder-looking states; empty
  states, errors, and edge cases get the same care as the happy path.
- One consistent token-driven system — colors, spacing, and components draw
  from the existing Tailwind theme (`src/index.css`) rather than one-off values.
- Confidence through precision, not volume — restrained color use, purposeful
  motion, tight visual rhythm over saturated/loud styling.
- Accessible by default — keyboard navigable, sufficient contrast, no
  interaction that depends on hover/color alone.

## Accessibility & Inclusion

No specific stated requirements beyond standard good practice: WCAG AA
contrast targets, full keyboard operability (forms, modals, nav), visible
focus states, and respect for `prefers-reduced-motion`.
