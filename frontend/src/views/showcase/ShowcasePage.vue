<template>
  <div class="showcase-page">
    <!-- ===== 顶部导航栏 ===== -->
    <nav class="navbar" :class="{ 'navbar-scrolled': scrolled }">
      <div class="navbar-inner">
        <div class="navbar-brand">
          <div class="brand-avatar">
            <span class="brand-avatar-text">兔</span>
          </div>
          <div class="brand-info">
            <span class="brand-name">小兔酱</span>
            <span class="brand-sig">✨ 你的专属游戏搭子 ✨</span>
          </div>
        </div>
        <div class="navbar-links" :class="{ 'nav-open': mobileMenuOpen }">
          <a href="#hero" class="nav-link active" @click.prevent="scrollTo('hero')">首页</a>
          <a href="#services" class="nav-link" @click.prevent="scrollTo('services')">服务介绍</a>
          <a href="#gallery" class="nav-link" @click.prevent="scrollTo('gallery')">战绩相册</a>
          <a href="#reviews" class="nav-link" @click.prevent="scrollTo('reviews')">用户评价</a>
          <a href="#booking" class="nav-link nav-link-cta" @click.prevent="openBooking">在线预约</a>
        </div>
        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
          <span></span><span></span><span></span>
        </button>
      </div>
    </nav>

    <!-- ===== 个人信息头部区 ===== -->
    <section id="hero" class="hero-section">
      <div class="hero-bg-deco">
        <span class="deco-star deco-1">✦</span>
        <span class="deco-star deco-2">✧</span>
        <span class="deco-star deco-3">♡</span>
        <span class="deco-star deco-4">✿</span>
      </div>
      <div class="hero-container">
        <!-- 头像 -->
        <div class="hero-avatar-frame">
          <div class="hero-avatar-inner">
            <span class="hero-avatar-text">兔</span>
          </div>
          <div class="avatar-badge">在线</div>
        </div>
        <!-- 昵称/签名 -->
        <h1 class="hero-name">小兔酱</h1>
        <p class="hero-sig">温柔甜妹｜全能陪玩｜可盐可甜</p>
        <!-- 标签组 -->
        <div class="hero-tags">
          <span class="tag tag-game">王者荣耀</span>
          <span class="tag tag-game">英雄联盟</span>
          <span class="tag tag-game">原神</span>
          <span class="tag tag-voice">萝莉音</span>
          <span class="tag tag-voice">甜妹</span>
          <span class="tag tag-rank">最强王者</span>
          <span class="tag tag-rank">钻石I</span>
        </div>
        <!-- 价格卡 -->
        <div class="hero-price-card">
          <div class="price-item">
            <span class="price-label">时薪</span>
            <span class="price-value"><small>¥</small>30</span>
          </div>
          <div class="price-divider"></div>
          <div class="price-item">
            <span class="price-label">通宵</span>
            <span class="price-value"><small>¥</small>150</span>
          </div>
          <div class="price-divider"></div>
          <div class="price-item">
            <span class="price-label">月卡</span>
            <span class="price-value"><small>¥</small>699</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 陪玩服务板块 ===== -->
    <section id="services" class="section services-section">
      <div class="section-header">
        <h2 class="section-title"><span class="title-deco">♡</span> 陪玩服务 <span class="title-deco">♡</span></h2>
        <p class="section-sub">多种套餐 · 暖心陪伴</p>
      </div>
      <div class="services-grid">
        <div v-for="svc in services" :key="svc.id" class="service-card" @click="openBooking">
          <div class="service-icon">{{ svc.icon }}</div>
          <h3 class="service-game">{{ svc.game }}</h3>
          <p class="service-desc">{{ svc.desc }}</p>
          <div class="service-meta">
            <span class="service-duration">{{ svc.duration }}</span>
            <span class="service-price"><small>¥</small>{{ svc.price }}</span>
          </div>
          <button class="service-btn">立即预约</button>
        </div>
      </div>
    </section>

    <!-- ===== 战绩相册区 ===== -->
    <section id="gallery" class="section gallery-section">
      <div class="section-header">
        <h2 class="section-title"><span class="title-deco">✧</span> 战绩相册 <span class="title-deco">✧</span></h2>
        <p class="section-sub">高光时刻 · 实力见证</p>
      </div>
      <div class="gallery-masonry">
        <div v-for="(img, idx) in gallery" :key="idx" class="gallery-card" :style="{ '--aspect': img.ratio }">
          <div class="gallery-img" :style="{ background: img.color }">
            <span class="gallery-emoji">{{ img.emoji }}</span>
          </div>
          <div class="gallery-label">{{ img.label }}</div>
        </div>
      </div>
    </section>

    <!-- ===== 用户评价区 ===== -->
    <section id="reviews" class="section reviews-section">
      <div class="section-header">
        <h2 class="section-title"><span class="title-deco">☆</span> 用户评价 <span class="title-deco">☆</span></h2>
        <p class="section-sub">听听他们怎么说</p>
      </div>
      <div class="reviews-grid">
        <div v-for="rv in reviews" :key="rv.id" class="review-card">
          <div class="review-user">
            <div class="review-avatar" :style="{ background: rv.avatarColor }">
              {{ rv.name.charAt(0) }}
            </div>
            <div class="review-user-info">
              <span class="review-name">{{ rv.name }}</span>
              <div class="review-stars">
                <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= rv.stars }">♡</span>
              </div>
            </div>
          </div>
          <p class="review-text">{{ rv.text }}</p>
          <span class="review-game">来自 {{ rv.game }} 玩家</span>
        </div>
      </div>
    </section>

    <!-- ===== 底部区域 ===== -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4 class="footer-title">🌸 联系方式</h4>
          <p>QQ: 123456789</p>
          <p>微信: xiaotu_0520</p>
          <p>YY频道: 5201314</p>
        </div>
        <div class="footer-col">
          <h4 class="footer-title">🕊 接单时间</h4>
          <p>周一至周五：19:00 - 01:00</p>
          <p>周六周日：14:00 - 03:00</p>
        </div>
        <div class="footer-col">
          <h4 class="footer-title">🎀 关于我</h4>
          <p>游戏时长 5000h+</p>
          <p>五星好评 99%</p>
          <p>诚信陪玩 · 温暖在线</p>
        </div>
      </div>
      <div class="footer-copy">
        <p>© 2025 小兔酱 · 用心陪伴每一局</p>
      </div>
    </footer>

    <!-- ===== 在线预约弹窗 ===== -->
    <el-dialog
      v-model="bookingVisible"
      title="🌸 在线预约"
      width="480px"
      top="8vh"
      class="booking-dialog"
      :close-on-click-modal="true"
      destroy-on-close
    >
      <el-form
        ref="bookingFormRef"
        :model="bookingForm"
        :rules="bookingRules"
        label-position="top"
        class="booking-form"
      >
        <el-form-item label="选择游戏" prop="game">
          <el-select v-model="bookingForm.game" placeholder="请选择游戏" style="width:100%">
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="英雄联盟" value="英雄联盟" />
            <el-option label="原神" value="原神" />
            <el-option label="绝地求生" value="绝地求生" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务时长" prop="duration">
          <el-select v-model="bookingForm.duration" placeholder="请选择时长" style="width:100%">
            <el-option label="1小时 (¥30)" value="1h" />
            <el-option label="2小时 (¥55)" value="2h" />
            <el-option label="3小时 (¥80)" value="3h" />
            <el-option label="通宵包夜 (¥150)" value="overnight" />
          </el-select>
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="bookingForm.contact" placeholder="微信号 / QQ号 / 手机号" maxlength="40" />
        </el-form-item>

        <el-form-item label="留言" prop="message">
          <el-input
            v-model="bookingForm.message"
            type="textarea"
            :rows="3"
            placeholder="想说的话或特殊要求…"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <button class="dialog-btn dialog-btn-cancel" @click="bookingVisible = false">再想想</button>
          <button class="dialog-btn dialog-btn-submit" :disabled="bookingLoading" @click="handleSubmitBooking">
            {{ bookingLoading ? '提交中…' : '♡ 确认预约 ♡' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 爱心确认弹窗（使用 Element Plus MessageBox） -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance } from 'element-plus';

/* ===== 类型定义 ===== */
interface ServiceItem {
  id: number;
  icon: string;
  game: string;
  desc: string;
  duration: string;
  price: number;
}

interface GalleryItem {
  emoji: string;
  color: string;
  label: string;
  ratio: string;
}

interface ReviewItem {
  id: number;
  name: string;
  avatarColor: string;
  stars: number;
  text: string;
  game: string;
}

interface BookingFormModel {
  game: string;
  duration: string;
  contact: string;
  message: string;
}

/* ===== 响应式状态 ===== */
const scrolled = ref(false);
const mobileMenuOpen = ref(false);
const bookingVisible = ref(false);
const bookingLoading = ref(false);
const bookingFormRef = ref<FormInstance>();

/* ===== 模拟数据 ===== */
const services = ref<ServiceItem[]>([
  { id: 1, icon: '👑', game: '王者荣耀', desc: '排位上分 · 温柔指导 · 不喷不闹', duration: '1小时', price: 30 },
  { id: 2, icon: '💫', game: '英雄联盟', desc: '灵活组排 · 补位全能 · 语音陪伴', duration: '1小时', price: 35 },
  { id: 3, icon: '🌸', game: '原神', desc: '周本boss · 材料收集 · 闲聊养老', duration: '1小时', price: 25 },
  { id: 4, icon: '🎯', game: '绝地求生', desc: '钢枪苟分 · 战术指挥 · 快乐吃鸡', duration: '1小时', price: 30 },
  { id: 5, icon: '🌙', game: '通宵陪伴', desc: '通宵畅玩 · 多游切换 · 治愈声线', duration: '包夜', price: 150 },
  { id: 6, icon: '🎀', game: '闲聊陪伴', desc: '不玩游戏也可以 · 陪你聊天解闷', duration: '1小时', price: 20 },
]);

const gallery = ref<GalleryItem[]>([
  { emoji: '🏆', color: 'linear-gradient(135deg, #FFD6E0, #FFE8EF)', label: '王者50星巅峰赛', ratio: '3/4' },
  { emoji: '🌟', color: 'linear-gradient(135deg, #E8DFF5, #F3EEFB)', label: '五杀精彩时刻', ratio: '1/1' },
  { emoji: '🎮', color: 'linear-gradient(135deg, #FFE4E8, #FFF0F3)', label: 'LOL完美战绩', ratio: '4/3' },
  { emoji: '✨', color: 'linear-gradient(135deg, #F0E6FF, #F8F0FF)', label: '原神满星深渊', ratio: '3/4' },
  { emoji: '💖', color: 'linear-gradient(135deg, #FFDEE4, #FFE9ED)', label: '赛季结算荣誉', ratio: '1/1' },
  { emoji: '🎯', color: 'linear-gradient(135deg, #E6F0FF, #F0F5FF)', label: '吃鸡20杀记录', ratio: '4/3' },
  { emoji: '🌈', color: 'linear-gradient(135deg, #FFE8F0, #FFF0F5)', label: '和老板的欢乐时刻', ratio: '1/1' },
  { emoji: '⭐', color: 'linear-gradient(135deg, #FFF3E0, #FFF8E7)', label: '千场70%胜率', ratio: '3/4' },
]);

const reviews = ref<ReviewItem[]>([
  { id: 1, name: '温柔一刀', avatarColor: '#FFD6E0', stars: 5, text: '声音超级甜！打游戏心态也很好，逆风局一直鼓励我，下次还点！', game: '王者荣耀' },
  { id: 2, name: '追风少年', avatarColor: '#E8DFF5', stars: 5, text: '技术真的好，带我连赢8把直接从钻石上王者，太强了叭', game: '英雄联盟' },
  { id: 3, name: '晚睡选手', avatarColor: '#FFE4E8', stars: 4, text: '很耐心的陪玩，聊天也很开心，不会冷场，推荐！', game: '原神' },
  { id: 4, name: '星星点点', avatarColor: '#F0E6FF', stars: 5, text: '通宵包夜太值了，又温柔又会照顾人，打游戏体验拉满', game: '绝地求生' },
]);

/* ===== 预约表单 ===== */
const bookingForm = reactive<BookingFormModel>({
  game: '',
  duration: '',
  contact: '',
  message: '',
});

const bookingRules = {
  game: [{ required: true, message: '请选择游戏', trigger: 'change' }],
  duration: [{ required: true, message: '请选择服务时长', trigger: 'change' }],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
};

/* ===== 方法 ===== */

/** 滚动到指定区块 */
function scrollTo(id: string) {
  mobileMenuOpen.value = false;
  const el = document.getElementById(id);
  if (el) {
    const top = el.getBoundingClientRect().top + window.scrollY - 70;
    window.scrollTo({ top, behavior: 'smooth' });
  }
}

/** 打开预约弹窗 */
function openBooking() {
  bookingVisible.value = true;
}

/** 提交预约 —— 模拟请求，实际对接后端 */
async function handleSubmitBooking() {
  const valid = await bookingFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  bookingLoading.value = true;
  try {
    // TODO: 对接后端 POST /api/bookings 提交预约
    // const res = await axios.post('/api/bookings', bookingForm);
    await new Promise(r => setTimeout(r, 1200));
    ElMessage.success('预约成功 ♡ 小兔酱会尽快联系你哦~');
    bookingVisible.value = false;
    bookingFormRef.value?.resetFields();
  } catch {
    ElMessage.error('提交失败，请稍后再试');
  } finally {
    bookingLoading.value = false;
  }
}

/* ===== 滚动监听 ===== */
function onScroll() {
  scrolled.value = window.scrollY > 60;
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll);
});
</script>

