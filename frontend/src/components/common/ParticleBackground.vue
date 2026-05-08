<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  count: { type: Number, default: 60 },
  speed: { type: Number, default: 0.5 },
  color: { type: String, default: 'rgba(26,115,232,0.22)' },
  lineColor: { type: String, default: 'rgba(26,115,232,0.12)' },
  maxDistance: { type: Number, default: 120 }
})

const canvasRef = ref(null)
let animationId = null
let particles = []
let mouse = { x: -999, y: -999 }

function init(canvas) {
  canvas.width = canvas.offsetWidth
  canvas.height = canvas.offsetHeight
  particles = []
  for (let i = 0; i < props.count; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * props.speed,
      vy: (Math.random() - 0.5) * props.speed,
      radius: Math.random() * 1.5 + 0.5
    })
  }
}

function draw(canvas, ctx) {
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  for (let i = 0; i < particles.length; i++) {
    const p = particles[i]
    p.x += p.vx
    p.y += p.vy

    if (p.x < 0 || p.x > canvas.width) p.vx *= -1
    if (p.y < 0 || p.y > canvas.height) p.vy *= -1

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = props.color
    ctx.fill()

    for (let j = i + 1; j < particles.length; j++) {
      const q = particles[j]
      const dx = p.x - q.x
      const dy = p.y - q.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < props.maxDistance) {
        ctx.beginPath()
        ctx.strokeStyle = props.lineColor.replace(/[\d.]+\)$/, (props.maxDistance - dist) / props.maxDistance * 0.5 + ')')
        ctx.lineWidth = 0.5
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(q.x, q.y)
        ctx.stroke()
      }
    }
  }

  animationId = requestAnimationFrame(() => draw(canvas, ctx))
}

function handleMouseMove(e) {
  const canvas = canvasRef.value
  if (!canvas) return
  const rect = canvas.getBoundingClientRect()
  mouse.x = e.clientX - rect.left
  mouse.y = e.clientY - rect.top
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  init(canvas)
  const ctx = canvas.getContext('2d')
  draw(canvas, ctx)
  canvas.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('resize', () => init(canvas))
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  const canvas = canvasRef.value
  if (canvas) canvas.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style scoped>
.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: auto;
  z-index: 0;
}
</style>
