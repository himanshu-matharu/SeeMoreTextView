# SeeMoreTextView

An expandable text view with animation

<img src="preview.gif" alt="preview" height="auto" width="180px"/>

## Attributes
The SeeMoreTextView has the following attributes

| Attribute            |Format| Description | Default |
| -------------|------------- |------------|-----------|
| showTrimExpandedText      | boolean | Show a clickable span with the ellipsis to toggle textView state | true |
| viewExpandedText     | string | Text to show with ellipsis in expanded state | Show less |
| viewCollapsedText     | string  | Text to show with ellipsis in collapsed state | Show more |
| clickableTextColor       | color  | Color of clickable span with the ellipsis | #c0c0c0 |
| trimMode | enum | Choose trimModeLine to limit text using number of lines in collapsed state or trimModeLength to limit using number of characters | trimModeLine |
| trimLines  | dimension  | Number of lines allowed in collapsed state of textView | 2 |
| trimLength       | string | Number of characters allowed in collapsed state of textView | 105 |
