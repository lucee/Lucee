const fs = require('fs-extra');
const path = require('path');

async function generateIndex() {
  const recipesDir = path.join(__dirname, '../../docs/recipes');
  const outputPath = path.join(recipesDir, 'index.json');
  const files = await fs.readdir(recipesDir);
  const index = [];

  for (const file of files) {
    if (file.endsWith('.md')) {
      const filePath = path.join(recipesDir, file);
      const content = await fs.readFile(filePath, 'utf-8');
      const titleMatch = content.match(/^#\s+(.+)$/m);
      const title = titleMatch ? titleMatch[1] : 'Untitled';
      index.push({
        file: file,
        title: title,
        path: `/docs/recipes/${file}`,
      });
    }
  }

  await fs.writeJson(outputPath, index, { spaces: 2 });
  console.log(`Index written to ${outputPath}`);
}

generateIndex().catch(err => {
  console.error(err);
  process.exit(1);
});