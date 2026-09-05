import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";

const outputDir = path.resolve("app/src/main/res/raw");
const yellow = [0.9804, 0.749, 0.2706, 1];
const green = [0.349, 0.6784, 0.3686, 1];
const black = [0.0784, 0.0784, 0.0784, 1];
const white = [1, 1, 1, 1];
const easeOut = { i: { x: [0.67], y: [1] }, o: { x: [0.33], y: [0] } };

function staticTransform(position = [0, 0, 0], anchor = [0, 0, 0], scale = [100, 100, 100]) {
  return {
    o: { a: 0, k: 100 },
    r: { a: 0, k: 0 },
    p: { a: 0, k: position },
    a: { a: 0, k: anchor },
    s: { a: 0, k: scale },
  };
}

function shapeLayer({ ind, name, shapes, transform, ip = 0, op = 108 }) {
  return {
    ddd: 0,
    ind,
    ty: 4,
    nm: name,
    sr: 1,
    ks: transform,
    ao: 0,
    shapes,
    ip,
    op,
    st: 0,
    bm: 0,
  };
}

function numberLayer(ind) {
  const pathData = {
    i: [[0, 1.5], [-2.2, 1.3], [0, 0], [-2.3, -1.1], [0, -2.6], [0, 0], [3.3, 0], [0, 0], [0, 3.3], [0, 0], [0, 0], [2, 2.2], [0, 1.5]],
    o: [[0, -2.5], [0, 0], [2.3, -1.3], [2.3, 1.1], [0, 0], [0, 3.3], [0, 0], [-3.3, 0], [0, 0], [0, 0], [-2.6, 1.5], [-1, -1.1], [0, 0]],
    v: [[34, 35], [37.6, 29.4], [59.8, 16.8], [67.3, 16.4], [71, 22.4], [71, 84], [65, 90], [57.6, 90], [51.6, 84], [51.6, 40.2], [43.5, 44.9], [35.6, 43.7], [34, 39.6]],
    c: true,
  };
  const transform = staticTransform([500, 500, 0], [54, 54, 0], [350, 350, 100]);
  transform.o = {
    a: 1,
    k: [
      { t: 7, s: [0], e: [100], ...easeOut },
      { t: 16, s: [100] },
    ],
  };
  transform.s = {
    a: 1,
    k: [
      { t: 7, s: [297.5, 297.5, 100], e: [350, 350, 100], ...easeOut },
      { t: 16, s: [350, 350, 100] },
    ],
  };
  return shapeLayer({
    ind,
    name: "Number_1",
    transform,
    shapes: [
      { ty: "sh", nm: "A1_Number_Path", ks: { a: 0, k: pathData } },
      { ty: "fl", nm: "Brand_Black", c: { a: 0, k: black }, o: { a: 0, k: 100 }, r: 1 },
    ],
  });
}

function animatedPosition() {
  return {
    a: 1,
    k: [
      { t: 0, s: [598, 584, 0], e: [598, 584, 0], ...easeOut },
      { t: 24, s: [598, 584, 0], e: [675, 555, 0], ...easeOut },
      { t: 36, s: [675, 555, 0], e: [670, 475, 0], ...easeOut },
      { t: 45, s: [670, 475, 0], e: [625, 500, 0], ...easeOut },
      { t: 52, s: [625, 500, 0] },
    ],
  };
}

function dotLayer(ind) {
  const transform = staticTransform([598, 584, 0]);
  transform.p = animatedPosition();
  transform.s = {
    a: 1,
    k: [
      { t: 8, s: [100, 100, 100], e: [125, 125, 100], ...easeOut },
      { t: 16, s: [125, 125, 100], e: [105, 105, 100], ...easeOut },
      { t: 24, s: [105, 105, 100], e: [100, 100, 100], ...easeOut },
      { t: 30, s: [100, 100, 100] },
    ],
  };
  return shapeLayer({
    ind,
    name: "Status_Point",
    transform,
    shapes: [
      { ty: "el", nm: "Status_Point_Circle", p: { a: 0, k: [0, 0] }, s: { a: 0, k: [84, 84] } },
      {
        ty: "fl",
        nm: "Status_Color_Yellow_To_Green",
        c: {
          a: 1,
          k: [
            { t: 0, s: yellow, e: yellow, ...easeOut },
            { t: 52, s: yellow, e: green, ...easeOut },
            { t: 68, s: green },
          ],
        },
        o: { a: 0, k: 100 },
        r: 1,
      },
    ],
  });
}