<style scoped>
/* ===== CSS 变量（马卡龙配色） ===== */
.showcase-page {
  --pink: #FFD6E0;
  --pink-dark: #FFB6C8;
  --pink-light: #FFE8EF;
  --pink-bg: #FFF5F7;
  --lavender: #E8DFF5;
  --lavender-light: #F3EEFB;
  --cream: #FFF9FB;
  --white: #FFFFFF;
  --text-primary: #5D4E6D;
  --text-secondary: #A890B0;
  --text-light: #C4B0CC;
  --shadow-soft: 0 4px 24px rgba(255, 182, 200, 0.18);
  --shadow-hover: 0 8px 32px rgba(255, 182, 200, 0.25);
  --radius: 16px;
  --radius-lg: 20px;
  --radius-xl: 24px;
  --font-cute: 'PingFang SC', 'Microsoft YaHei', 'Noto Sans SC', -apple-system, sans-serif;

  font-family: var(--font-cute);
  color: var(--text-primary);
  background: linear-gradient(180deg, var(--cream) 0%, var(--pink-bg) 50%, var(--cream) 100%);
  min-height: 100vh;
  overflow-x: hidden;
}

/* ===== 导航栏 ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.4s ease, box-shadow 0.4s ease;
  background: transparent;
}

.navbar-scrolled {
  background: rgba(255, 249, 251, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 2px 20px rgba(255, 182, 200, 0.12);
}

.navbar-inner {
  width: 100%;
  max-width: 1100px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 品牌区 */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink), var(--lavender));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--white);
  font-weight: 600;
  flex-shrink: 0;
}

