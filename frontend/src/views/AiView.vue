<template>
  <section class="content-section narrow">
    <div class="section-heading">
      <div>
        <h1>AI 客服</h1>
        <p>课程咨询、报课流程和学习问题答疑入口。</p>
      </div>
    </div>
    <div class="chat-shell">
      <div ref="messageListRef" class="chat-list">
        <div
          v-for="message in messages"
          :key="message.id"
          class="chat-row"
          :class="message.role"
        >
          <div class="chat-message" :class="message.role">{{ message.content }}</div>
        </div>
        <div v-if="sending" class="chat-row assistant">
          <div class="chat-message assistant">正在思考...</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="input"
          placeholder="输入课程咨询、报名问题或学习疑问"
          :disabled="sending"
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" :loading="sending" :disabled="!canSend" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chatWithAgent } from '../api/agent'

const input = ref('')
const sending = ref(false)
const messageListRef = ref(null)
const sessionId = `web-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
const candidateCourseId = ref(null)
const pendingCourseId = ref(null)
const messages = ref([
  {
    id: 1,
    role: 'assistant',
    content: '你好，我可以帮你查询课程、了解课程详情，也可以协助你完成报课确认。'
  }
])

const canSend = computed(() => input.value.trim().length > 0 && !sending.value)

const sendMessage = async () => {
  const content = input.value.trim()
  if (!content || sending.value) return

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content
  })
  input.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const response = await chatWithAgent({
      message: content,
      sessionId,
      candidateCourseId: candidateCourseId.value,
      pendingCourseId: pendingCourseId.value,
      humanResponse: pendingCourseId.value ? content : null
    })
    candidateCourseId.value = response.candidateCourseId ?? response.candidate_course_id ?? null
    pendingCourseId.value = response.pendingCourseId ?? response.pending_course_id ?? null
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: response.reply || '我暂时没有生成回复，请稍后再试。'
    })
  } catch (error) {
    ElMessage.error(error.message || 'AI 客服请求失败')
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}
</script>