function glowLayer(ind) {
  const transform = staticTransform([598, 584, 0]);
  transform.p = animatedPosition();
  transform.o = {
    a: 1,
    k: [
      { t: 7, s: [0], e: [42], ...easeOut },
      { t: 16, s: [42], e: [25], ...easeOut },
      { t: 68, s: [25], e: [0], ...easeOut },
      { t: 92, s: [0] },
    ],
  };
  transform.s = {
    a: 1,
    k: [
      { t: 7, s: [30, 30, 100], e: [150, 150, 100], ...easeOut },
      { t: 20, s: [150, 150, 100], e: [112, 112, 100], ...easeOut },
      { t: 52, s: [112, 112, 100], e: [135, 135, 100], ...easeOut },
      { t: 92, s: [135, 135, 100] },
    ],
  };
  return shapeLayer({
    ind,
    name: "Glow",
    transform,
    shapes: [
      { ty: "el", nm: "Soft_Glow", p: { a: 0, k: [0, 0] }, s: { a: 0, k: [126, 126] } },
      { ty: "fl", nm: "Glow_Color", c: { a: 0, k: [1, 0.9137, 0.6588, 1] }, o: { a: 0, k: 100 }, r: 1 },
    ],
  });
}

function orbitLayer(ind) {
  const transform = staticTransform();
  transform.o = {
    a: 1,
    k: [
      { t: 23, s: [0], e: [80], ...easeOut },
      { t: 28, s: [80], e: [55], ...easeOut },
      { t: 52, s: [55], e: [0], ...easeOut },
      { t: 68, s: [0] },
    ],
  };
  const orbit = {
    i: [[0, 0], [-34, 18], [4, 42], [20, -10]],
    o: [[34, -8], [10, -42], [-24, 0], [0, 0]],
    v: [[598, 584], [675, 555], [670, 475], [625, 500]],
    c: false,
  };
  return shapeLayer({
    ind,
    name: "Orbit_Path",
    transform,
    shapes: [
      { ty: "sh", nm: "Half_Orbit", ks: { a: 0, k: orbit } },
      { ty: "st", nm: "Orbit_Stroke", c: { a: 0, k: yellow }, o: { a: 0, k: 100 }, w: { a: 0, k: 18 }, lc: 2, lj: 2 },
      {
        ty: "tm",
        nm: "Orbit_Draw",
        s: { a: 0, k: 0 },
        e: {
          a: 1,
          k: [
            { t: 24, s: [0], e: [100], ...easeOut },
            { t: 52, s: [100] },
          ],
        },
        o: { a: 0, k: 0 },
        m: 1,
      },
    ],
  });
}

function checkLayer(ind) {
  const transform = staticTransform([625, 500, 0]);
  transform.s = { a: 0, k: [115, 115, 100] };
  transform.o = {
    a: 1,
    k: [
      { t: 51, s: [0], e: [100], ...easeOut },
      { t: 60, s: [100] },
    ],
  };
  return shapeLayer({
    ind,
    name: "Success_Check",
    transform,
    shapes: [
      { ty: "sh", nm: "Check_Path", ks: { a: 0, k: { i: [[0, 0], [0, 0], [0, 0]], o: [[0, 0], [0, 0], [0, 0]], v: [[-15, 0], [-4, 12], [17, -13]], c: false } } },
      { ty: "st", nm: "Check_Stroke", c: { a: 0, k: white }, o: { a: 0, k: 100 }, w: { a: 0, k: 9 }, lc: 2, lj: 2 },
      {
        ty: "tm",
        nm: "Check_Draw",
        s: { a: 0, k: 0 },
        e: {
          a: 1,
          k: [
            { t: 52, s: [0], e: [100], ...easeOut },
            { t: 64, s: [100] },
          ],
        },
        o: { a: 0, k: 0 },
        m: 1,
      },
    ],
  });
}

