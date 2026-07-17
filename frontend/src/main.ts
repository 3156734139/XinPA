import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import './style.css';

const app = createApp(App);

// 按需注册全局常用的 Element Plus 图标
import {
  ArrowDown, Bell, Camera, Coin, DataAnalysis, DataLine, Edit, Expand, Fold,
  Folder, List, Lock, Money, Monitor, PriceTag, Setting, SwitchButton,
  TrendCharts, User, UserFilled, Plus, Calendar, MagicStick, Delete,
  Star, InfoFilled,
} from '@element-plus/icons-vue';
const icons = { ArrowDown, Bell, Camera, Coin, DataAnalysis, DataLine, Edit, Expand, Fold, Folder, List, Lock, Money, Monitor, PriceTag, Setting, SwitchButton, TrendCharts, User, UserFilled, Plus, Calendar, MagicStick, Delete, Star, InfoFilled };
for (const [key, component] of Object.entries(icons)) {
  app.component(key, component);
}

app.use(createPinia());
app.use(router);
app.use(ElementPlus);
app.mount('#app');
