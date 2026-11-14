---
name: Arbitrum
colors:
  primary: "#05163D"
  secondary: "#016BE5"
  accent: "#10E1FF"
  highlight: "#FFE103"
  surface: "#FFFFFF"
  surface-alt: "#F2F2F2"
  on-surface: "#05163D"
typography:
  display:
    fontFamily: FK Screamer
    fontSize: 128px
    fontWeight: 500
  heading:
    fontFamily: Aeonik
    fontSize: 36px
    fontWeight: 500
  body-md:
    fontFamily: Aeonik
    fontSize: 16px
    fontWeight: 500
  mono:
    fontFamily: Aeonik Fono
    fontSize: 16px
    fontWeight: 400
rounded:
  sm: 6px
  md: 8px
  lg: 10px
  xl: 16px
  pill: 40px
---

# Design System Inspired by Arbitrum (`DKSang/ARBITRUM`)

## 1. Visual Theme & Atmosphere

Arbitrum's design system embodies a high-energy, finance-native Web3 aesthetic that pairs architectural precision with kinetic 3D and motion-driven storytelling. Anchored on a crisp white canvas overlaid with subtle vertical architectural grid lines (`repeating-linear-gradient` at `16.67%` column intervals), the visual language alternates between clean, light-mode editorial clarity (`#FFFFFF` and `#F2F2F2` Stone White) and bold, saturated brand moments—deep navy (`#05163D`), electric blue (`#016BE5`), neon cyan (`#10E1FF`), and high-voltage yellow (`#FFE103`).

Typography drives contrast across scales: ultra-condensed uppercase display headlines in **FK Screamer** create monumental hero moments, geometric **Aeonik** handles section headers and body copy with crisp authority, and monospaced **Aeonik Fono** provides technical eyebrow labels and metadata categorization. Interactive 3D cubes, counter-rotating orbital ecosystem rings, infinite marquees, and pill-shaped glassmorphic CTAs with cyan glow effects bring a tactile, programmable-economy feel to the interface.

**Key Characteristics**
- **Architectural Grid Canvas:** Full-bleed repeating vertical column lines (`1px` at `rgba(0, 0, 0, 0.08)` every `16.67%`) grounding a `1440px` centered layout container
- **High-Contrast Brand Palette:** Deep navy (`#05163D`) and pure white (`#FFFFFF`) foundation energized by electric blue (`#016BE5`), neon cyan (`#10E1FF`), and signal yellow (`#FFE103`)
- **Tri-Font Typographic System:** Condensed display (**FK Screamer**) for monumental uppercase impact, geometric sans (**Aeonik**) for UI and editorial copy, and technical monospace (**Aeonik Fono**) for uppercase eyebrows
- **Kinetic & 3D Interactions:** Perspective-driven 3D tilt cards (`perspective: 1000px`, `preserve-3d`), interactive 6-face 3D cubes, orbital counter-rotating logo rings, staggered SVG clip-path scan animations, and pause-on-hover infinite marquees
- **Pill-Shaped Interactive Language:** Rounded pill buttons (`rounded-[40px]` and `rounded-3xl`) with backdrop blur, sliding arrow icons (`group-hover:translate-x-1`), and neon cyan glow shadows (`0 0 14px 0 rgba(16, 225, 255, 0.5)`)

---

## 2. Color Palette & Roles

### Primary Brand Colors
- **Primary Blue 1 / Navy** (`#05163D`): Primary brand foreground, deep structural text, SVG scan paths, and dark gradient anchor
- **Primary Blue 2 / Arbitrum Blue** (`#016BE5`): Core brand blue used in hero top gradients and primary brand accents
- **Primary Blue 3 / Neon Cyan** (`#10E1FF`): Interactive focus rings, text selection highlight (`selection:bg-[#10e1ff]`), hover text states, button/icon glow effects, and gradient stops

### Accent & Signal Colors
- **Arbitrum Yellow** (`#FFE103`): High-emphasis hover state fill for jumbo CTA pills (`hover:bg-yellow`) and top stop of the technology gradient (`bg-yellow-blue`)
- **Arbitrum Orange** (`#FF4400`): Secondary energetic brand accent token (`--orange`)
- **Arbitrum Green** (`#00E340`): Vibrant neon green brand token (`--green`)
- **Success Green** (`#47D32F`): Semantic positive/status confirmation token (`--success`)
- **Destructive** (`oklch(0.577 0.245 27.325)`): Error and destructive state token

