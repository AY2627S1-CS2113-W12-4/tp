/** Renders every maintained Markdown page once, then checks the resulting local links. */
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const { marked } = require('marked');
const source = path.resolve(process.argv[2] || 'docs');
const output = path.resolve(process.argv[3] || '_site');
if (source === output || source.startsWith(output + path.sep) || output.startsWith(source + path.sep)) {
  throw new Error('The website output must be separate from the source documents.');
}
const escape = text => text.replaceAll('&', '&amp;').replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const htmlPath = file => file.replace(/(^|\/)README\.md$/, '$1index.html').replace(/\.md$/, '.html');
const pages = [];
const manifest = {};
fs.mkdirSync(output, { recursive: true });
fs.mkdirSync(path.join(output, 'assets'), { recursive: true });
fs.copyFileSync(new URL('site.css', import.meta.url), path.join(output, 'assets/style.css'));

function render(relative) {
  const original = fs.readFileSync(path.join(source, relative));
  const markdown = original.toString('utf8');
  const destinations = new Map();
  const renderer = new marked.Renderer();
  renderer.heading = function (token) {
    const text = this.parser.parseInline(token.tokens);
    const base = text.replace(/<[^>]*>/g, '').toLowerCase()
      .replace(/[^\p{L}\p{N}_\s-]/gu, '').replace(/\s/g, '-');
    const count = destinations.get(base) || 0;
    destinations.set(base, count + 1);
    return `<h${token.depth} id="${base}${count ? '-' + count : ''}">${text}</h${token.depth}>\n`;
  };
  let content = marked.parse(markdown, { renderer });
  content = content.replace(/href="([^"#]+\.md)(#[^"]*)?"/g, (match, target, anchor = '') =>
    /^[a-z]+:|^\/\//i.test(target) ? match : `href="${htmlPath(target)}${anchor}"`);
  const destination = htmlPath(relative);
  const relativeRoot = path.posix.relative(path.posix.dirname(destination), '.') || '.';
  const title = markdown.match(/^#\s+(.+)$/m)?.[1] || 'Keyboard Warrior';
  const digest = crypto.createHash('sha256').update(original).digest('hex');
  manifest[relative] = { output: destination, sha256: digest };
  const document = `<!doctype html><html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>${escape(title)}</title>
<meta name="source-sha256" content="${digest}"><link rel="stylesheet" href="${relativeRoot}/assets/style.css">
</head><body><header><a class="brand" href="${relativeRoot}/index.html">Keyboard Warrior</a>
<a href="${relativeRoot}/UserGuide.html">User Guide</a><a href="${relativeRoot}/DeveloperGuide.html">Developer Guide</a>
<a href="${relativeRoot}/AboutUs.html">About Us</a></header><main>${content}
<footer>Keyboard Warrior v2.1 · Generated directly from the project’s Markdown documentation.</footer>
</main></body></html>`;
  fs.mkdirSync(path.dirname(path.join(output, destination)), { recursive: true });
  fs.writeFileSync(path.join(output, destination), document);
  pages.push(destination);
}

function walk(directory = '') {
  for (const entry of fs.readdirSync(path.join(source, directory), { withFileTypes: true })) {
    if (entry.name.startsWith('.') || entry.name === '_config.yml') continue;
    const relative = path.posix.join(directory, entry.name);
    if (entry.isDirectory()) walk(relative);
    else if (entry.name.endsWith('.md')) render(relative);
    else {
      fs.mkdirSync(path.dirname(path.join(output, relative)), { recursive: true });
      fs.copyFileSync(path.join(source, relative), path.join(output, relative));
    }
  }
}
walk();
fs.writeFileSync(path.join(output, '.nojekyll'), '');
fs.writeFileSync(path.join(output, 'source-manifest.json'), JSON.stringify(manifest, null, 2) + '\n');

if (!process.argv.includes('--skip-link-check')) {
  const errors = [];
  for (const page of pages) {
    const html = fs.readFileSync(path.join(output, page), 'utf8');
    for (const [, raw] of html.matchAll(/(?:href|src)="([^"]+)"/g)) {
      if (/^[a-z]+:|^\/\//i.test(raw)) continue;
      const [target, fragment] = raw.replaceAll('&amp;', '&').split('#');
      const resolved = target ? path.resolve(output, path.dirname(page), decodeURIComponent(target))
        : path.join(output, page);
      if (!resolved.startsWith(output + path.sep) || !fs.existsSync(resolved)) {
        errors.push(`${page}: missing ${raw}`);
      } else if (fragment && resolved.endsWith('.html')) {
        const destination = fs.readFileSync(resolved, 'utf8');
        if (!destination.includes(`id="${decodeURIComponent(fragment)}"`)
            && !destination.includes(`name="${decodeURIComponent(fragment)}"`)) {
          errors.push(`${page}: missing anchor ${raw}`);
        }
      }
    }
  }
  if (errors.length) throw new Error(errors.join('\n'));
}
console.log(`Built ${pages.length} pages directly from Markdown. Source hashes are recorded in source-manifest.json.`);