.brand-info {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.brand-sig {
  font-size: 11px;
  color: var(--text-secondary);
  line-height: 1.2;
}

/* 导航链接 */
.navbar-links {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-link {
  text-decoration: none;
  font-size: 14px;
  color: var(--text-secondary);
  padding: 6px 14px;
  border-radius: var(--radius);
  transition: all 0.3s ease;
  cursor: pointer;
  font-weight: 500;
}

.nav-link:hover {
  color: var(--text-primary);
  background: rgba(255, 214, 224, 0.4);
}

.nav-link.active {
  color: #E8789A;
  background: rgba(255, 214, 224, 0.5);
}

.nav-link-cta {
  background: linear-gradient(135deg, var(--pink), var(--lavender)) !important;
  color: var(--text-primary) !important;
  font-weight: 600;
  padding: 6px 18px;
  border-radius: var(--radius);
}

.nav-link-cta:hover {
  transform: scale(1.04);
  box-shadow: var(--shadow-soft);
}

/* 移动端汉堡按钮 */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
}

.mobile-menu-btn span {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--text-primary);
  border-radius: 2px;
  transition: 0.3s;
}

/* ===== Hero 区块 ===== */
.hero-section {
  padding: 120px 24px 60px;
  text-align: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255, 214, 224, 0.15) 0%, transparent 100%);
}

