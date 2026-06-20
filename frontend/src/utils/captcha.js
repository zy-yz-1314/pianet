/** 易混淆字符已排除：0 O 1 I l */
const CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

export function generateCaptchaCode(length = 4) {
  let s = "";
  for (let i = 0; i < length; i++) {
    s += CHARSET[Math.floor(Math.random() * CHARSET.length)];
  }
  return s;
}

/**
 * 在 canvas 上绘制干扰线与随机验证码（区分人机）
 */
export function drawCaptchaCanvas(canvas, code) {
  if (!canvas) return;
  const w = canvas.width;
  const h = canvas.height;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, w, h);

  ctx.fillStyle = "#f0f2f5";
  ctx.fillRect(0, 0, w, h);

  for (let i = 0; i < 6; i++) {
    ctx.strokeStyle = `rgba(${rand(80, 180)},${rand(80, 180)},${rand(80, 180)},0.45)`;
    ctx.lineWidth = rand(1, 2);
    ctx.beginPath();
    ctx.moveTo(rand(0, w), rand(0, h));
    ctx.lineTo(rand(0, w), rand(0, h));
    ctx.stroke();
  }

  for (let i = 0; i < 40; i++) {
    ctx.fillStyle = `rgba(${rand(0, 255)},${rand(0, 255)},${rand(0, 255)},0.35)`;
    ctx.beginPath();
    ctx.arc(rand(0, w), rand(0, h), rand(0.5, 1.5), 0, Math.PI * 2);
    ctx.fill();
  }

  const chars = code.split("");
  const step = w / (chars.length + 1);
  ctx.textBaseline = "middle";
  chars.forEach((ch, i) => {
    ctx.save();
    ctx.translate(step * (i + 1), h / 2);
    ctx.rotate((rand(-28, 28) * Math.PI) / 180);
    ctx.font = `bold ${rand(22, 28)}px Consolas, "Courier New", monospace`;
    ctx.fillStyle = `rgb(${rand(20, 90)},${rand(20, 90)},${rand(20, 90)})`;
    ctx.fillText(ch, 0, 0);
    ctx.restore();
  });
}

function rand(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
