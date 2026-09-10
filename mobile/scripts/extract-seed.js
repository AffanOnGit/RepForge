const fs = require('fs');
const path = require('path');

const srcPath = path.join(
  __dirname,
  '../../core/core-data/src/main/java/com/repforge/core/data/database/seed/ExerciseSeedData.kt'
);
const src = fs.readFileSync(srcPath, 'utf8');
const re =
  /ExerciseEntity\(\s*id\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)",\s*equipment\s*=\s*"([^"]+)",\s*primarySubMuscle\s*=\s*"([^"]+)",\s*secondarySubMuscles\s*=\s*"([^"]*)",\s*creatorTags\s*=\s*"([^"]*)"/g;

const exercises = [];
let m;
while ((m = re.exec(src)) !== null) {
  exercises.push({
    id: m[1],
    name: m[2],
    equipment: m[3],
    primarySubMuscle: m[4],
    secondarySubMuscles: m[5] ? m[5].split(',').filter(Boolean) : [],
    creatorTags: m[6] ? m[6].split(',').filter(Boolean) : [],
    isCustom: false,
  });
}

const outDir = path.join(__dirname, '../src/data/seed');
fs.mkdirSync(outDir, { recursive: true });
const out = `import type { Exercise } from '@/src/domain/types';

/** Ported from Android ExerciseSeedData (${exercises.length} canonical exercises). */
export const EXERCISE_SEED: Exercise[] = ${JSON.stringify(exercises, null, 2)} as Exercise[];
`;
fs.writeFileSync(path.join(outDir, 'exercises.ts'), out);
console.log('Wrote', exercises.length, 'exercises');