/* 背景装饰 */
.hero-bg-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.deco-star {
  position: absolute;
  font-size: 20px;
  opacity: 0.25;
  animation: float 6s ease-in-out infinite;
}

.deco-1 { top: 15%; left: 10%; font-size: 24px; animation-delay: 0s; }
.deco-2 { top: 25%; right: 12%; font-size: 18px; animation-delay: 1.5s; }
.deco-3 { bottom: 30%; left: 8%; font-size: 16px; animation-delay: 3s; }
.deco-4 { bottom: 20%; right: 10%; font-size: 22px; animation-delay: 4.5s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-12px) rotate(8deg); }
}

.hero-container {
  max-width: 700px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 头像 */
.hero-avatar-frame {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  border-radius: 50%;
  padding: 4px;
  background: linear-gradient(135deg, var(--pink), var(--lavender));
  box-shadow: var(--shadow-soft);
}

.hero-avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink-light), var(--lavender-light));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-avatar-text {
  font-size: 40px;
  font-weight: 700;
  color: #E8789A;
}

.avatar-badge {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: #7BC47F;
  color: white;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
  border: 2px solid var(--white);
}

/* 昵称签名 */
.hero-name {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.hero-sig {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 20px;
}

/* 标签组 */
.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 28px;
}

.tag {
  display: inline-block;
  padding: 4px 14px;
  border-radius: var(--radius);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.tag-game {
  background: rgba(255, 214, 224, 0.5);
  color: #D4789A;
}

.tag-voice {
  background: rgba(232, 223, 245, 0.6);
  color: #8B7EAD;
}

.tag-rank {
  background: rgba(255, 228, 232, 0.5);
  color: #D48898;
}

/* 价格卡片 */
.hero-price-card {
  display: inline-flex;
  align-items: center;
  gap: 24px;
  background: var(--white);
  border-radius: var(--radius-xl);
  padding: 16px 32px;
  box-shadow: var(--shadow-soft);
}

.price-item {
  text-align: center;
}

.price-label {
  display: block;
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 2px;
}

.price-value {
  font-size: 24px;
  font-weight: 800;
  color: #E8789A;
}

.price-value small {
  font-size: 14px;
  font-weight: 600;
}

.price-divider {
  width: 1px;
  height: 36px;
  background: var(--pink);
  opacity: 0.3;
}

/* ===== 通用区块 ===== */
.section {
  padding: 60px 24px;
  max-width: 1100px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.title-deco {
  color: var(--pink-dark);
  font-size: 18px;
}

.section-sub {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* ===== 服务板块 ===== */
.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.service-card {
  background: var(--white);
  border-radius: var(--radius-xl);
  padding: 28px 24px;
  box-shadow: var(--shadow-soft);
  transition: all 0.35s ease;
  cursor: pointer;
  text-align: center;
}

.service-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.service-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.service-game {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.service-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 16px;
  line-height: 1.5;
}

.service-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.service-duration {
  font-size: 13px;
  color: var(--text-light);
}

.service-price {
  font-size: 22px;
  font-weight: 800;
  color: #E8789A;
}

.service-price small {
  font-size: 13px;
  font-weight: 600;
}

.service-btn {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--pink), var(--lavender));
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: inherit;
}

.service-btn:hover {
  transform: scale(1.03);
  box-shadow: var(--shadow-soft);
}

/* ===== 战绩相册 ===== */
.gallery-masonry {
  columns: 4 220px;
  column-gap: 16px;
}

.gallery-card {
  break-inside: avoid;
  margin-bottom: 16px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: var(--white);
  box-shadow: var(--shadow-soft);
  transition: all 0.35s ease;
}

.gallery-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.gallery-img {
  width: 100%;
  aspect-ratio: var(--aspect);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.gallery-label {
  padding: 10px 14px;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  font-weight: 500;
}

/* ===== 评价区 ===== */
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.review-card {
  background: var(--white);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-soft);
  transition: all 0.35s ease;
}

.review-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  flex-shrink: 0;
}