### Neutral & Surface Scale
- **Pure White** (`#FFFFFF`): Primary page background (`--background`), card surface (`--card`), sticky header surface on scroll, and active tab pill background
- **Stone White** (`#F2F2F2`): Secondary section background (`--stone-white`, `--secondary`, `--muted`) used for Stats, Case Studies, Project Cards, and Final CTA sections
- **Gray Lighter / Border** (`#E7E7E7`): Default border and input stroke color (`--border`, `--input`)
- **Grey** (`#D6D6D6`): Mid-light neutral divider token (`--grey`)
- **Gray Dark** (`#272727`): Muted foreground text color (`--muted-foreground`)
- **Gray Darker** (`#131313`): Near-black neutral token (`--gray-darker`)
- **Pure Black** (`#000000`): Hero CTA pill background, primary body text utility (`text-black`), and bottom stop of the footer gradient

### Gradients & Special Surfaces
- **Hero Gradient:** `linear-gradient(180deg, #016be5 0%, #05163d 100%)` with `xerox_scan.webp` texture overlay and `#05163D` animated SVG vector paths
- **Yellow-to-Cyan Gradient (`.bg-yellow-blue`):** `linear-gradient(#ffe103 0%, #10e1ff 100%)` used for featured technology callout containers
- **Footer Gradient (`.bg-footer`):** `linear-gradient(#05163d 0%, #000000 100%)`
- **Case Study Card Overlay:** `linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.2) 50%, transparent 100%)`
- **Repeating Architectural Grid (`.bg-repeating-lines`):**
  ```css
  repeating-linear-gradient(
    90deg,
    rgba(0, 0, 0, 0.08) 0px,
    rgba(0, 0, 0, 0.08) 1px,
    #ffffff 1px,
    #ffffff 16.67%
  )
  ```

---

## 3. Typography Rules

### Font Families
- **Primary Sans (`--font-sans` / `--font-aeonik`):** `Aeonik` (Regular `400`, Medium `500`), fallback `ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`
- **Display Condensed (`--font-screamer`):** `FK Screamer Upright` (Medium `500`), fallback `Aeonik, sans-serif`
- **Technical Monospace (`--font-mono` / `--font-aeonik-fono`):** `Aeonik Fono` (Regular `400`), fallback `ui-monospace, monospace`
- **Supplementary Fonts:** `DM Sans` (`--font-dm-sans`), `DM Mono` (`--font-dm-mono`), `Fjalla One` (`--font-fjalla`)

### Hierarchy

| Role | Font | Size | Weight | Line Height | Case / Tracking | Notes |
|------|------|------|--------|-------------|-----------------|-------|
| Hero Display (H1) | FK Screamer | `128px` (`lg:text-9xl`), `96px` (`md:text-8xl`), `60px` (`text-6xl`) | 500 | `0.9` (`lg:!leading-[0.9]`) / `1.0` | `uppercase` | Monumental hero headline in white |
| Scene Panel Title | FK Screamer | `36px` (`text-4xl`) | 700 (`font-bold`) | `1.0` (`leading-none`) | Title Case | 3D tilt card headings |
| Section Heading (H2) | Aeonik | `36px` (`text-4xl`) | 500 (`font-medium`) | `0.95` (`.leading-extra-tight`) or `1.0` (`leading-none`) | Title Case | Primary section headers in `#05163D` |
| Jumbo CTA Pill Text | Aeonik | `36px` (`text-4xl`) | 500 (`font-medium`) | `1.1` | Title Case | Full-width stacked CTAs in FinalCTA |
| Feature / Stat Value | Aeonik | `24px` (`text-2xl`) | 500–600 | `0.95` (`.leading-extra-tight`) | Normal | Stat values use `font-semibold` + `.text-shadow-blue` |
| Subheading / Lead | Aeonik | `18px–20px` (`text-lg` to `text-xl`) | 500 | `1.375` (`leading-snug`) | Normal | Hero lead, stats intro, ecosystem ring copy |
| Eyebrow / Tag | Aeonik Fono | `14px–16px` (`text-sm` to `text-base`) | 400 | `1.5` | `uppercase` | Section category labels (`CASE STUDIES`, `A FLEXIBLE PLATFORM`) |
| Body Standard | Aeonik | `16px` (`text-base` / `text-md`) | 500 | `1.5` | Normal | Standard descriptions (often `opacity-60` or `opacity-80`) |
| Card Description / UI | Aeonik | `14px` (`text-sm`) | 500–600 | `1.4` | Normal | Project card copy (`opacity-60`), pill button labels (`font-semibold`) |
| Navigation / Footer Link | Aeonik | `12px–14px` (`text-xs` to `text-sm`) | 500–600 | `1.4` | `uppercase` (`tracking-wide`) | Header dropdowns and footer directory links |
| Micro Label | Aeonik | `10px` (`text-[10px]`) | 500 | `1.2` | Normal | Orbital ring logo labels |

