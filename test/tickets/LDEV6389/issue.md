# LDEV-6389 parse multipart content in Claude response getAnswers()

## Type
Bug / Enhancement

## Component
AI / Claude

## Description
Claude API responses can return `content` as an array of typed blocks (`text`, `image`, `tool_use`, `thinking`, etc.), not only a plain string.

`ClaudeResponse.getAnswers()` and `isMultiPart()` currently stub out multipart support (`TODO`), while `getAnswer()` only concatenates `text` and `code` blocks. This mirrors the gap fixed for OpenAI in [LDEV-6387](https://luceeserver.atlassian.net/browse/LDEV-6387).

## Expected behaviour
- `getAnswers()` returns a `List<Part>` with one entry per content block
- `getAnswer()` concatenates text parts (same as OpenAI after LDEV-6387)
- `isMultiPart()` returns true when there are multiple parts or a non-text part
- Streaming responses accumulate text and binary parts where supported

## Reference
- [LDEV-6387](https://luceeserver.atlassian.net/browse/LDEV-6387) — OpenAI multipart parsing (`OpenAIResponseUtil`)
- [Anthropic Messages API — content blocks](https://docs.anthropic.com/en/api/messages)
