<template>
  <div class="login-container">
    <ParticleBackground />
    <div class="login-card glass-card">
      <h2>体育馆管理系统 - 用户登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" style="margin-top: 20px;">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%;" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button text @click="$router.push('/register')">没有账号？去注册</el-button>
          <el-button text @click="$router.push('/admin/login')">管理员登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import ParticleBackground from '@/components/common/ParticleBackground.vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('role', res.data.role)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('username', res.data.username)
    ElMessage.success('登录成功')
    router.push('/venues')
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);
  position: relative;
  overflow: hidden;
}

.login-card {
  width: 420px;
  position: relative;
  z-index: 1;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #15213a;
  font-size: 22px;
  font-weight: 700;
}

.login-container :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(26, 115, 232, 0.08);
  box-shadow: 0 8px 22px rgba(31, 57, 95, 0.06);
}

.login-container :deep(.el-input__inner) {
  color: #253247;
}

.login-container :deep(.el-input__inner::placeholder) {
  color: #9aa7b8;
}

.login-container :deep(.el-button--primary) {
  background: var(--gradient-blue);
  border: none;
  height: 44px;
  font-size: 16px;
}

.login-container :deep(.el-button.el-button--text) {
  color: #52657d;
}

.login-container :deep(.el-form-item__label) {
  color: #52657d;
}

.login-container :deep(.el-input__suffix .el-icon) {
  color: #8b9bb0;
}
</style>