### Principles
- **Contrast via Condensed Display vs. Geometric Sans:** Pair ultra-tight `FK Screamer` (`line-height: 0.9–1.0`) for high-impact headlines with wide, geometric `Aeonik` for scannable headings and body text.
- **Monospace Category Anchors:** Always precede major feature sections with an uppercase `Aeonik Fono` eyebrow (`mb-2 font-aeonik-fono text-base uppercase text-primary-blue-1`).
- **Opacity-Driven Hierarchy:** Instead of introducing extra gray hex tokens for body copy, use `#05163D` or `#000000` with `opacity-80` for primary descriptions and `opacity-60` for secondary card descriptions.

---

## 4. Component Stylings

### Buttons & CTAs

#### Hero Primary Pill (Dark)
- **Background:** `#000000` (`bg-black`)
- **Text Color:** `#FFFFFF` (`text-white`), `14px` (`text-sm`) mobile / `16px` (`lg:text-base`) desktop
- **Dimensions:** `h-10` (`40px`), `w-full` on mobile, `lg:w-[200px] xl:w-[250px]` on desktop
- **Border Radius:** `24px` (`rounded-3xl`)
- **Hover State:** Text shifts to `#10E1FF` (`hover:text-primary-blue-3`) with cyan outer glow (`hover:shadow-button-glow`: `0 0 14px 0 rgba(16, 225, 255, 0.5)`)

#### Outlined Glass Pill Button (Standard Section CTA)
- **Background:** Transparent with `backdrop-blur`
- **Border:** `1px solid rgba(5, 22, 61, 0.2)` (`border-primary-blue-1/20`) or `border-black/10`
- **Text Color:** `#05163D` (`text-primary-blue-1`) or `#000000`, `14px` (`text-sm`), `font-semibold`
- **Padding:** `8px 20px` (`px-5 py-2`), `gap-2` (`8px`)
- **Border Radius:** `40px` (`rounded-[40px]`)
- **Trailing Icon:** `24px × 24px` right arrow (`/arrow_right.svg`) that slides right on hover (`group-hover:translate-x-1`) and gains `group-hover:shadow-button-glow`

#### Jumbo Action Pill (Final CTA)
- **Background:** Transparent default, `#FFE103` (`hover:bg-yellow`) on hover
- **Border:** `1px solid rgba(5, 22, 61, 0.2)` (`border-primary-blue-1/20`)
- **Text Color:** `#000000` (`text-black`), `36px` (`text-4xl`), `font-medium`
- **Padding:** `40px 16px` (`px-4 py-10`)
- **Border Radius:** `40px` (`rounded-[40px]`)
- **Hover Interaction:** Background fills with `#FFE103`, hidden `48px × 48px` arrow fades in and shifts right (`group-hover:translate-x-1 group-hover:opacity-100`)

#### Base UI / Shadcn Button Primitive (`src/components/ui/button.tsx`)
- **Base:** `rounded-lg text-sm font-medium transition-all`, focus ring `focus-visible:ring-3 focus-visible:ring-ring/50` (`#10E1FF`)
- **Sizes:** `xs` (`h-6 px-2 text-xs`), `sm` (`h-7 px-2.5 text-[0.8rem]`), `default` (`h-8 px-2.5`), `lg` (`h-9 px-2.5`), `icon` (`size-8`)
- **Variants:** `default` (`bg-primary text-primary-foreground`), `outline`, `secondary`, `ghost`, `destructive`, `link`

### Cards & Containers