function particleLayer(ind, particle, milestone) {
  const startFrame = particle.late ? 54 : 13;
  const peakFrame = particle.late ? 64 : 22;
  const endFrame = particle.late ? 88 : 38;
  const origin = particle.late ? [625, 500, 0] : [598, 584, 0];
  const destination = [origin[0] + particle.dx, origin[1] + particle.dy, 0];
  const transform = staticTransform(origin);
  transform.p = {
    a: 1,
    k: [
      { t: startFrame, s: origin, e: destination, ...easeOut },
      { t: endFrame, s: destination },
    ],
  };
  transform.o = {
    a: 1,
    k: [
      { t: startFrame - 1, s: [0], e: [85], ...easeOut },
      { t: peakFrame, s: [85], e: [0], ...easeOut },
      { t: endFrame, s: [0] },
    ],
  };
  transform.s = {
    a: 1,
    k: [
      { t: startFrame, s: [45, 45, 100], e: [110, 110, 100], ...easeOut },
      { t: peakFrame, s: [110, 110, 100], e: [75, 75, 100], ...easeOut },
      { t: endFrame, s: [75, 75, 100] },
    ],
  };
  return shapeLayer({
    ind,
    name: `Particle_${milestone}_${ind}`,
    transform,
    shapes: [
      { ty: "el", nm: "Particle_Dot", p: { a: 0, k: [0, 0] }, s: { a: 0, k: [particle.size, particle.size] } },
      { ty: "fl", nm: particle.late ? "Completion_Green" : "Completion_Yellow", c: { a: 0, k: particle.late ? green : yellow }, o: { a: 0, k: 100 }, r: 1 },
    ],
  });
}

const particles = [
  { dx: 72, dy: -48, size: 13, late: false },
  { dx: 28, dy: -92, size: 10, late: false },
  { dx: -52, dy: -70, size: 15, late: false },
  { dx: 86, dy: 28, size: 9, late: true },
  { dx: 40, dy: 82, size: 13, late: true },
  { dx: -62, dy: 56, size: 10, late: true },
  { dx: -88, dy: -16, size: 12, late: true },
  { dx: 12, dy: -104, size: 8, late: true },
];

function composition(fileName, milestone, particleCount) {
  const particleLayers = particles
    .slice(0, particleCount)
    .map((particle, index) => particleLayer(10 + index, particle, milestone));
  const layers = [
    checkLayer(1),
    dotLayer(2),
    ...particleLayers,
    numberLayer(3),
    orbitLayer(4),
    glowLayer(5),
  ];
  return {
    v: "5.12.2",
    fr: 60,
    ip: 0,
    op: 108,
    w: 1080,
    h: 1080,
    nm: `OneThing Complete ${milestone}`,
    ddd: 0,
    assets: [],
    layers,
    markers: [
      { tm: 0, cm: "button_press_compose", dr: 7 },
      { tm: 7, cm: "logo_enter", dr: 9 },
      { tm: 16, cm: "yellow_light", dr: 15 },
      { tm: 31, cm: "orbit", dr: 28 },
      { tm: 59, cm: "success", dr: 16 },
      { tm: 75, cm: "hold", dr: 24 },
      { tm: 99, cm: "return_home_compose", dr: 9 },
    ],
    meta: {
      generator: "OneThing tools/generate_completion_lottie.mjs",
      milestone,
      output: fileName,
      textLayers: false,
      externalImages: false,
    },
  };
}

const outputs = [
  ["complete_success.json", "base", 4],
  ["complete_streak_3.json", "streak_3", 5],
  ["complete_streak_7.json", "streak_7", 6],
  ["complete_streak_30.json", "streak_30", 8],
];

await mkdir(outputDir, { recursive: true });
for (const [fileName, milestone, particleCount] of outputs) {
  const document = composition(fileName, milestone, particleCount);
  const text = `${JSON.stringify(document)}\n`;
  if (text.includes('"ty":5')) throw new Error(`${fileName} contains a text layer`);
  if (text.length > 200_000) throw new Error(`${fileName} exceeds 200KB`);
  await writeFile(path.join(outputDir, fileName), text, "utf8");
  process.stdout.write(`${fileName} ${Buffer.byteLength(text)} bytes\n`);
}
