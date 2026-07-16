import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref<any>(null);
  const userType = ref(localStorage.getItem('userType') || 'USER');

  function setToken(val: string) {
    token.value = val;
    localStorage.setItem('token', val);
  }

  function setUserInfo(info: any) {
    userInfo.value = info;
    localStorage.setItem('userInfo', JSON.stringify(info));
  }

  function setUserType(type: string) {
    userType.value = type;
    localStorage.setItem('userType', type);
  }

  function logout() {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('userType');
  }

  return { token, userInfo, userType, setToken, setUserInfo, setUserType, logout };
});