.review-user-info {
  flex: 1;
}

.review-name {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.review-stars {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 14px;
  color: var(--text-light);
  transition: color 0.3s;
}

.star.active {
  color: var(--pink-dark);
}

.review-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 8px;
}

.review-game {
  font-size: 12px;
  color: var(--text-light);
}

/* ===== 底部 ===== */
.footer {
  background: linear-gradient(135deg, rgba(255, 214, 224, 0.2), rgba(232, 223, 245, 0.2));
  padding: 48px 24px 0;
  margin-top: 40px;
}

.footer-inner {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  padding-bottom: 32px;
}

.footer-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px;
}

.footer-col p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 6px;
  line-height: 1.6;
}

.footer-copy {
  text-align: center;
  padding: 16px 0;
  border-top: 1px solid rgba(255, 182, 200, 0.2);
}

.footer-copy p {
  font-size: 12px;
  color: var(--text-light);
  margin: 0;
}

/* ===== 预约弹窗样式 ===== */
:deep(.booking-dialog .el-dialog) {
  border-radius: var(--radius-xl) !important;
  overflow: hidden;
}

:deep(.booking-dialog .el-dialog__header) {
  padding: 24px 28px 0 !important;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

:deep(.booking-dialog .el-dialog__body) {
  padding: 16px 28px 0 !important;
}

:deep(.booking-dialog .el-dialog__footer) {
  padding: 16px 28px 24px !important;
}

:deep(.booking-form .el-form-item__label) {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 13px;
  padding-bottom: 4px;
}

:deep(.booking-form .el-select .el-input__wrapper),
:deep(.booking-form .el-input__wrapper) {
  border-radius: var(--radius) !important;
  box-shadow: 0 0 0 1px rgba(255, 182, 200, 0.3) inset !important;
  transition: box-shadow 0.3s ease !important;
}

:deep(.booking-form .el-select .el-input__wrapper:hover),
:deep(.booking-form .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--pink-dark) inset !important;
}

