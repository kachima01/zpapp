# ZCMFF mobile design system

The app uses Jetpack Compose Material 3. Use `MaterialTheme.colorScheme` and the
tokens in `ui/theme/DesignTokens.kt`; do not introduce screen-local hex colours,
component heights, or spacing values unless a new token is first agreed.

## Layout

- Use the 4dp spacing scale: 4, 8, 12, 16, 24, 32.
- Page padding is 16dp on compact widths and 24dp on expanded layouts.
- Interactive targets are at least 48dp high. Standard input/button height is
  52dp.
- Use cards only to group related information. They are not a substitute for
  vertical spacing or section headings.

## Colour and type

- `primary` is reserved for the primary action and active navigation.
- Success, warning, and error colours communicate status only; they are never
  decorative accents.
- Use `onSurface` for primary content and `onSurfaceVariant` for supporting
  content. Do not use colour as the only status signal.
- Use Material title/body/label styles. Monospace text is limited to case IDs,
  timestamps, and technical evidence values.

## Components and behaviour

- Use Material buttons, fields, switches, navigation items, dialogs, menus, and
  snackbars before building a custom clickable surface.
- Forms show a label, required state, contextual validation, and a clear saved
  or failed outcome. Destructive and draft-abandon actions require confirmation.
- All controls need enabled, disabled, focused, and pressed states plus a
  meaningful content description when an icon has no adjacent text label.
- Global navigation is shown only at top-level destinations. Focused workflows
  such as case creation and decision review use back navigation instead.

## Responsive rules

- Compact: one content column and bottom navigation.
- Medium/expanded: retain a readable content maximum and introduce a navigation
  rail or two-pane list/detail view before increasing card density.
- Never force two text inputs into one compact row when either label or expected
  value would become hard to read.
