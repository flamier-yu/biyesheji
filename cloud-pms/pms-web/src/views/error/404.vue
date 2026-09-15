<template>
  <div class="error-page">
    <div class="code">404</div>
    <p class="tip">抱歉，你访问的页面不存在或已被移除</p>
    <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.error-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 60px 0;
}

.code {
  font-size: 88px;
  font-weight: 700;
  color: #dcdfe6;
  line-height: 1;
  letter-spacing: 4px;
}

.tip {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
</style>