#### 3D Tilt Scene Card (`ScenePanels`)
- **Wrapper:** `perspective: 1000px` (`.hero-card-wrapper`)
- **Surface:** `rgba(255, 255, 255, 0.70)` (`bg-white/70`)
- **Dimensions:** `max-w-[360px]`, `h-full`, `p-4` (`16px`), `gap-10` (`40px`)
- **Border Radius:** `12px` (`rounded-[12px]`)
- **Media:** `327px` tall image (`rounded-lg object-fill`)
- **Interaction:** Pointer-driven 3D tilt (`rotateY(px * 10deg) rotateX(-py * 10deg)`) with `transition: transform 0.3s`

#### Case Study Marquee Card (`CaseStudies`)
- **Dimensions:** `315px × 280px` (`w-[315px] h-[280px]`), `shrink-0`, `mx-3`
- **Border Radius:** `16px` (`rounded-2xl`)
- **Border:** `1px solid transparent`, shifts to `rgba(0, 0, 0, 0.4)` (`hover:border-black/40`) on hover
- **Overlay & Media:** Full-bleed cover image (`group-hover:scale-105 duration-500`) + bottom dark gradient (`from-black/80 via-black/20 to-transparent`) + bottom-left white title (`p-5 text-base font-medium text-white`)
- **Hover Elevation:** `box-shadow: 0 12px 40px rgba(0, 0, 0, 0.14)` (`.shadow-card-hover`)

#### Ecosystem Project Card (`EcosystemTabs`)
- **Surface:** `#F2F2F2` (`bg-stone-white`)
- **Dimensions:** `min-h-[320px]`, `w-full`, `p-6` (`24px`), `gap-14` (`56px`)
- **Border Radius:** `16px` (`rounded-2xl`)
- **Header Row:** `48px × 48px` project logo (`rounded-lg`) on left + `Learn More` link with `32px × 32px` arrow on right
- **Footer Block:** Project title (`text-lg` / `18px`) + 2–3 line description (`text-sm opacity-60`)

#### Featured Gradient Banner (`TechSection`)
- **Surface:** `.bg-yellow-blue` (`linear-gradient(#ffe103 0%, #10e1ff 100%)`)
- **Border Radius:** `20px` (`rounded-[1.25rem]`)
- **Padding:** `pt-20 pb-[340px] px-4` on mobile, `lg:px-20 lg:py-60` on desktop
- **Decor:** 14絕對-positioned isometric `lego_cube.webp` (`220px × 288px`) arranged in a staircase on the left (`hover:mix-blend-soft-light`), content aligned to the right `40%` (`max-w-[540px]`)

### Navigation & Tabs

#### Sticky Header (`Header`)
- **Layout:** `sticky top-0 z-20`, `px-5 py-4` (`~70px` height)
- **Scroll Behavior:** Transparent on desktop at scroll `0` (`lg:bg-transparent`), transitions (`duration-300 cubic-bezier(0.4,0,0.2,1)`) to solid `#FFFFFF` with `shadow-[0_1px_2px_rgba(0,0,0,0.05)]` when scrolled
- **Brand Logo Interaction:** Default inverted black mark (`icon_white.svg` with `invert`); on hover, crossfades (`duration-300`) to full-color mark (`logo_color.svg`) and reveals the 8-letter SVG wordmark (`arbitrum`)
- **Nav Triggers:** 5 uppercase dropdown buttons (`SOLUTIONS`, `PRODUCTS`, `WHY ARBITRUM`, `DEVELOPERS`, `RESOURCES`) with rotating `▾` indicator (`rotate-180`)
- **Dropdown Panel:** Centered below trigger (`lg:left-1/2 lg:top-[38px] lg:-translate-x-1/2 lg:z-30`), white card (`bg-white py-2 px-2 rounded-lg`), shadow `0 4px 20px rgba(0,0,0,0.08)`, links in uppercase `#05163D/70` → `#05163D` on hover

#### Segmented Tab Bar (`EcosystemTabs`)
- **Container:** `max-w-[480px]`, `rounded-lg bg-muted` (`#F2F2F2`), `p-1` (`4px`)
- **Tab Trigger:** `h-11` (`44px`), `flex-1`, `rounded-md`, `text-sm font-medium`
- **Active State:** `bg-white text-primary-blue-1 shadow-sm`
- **Inactive State:** `text-primary-blue-1/60 hover:text-primary-blue-1`