:deep(.booking-form .el-select .el-input__wrapper.is-focus),
:deep(.booking-form .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--pink-dark) inset !important;
}

:deep(.booking-form .el-form-item__error) {
  color: #E8789A !important;
  font-size: 12px;
}

:deep(.booking-form textarea) {
  border-radius: var(--radius) !important;
  font-family: inherit;
}

/* 预约弹窗按钮 */
.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.dialog-btn {
  padding: 10px 24px;
  border: none;
  border-radius: var(--radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: inherit;
}

.dialog-btn-cancel {
  background: var(--pink-light);
  color: var(--text-secondary);
}

.dialog-btn-cancel:hover {
  background: var(--pink);
}

.dialog-btn-submit {
  background: linear-gradient(135deg, var(--pink), var(--lavender));
  color: var(--text-primary);
}

.dialog-btn-submit:hover {
  transform: scale(1.03);
  box-shadow: var(--shadow-soft);
}

.dialog-btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .navbar {
    padding: 0 16px;
  }

  .navbar-links {
    position: fixed;
    top: 64px;
    left: 0;
    right: 0;
    background: rgba(255, 249, 251, 0.98);
    backdrop-filter: blur(12px);
    flex-direction: column;
    padding: 12px 16px;
    gap: 4px;
    box-shadow: 0 4px 20px rgba(255, 182, 200, 0.15);
    transform: translateY(-110%);
    opacity: 0;
    transition: all 0.35s ease;
  }

  .navbar-links.nav-open {
    transform: translateY(0);
    opacity: 1;
  }

  .nav-link {
    width: 100%;
    text-align: center;
    padding: 10px;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .hero-section {
    padding: 100px 16px 40px;
  }

  .hero-avatar-frame {
    width: 100px;
    height: 100px;
  }

  .hero-avatar-text {
    font-size: 32px;
  }

  .hero-name {
    font-size: 22px;
  }

  .hero-price-card {
    padding: 12px 20px;
    gap: 16px;
  }

  .price-value {
    font-size: 18px;
  }

  .section {
    padding: 40px 16px;
  }

  .section-title {
    font-size: 20px;
  }

  .services-grid,
  .reviews-grid {
    grid-template-columns: 1fr;
  }

  .gallery-masonry {
    columns: 2 160px;
    column-gap: 12px;
  }

  .gallery-card {
    margin-bottom: 12px;
  }

  .footer-inner {
    grid-template-columns: 1fr;
    gap: 24px;
    text-align: center;
  }

  :deep(.booking-dialog .el-dialog) {
    width: 90vw !important;
    max-width: 400px;
  }
}

@media (max-width: 480px) {
  .gallery-masonry {
    columns: 2 130px;
    column-gap: 10px;
  }

  .gallery-card {
    margin-bottom: 10px;
  }

  .gallery-emoji {
    font-size: 28px;
  }

  .gallery-label {
    font-size: 11px;
    padding: 8px 10px;
  }

  .hero-tags {
    gap: 6px;
  }

  .tag {
    font-size: 11px;
    padding: 3px 10px;
  }
}
</style>
