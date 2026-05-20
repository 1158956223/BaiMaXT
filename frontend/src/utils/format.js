export const formatMoney = (value) => {
  if (value === null || value === undefined || value === '') return '面议'
  return `¥${Number(value).toFixed(2)}`
}

export const courseTypeText = {
  ONLINE: '线上课',
  OFFLINE: '线下课',
  RECORDED: '录播课'
}

export const courseStatusText = {
  DRAFT: '草稿',
  ON_SALE: '上架',
  OFF_SALE: '下架'
}

export const enabledText = (status) => {
  if (status === 1 || status === 'ENABLED') return '启用'
  return '禁用'
}

export const enabledValue = (status) => status === 1 || status === 'ENABLED'

export const userStatusText = {
  ENABLED: '启用',
  DISABLED: '禁用'
}

export const roleText = {
  STUDENT: '学员',
  TEACHER: '教师',
  ADMIN: '管理员'
}

export const orderStatusText = {
  CREATED: '待支付',
  PAID: '已支付',
  CANCELLED: '已取消',
  EXPIRED: '已过期'
}

export const payStatusText = {
  UNPAID: '未支付',
  PAID: '已支付',
  WAITING: '待支付',
  SUCCESS: '支付成功',
  FAILED: '支付失败',
  CLOSED: '已关闭'
}

export const payTypeText = {
  MOCK: '模拟支付',
  WECHAT: '微信支付',
  ALIPAY: '支付宝'
}

export const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}
