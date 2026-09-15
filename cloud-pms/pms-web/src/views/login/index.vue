<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="34" color="#409eff"><Grid /></el-icon>
        <h2>云协同 PMS</h2>
        <p>中小企业项目全生命周期管理系统</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="code" v-if="captchaEnabled">
          <div class="captcha-row">
            <el-input
              v-model="form.code"
              placeholder="请输入验证码"
              @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><Key /></el-icon></template>
            </el-input>
            <img
              v-if="captchaImg"
              :src="captchaImg"
              class="captcha-img"
              alt="验证码"
              title="点击刷新"
              @click="loadCaptcha"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        <span>管理员：admin / admin123</span>
        <span>普通成员：lilei / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/login'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')
const captchaEnabled = ref(true)

const form = reactive({
  username: 'admin',
  password: 'admin123',
  code: '',
  uuid: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function loadCaptcha() {
  try {
    const res = await getCaptcha()
    captchaEnabled.value = res.data.captchaEnabled !== false
    if (captchaEnabled.value) {
      captchaImg.value = res.data.img
      form.uuid = res.data.uuid
    }
  } catch (e) {
    captchaEnabled.value = false
  }
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password,
      code: form.code,
      uuid: form.uuid
    })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect ? decodeURIComponent(route.query.redirect) : '/'
    router.push(redirect)
  } catch (e) {
    // 登录失败后刷新验证码
    form.code = ''
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped lang="scss">
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3a5f 0%, #2b5876 50%, #4e4376 100%);
}

.login-box {
  width: 420px;
  padding: 36px 36px 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.22);
}

.login-header {
  text-align: center;
  margin-bottom: 26px;

  h2 {
    margin: 10px 0 6px;
    font-size: 22px;
    color: #303133;
    letter-spacing: 1px;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: #909399;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;

  .el-input {
    flex: 1;
  }

  .captcha-img {
    width: 118px;
    height: 40px;
    border-radius: 4px;
    cursor: pointer;
    border: 1px solid #dcdfe6;
    flex-shrink: 0;
  }
}

.login-btn {
  width: 100%;
  letter-spacing: 4px;
}

.login-tip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: center;
  font-size: 12px;
  color: #a8abb2;
  padding-top: 6px;
  border-top: 1px dashed #ebeef5;
  margin-top: 6px;
}
</style>