#### Footer Newsletter Form (`Footer`)
- **Input:** `rounded-md border border-white/20 bg-transparent px-3 py-2 text-sm text-white placeholder:text-white/40 focus:border-primary-blue-3`
- **Submit Button:** `rounded-md bg-white px-4 py-2 text-sm font-semibold uppercase text-primary-blue-1 hover:bg-primary-blue-3`

---

## 5. Layout Principles

### Page Topology & Grid System
- **Root Wrapper:** `<main class="bg-repeating-lines">` > `<div class="m-auto flex max-w-[1440px] flex-col">`
- **Content Max-Widths:**
  - `1440px`: Outer page frame and orbital ring diameter
  - `1280px`: 2-column feature grids (`FlexiblePlatform`) and 4-column project card grids (`EcosystemTabs`)
  - `1000px`: Final CTA button stack
  - `720px` / `670px` / `620px`: Centered section lead paragraphs and hero subtitle
- **Column Patterns:**
  - **1-Column Stacked:** Hero, Stats rows (`grid-rows-3`), ReadyToBuild
  - **2-Column Split:** FlexiblePlatform (`lg:grid-cols-2 gap-20`), FinalCTA (`lg:flex-row gap-20`)
  - **3-Column Grid:** ScenePanels (`md:grid-cols-3 gap-16 lg:gap-[6vw]`)
  - **4-Column Grid:** EcosystemTabs project cards (`grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10`)
  - **7-Column Directory:** Footer navigation (`grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-8`)

### Spacing Scale
- **Component Micro-Gaps:** `4px` (`p-1`), `8px` (`gap-2`), `12px` (`gap-3`), `16px` (`p-4`, `px-4`)
- **Card & Grid Gaps:** `24px` (`gap-6`, `p-6`), `40px` (`gap-10`), `64px` (`gap-16`), `80px` (`gap-20`)
- **Section Vertical Padding:**
  - Standard major sections: `120px` (`py-30`)
  - Hero container: `p-5 pt-0`, height `736px` mobile / `calc(100vh - 84px)` desktop
  - Large callout sections: `py-20` (`80px`) to `lg:py-60` (`240px`)

### Border Radius Scale
- **Micro (`4px`–`6px`):** Arrow wrappers (`rounded-[4px]`), tab buttons (`rounded-md`), footer inputs
- **Standard (`8px`–`12px`):** Base buttons (`rounded-lg`), project logo tiles, 3D tilt scene cards (`rounded-[12px]`)
- **Large Cards (`16px`–`20px`):** Case study cards, ecosystem project cards (`rounded-2xl`), tech section container (`rounded-[1.25rem]`)
- **Hero Frame (`30px`):** Main hero inner container (`rounded-[30px]`)
- **Full Pill (`24px`–`40px`):** Hero CTA buttons (`rounded-3xl`), section & jumbo CTA pills (`rounded-[40px]`)

---

## 6. Depth, Elevation & Motion

### Shadow & Glow Tokens

| Token / Class | Value | Usage |
|---------------|-------|-------|
| Sticky Header Shadow | `0 1px 2px rgba(0, 0, 0, 0.05)` | Subtle bottom edge when page is scrolled |
| Dropdown Shadow | `0 4px 20px rgba(0, 0, 0, 0.08)` | Header navigation popover menus |
| Card Hover (`.shadow-card-hover`) | `0 12px 40px rgba(0, 0, 0, 0.14)` | Case study cards on hover |
| 3D Cube Shadow (`.drop-shadow-cube`) | `drop-shadow(0 52px 26px rgba(0, 0, 0, 0.2))` | Interactive 3D cube in FinalCTA |
| Cyan Button Glow (`.shadow-button-glow`) | `0 0 14px 0 rgba(16, 225, 255, 0.5)` | Primary pills and arrow badges on hover |
| Social Hex Glow | `drop-shadow(0 0 6px rgba(16, 225, 255, 0.6))` | Header and footer social icons on hover |
| Stat Cyan Text Glow (`.text-shadow-blue`) | `0 0 12px rgba(16, 225, 255, 0.45)` | Highlighted metric numbers in Stats section |

