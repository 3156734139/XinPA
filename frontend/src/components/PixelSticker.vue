<template>
  <span
    class="pixel-sticker"
    :style="{
      width: size + 'px',
      height: size + 'px',
    }"
    v-bind="$attrs"
  >
    <svg viewBox="0 0 32 32" fill="none">
      <!-- 耳朵 -->
      <path d="M5 9 L3 1 L11 7Z" fill="#FFB3C6" opacity="0.9" />
      <path d="M27 9 L29 1 L21 7Z" fill="#FFB3C6" opacity="0.9" />
      <!-- 内耳 -->
      <path d="M5.5 8 L4 3 L10 7Z" fill="#FF8FA3" opacity="0.5" />
      <path d="M26.5 8 L28 3 L22 7Z" fill="#FF8FA3" opacity="0.5" />

      <!-- 头部 -->
      <circle cx="16" cy="17" r="12.5" fill="white" stroke="#FFB3C6" stroke-width="0.5" opacity="0.95" />

      <!-- 蝴蝶结 -->
      <path d="M3 12 L1 9 L6 12 L1 15 Z" fill="#FF6B8A" opacity="0.85" />
      <path d="M3 12 L6 9 L1 12 L6 15 Z" fill="#FF6B8A" opacity="0.85" />
      <circle cx="3" cy="12" r="1.2" fill="#E85570" opacity="0.9" />

      <!-- 眼睛 -->
      <ellipse cx="11" cy="16" rx="1.8" ry="2.5" fill="#333" opacity="0.85" />
      <ellipse cx="21" cy="16" rx="1.8" ry="2.5" fill="#333" opacity="0.85" />
      <!-- 眼睛高光 -->
      <circle cx="11.8" cy="14.8" r="0.8" fill="white" />
      <circle cx="21.8" cy="14.8" r="0.8" fill="white" />

      <!-- 鼻子 -->
      <ellipse cx="16" cy="19" rx="1.5" ry="1" fill="#FFB3C6" opacity="0.85" />

      <!-- 胡须 -->
      <line x1="4" y1="17" x2="9" y2="19" stroke="#DDD" stroke-width="0.5" opacity="0.6" />
      <line x1="4" y1="20" x2="9" y2="20" stroke="#DDD" stroke-width="0.5" opacity="0.6" />
      <line x1="4" y1="23" x2="9" y2="21" stroke="#DDD" stroke-width="0.5" opacity="0.6" />
      <line x1="28" y1="17" x2="23" y2="19" stroke="#DDD" stroke-width="0.5" opacity="0.6" />
      <line x1="28" y1="20" x2="23" y2="20" stroke="#DDD" stroke-width="0.5" opacity="0.6" />
      <line x1="28" y1="23" x2="23" y2="21" stroke="#DDD" stroke-width="0.5" opacity="0.6" />

      <!-- 腮红 -->
      <ellipse cx="7" cy="21" rx="2.5" ry="1.5" fill="#FFB3C6" opacity="0.25" />
      <ellipse cx="25" cy="21" rx="2.5" ry="1.5" fill="#FFB3C6" opacity="0.25" />
    </svg>
  </span>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  size?: number;
}>(), {
  size: 32,
});
</script>

<style scoped>
.pixel-sticker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  pointer-events: none;
  user-select: none;
}

.pixel-sticker svg {
  width: 100%;
  height: 100%;
  display: block;
  filter: drop-shadow(0 1px 2px rgba(255, 107, 138, 0.15));
}
</style>
