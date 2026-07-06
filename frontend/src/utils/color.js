// WCAG relative luminance (sRGB), per https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
function relativeLuminance(hex) {
  const clean = hex.replace('#', '')
  if (clean.length !== 6) return null
  const [r, g, b] = [0, 2, 4].map((i) => parseInt(clean.substring(i, i + 2), 16) / 255)
  const linearize = (c) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4)
  return 0.2126 * linearize(r) + 0.7152 * linearize(g) + 0.0722 * linearize(b)
}

// Label/badge backgrounds are arbitrary user-chosen colors (a native color
// picker with no restrictions) — hardcoding white text fails contrast on any
// pale or mid-toned choice. Picks whichever of white / ink text clears more
// contrast against the given background, so it degrades gracefully instead
// of assuming every color is dark enough for white text.
export function readableTextColor(backgroundHex, { light = '#ffffff', dark = '#0f172a' } = {}) {
  const luminance = relativeLuminance(backgroundHex)
  if (luminance === null) return light
  const contrastWithLight = 1.05 / (luminance + 0.05)
  const contrastWithDark = (luminance + 0.05) / 0.058816 // dark luminance (#0f172a) ≈ 0.008816
  return contrastWithLight >= contrastWithDark ? light : dark
}