### Signature Motion & Interactive Behaviors
- **Easing Curve:** Standard transitions use `cubic-bezier(0.4, 0, 0.2, 1)` over `300ms` (`duration-300`).
- **Hero Staggered Scan (`.hero-path`):** 7 diagonal SVG paths animate via `hero-scan` (`clip-path: inset(0 100% 0 0)` → `inset(0 0 0 0)`) over `1.2s cubic-bezier(0.4, 0, 0.2, 1)` with `0.08s` stagger increments (`0s` to `0.48s`).
- **Counter-Rotating Orbital Rings (`EcosystemRing`):** Outer ring (`1440px`, radius `720px`, 11 logos, `80s` CW) and inner ring (`620px`, radius `310px`, 5 logos, `60s` CCW) rotate via `requestAnimationFrame` while each child logo counter-rotates (`rotate(-a deg)`) to stay upright.
- **Infinite Marquee (`.animate-marquee`):** `translateX(0)` to `translateX(-50%)` over `30s linear infinite`, pausing on container hover (`.marquee-container:hover`).
- **Interactive 3D Cube (`Cube3D`):** 6-face CSS 3D cube (`320px × 320px`, `HALF = 160px`, initial rotation `x: -35deg, y: 45deg`) supporting pointer capture drag rotation.

### Z-Index Scale
- **Base Flow:** `0` / `auto`
- **Raised Interactive Panels (`ScenePanels`, `TechSection` cubes on hover):** `z-[1]`
- **Mobile Menu Toggle:** `z-10`
- **Sticky Header:** `z-20`
- **Header Dropdown Menus:** `z-30`

---

## 7. Do's and Don'ts

### Do
- **Use the repeating vertical line background (`.bg-repeating-lines`)** on the `<main>` wrapper to preserve Arbitrum's signature architectural grid rhythm.
- **Alternate white (`#FFFFFF`) and Stone White (`#F2F2F2`) sections** to create clear visual pacing without heavy horizontal borders.
- **Pair `Aeonik Fono` uppercase eyebrows** with `Aeonik` (`36px`, `font-medium`, `leading-extra-tight`) for all standard section headers.
- **Reserve `FK Screamer` for uppercase hero headlines and 3D scene cards** where condensed vertical punch is required.
- **Include the sliding arrow interaction (`group-hover:translate-x-1`) and cyan glow (`group-hover:shadow-button-glow`)** on all pill-shaped directional links.
- **Keep text selection styled in neon cyan (`selection:bg-[#10e1ff]`)** across the document body.

### Don't
- **Don't use static drop shadows on resting cards**; cards sit flat on `#F2F2F2` or `#FFFFFF` and only elevate on hover (`.shadow-card-hover`).
- **Don't use `FK Screamer` for body copy, navigation, or long-form text**—its ultra-condensed proportions are strictly for display impact.
- **Don't introduce arbitrary border radii for buttons**; stick to `rounded-[40px]` / `rounded-3xl` for marketing pills and `rounded-md` / `rounded-lg` for compact UI controls and tabs.
- **Don't forget to counter-rotate items** inside rotating orbital rings, or logos and labels will turn upside down.
- **Don't use inline styles for layout or static theming**; reserve inline `style` attributes strictly for dynamic coordinates (e.g., 3D transforms, orbital trigonometry, and staircase cube positions).

---

## 8. Responsive Behavior

### Breakpoints

| Breakpoint | Width | Key Layout & Component Changes |
|------------|-------|--------------------------------|
| Mobile (`default` / `<640px`) | `320px–639px` | Single-column stacks; header is solid `#FFFFFF` with hamburger menu (`top-[62px] h-[calc(100vh-62px)]`); Hero height fixed at `736px` (`h-mobile-hero`) with `text-6xl` H1 and full-width CTAs; 3D cube scales to `scale-75`; `TechSection` hides scattered lego cubes (`max-sm:hidden`) and adds bottom padding (`pb-[340px]`) |
| Small / Tablet (`sm:` `640px+`, `md:` `768px+`) | `640px–1023px` | `EcosystemTabs` shifts to 2 columns (`sm:grid-cols-2`); `ScenePanels` shifts to 3 columns (`md:grid-cols-3`); Hero H1 scales to `md:text-8xl`; Footer expands to 4 columns (`md:grid-cols-4`) and reveals the full-width `all_on_arbitrum.svg` banner |
| Desktop (`lg:` `1024px+`) | `1024px–1439px` | Header becomes transparent at top (`lg:bg-transparent`) with inline horizontal nav and centered popovers; Hero height becomes `lg:h-[calc(100vh-84px)]` with `lg:text-9xl` H1; `FlexiblePlatform` splits into 2 columns (`lg:grid-cols-2`); `EcosystemTabs` expands to 4 columns (`lg:grid-cols-4`); `FinalCta` aligns horizontally (`lg:flex-row`); Footer expands to 7 columns (`lg:grid-cols-7`) |
| Wide (`xl:` `1280px+` / `1440px`) | `1280px+` | Outer container caps at `max-w-[1440px]`; Hero CTA buttons widen to `xl:w-[250px]`; Stat value rows scale height dynamically (`xl:h-[5vw]`) |

