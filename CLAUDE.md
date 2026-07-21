## Output format
For any artifact intended for human consumption (reports, briefings, summaries, 
plans, retros, status updates, memos, decks):
- Default to a single self-contained HTML file with inline CSS
- Use semantic HTML (h1, h2, sections) and embedded styles
- Visual style: clean typography, generous whitespace, restrained color use
- Always include a print-friendly stylesheet (@media print)
- Light Mode is the default.
- File extension .html, save in artifacts/ alongside the markdown source
For reference material that will only be edited (specs, configs, READMEs, 
project notes): keep markdown as the primary format.
When unsure: ask "is this for someone to read or someone to edit?" 
Read = HTML. Edit = markdown.