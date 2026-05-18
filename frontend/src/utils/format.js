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