### Touch & Mobile Adaptations
- **Navigation Drawer:** Full-viewport overlay (`h-[calc(100vh-62px)] overflow-auto bg-white`) with accordion-style vertical expansion for the 5 nav groups and bottom-pinned social icons.
- **Overflow Protection:** `.page-wrapper` enforces `overflow-hidden` so `1440px` orbital rings and infinite marquees never cause horizontal page scroll on mobile viewports.

---

## 9. Agent Prompt Guide

### Quick Color Reference
- **Primary Brand Navy (Text & Dark Surfaces):** `#05163D` (`--primary-blue-1` / `text-primary-blue-1`)
- **Primary Brand Blue (Hero Gradient Top):** `#016BE5` (`--primary-blue-2` / `bg-arb-blue`)
- **Neon Cyan Accent (Glows, Focus, Selection):** `#10E1FF` (`--primary-blue-3` / `text-primary-blue-3`)
- **High-Voltage Yellow (Jumbo CTA Hover & Tech Gradient):** `#FFE103` (`--yellow` / `bg-yellow`)
- **Page Background:** `#FFFFFF` with `.bg-repeating-lines`
- **Secondary Section & Card Surface:** `#F2F2F2` (`--stone-white` / `bg-stone-white`)
- **Default Border / Divider:** `#E7E7E7` (`--border`) or `rgba(5, 22, 61, 0.1–0.2)`

### Iteration & Implementation Guide
1. **Page Shell Setup:** Wrap the page in `<main className="bg-repeating-lines"><div className="m-auto flex max-w-[1440px] flex-col">`. Keep `.page-wrapper` set to `overflow-hidden`.
2. **Header Pattern:** Implement a `sticky top-0 z-20` header that listens to `window.scrollY > 0` to toggle `lg:bg-white lg:shadow-[0_1px_2px_rgba(0,0,0,0.05)]`. Include the dual-layer logo crossfade on `group-hover`.
3. **Hero Construction:** Use a `rounded-[30px]` container with `bg-[linear-gradient(180deg,#016be5_0%,#05163d_100%)]`, layered with a texture image, staggered `.hero-path` SVG scan lines, `font-screamer uppercase` headline (`text-6xl md:text-8xl lg:text-9xl lg:!leading-[0.9]`), and black `rounded-3xl` pill CTAs with `hover:text-primary-blue-3 hover:shadow-button-glow`.
4. **Section Rhythm:** Alternate open grid sections (transparent over `.bg-repeating-lines`, `py-30`) with full-width `#F2F2F2` (`bg-stone-white py-30`) sections.
5. **Section Headers:** Always stack:
   - Eyebrow: `<p className="mb-2 font-aeonik-fono text-base uppercase text-primary-blue-1">`
   - Title: `<h2 className="mb-6 text-4xl font-medium leading-extra-tight text-primary-blue-1">`
   - Subtitle: `<p className="m-auto max-w-[670px] text-md opacity-60">`
6. **Pill CTA Links:** Use `group inline-flex items-center justify-center gap-2 rounded-[40px] border border-primary-blue-1/20 px-5 py-2 text-sm font-semibold text-primary-blue-1 backdrop-blur transition-all` with a trailing `24px` arrow inside `<span className="h-6 w-6 transition-transform group-hover:translate-x-1 group-hover:shadow-button-glow">`.
7. **Footer Construction:** Use `.bg-footer` (`linear-gradient(#05163d 0%, #000000 100%)`), uppercase white typography, a 7-column responsive link grid (`lg:grid-cols-7`), cyan hover states (`hover:text-primary-blue-3`), and a full-width bottom brand SVG banner.
