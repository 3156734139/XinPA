/**
 * 格式化日期时间：YYYY-MM-DD HH:mm
 */
export function formatDateTime(dt: string | null | undefined): string {
  if (!dt) return '-';
  const d = new Date(dt);
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * 格式化时长：将 actualMinutes 转为 XhYmin 格式
 */
export function formatDuration(row: { actualMinutes?: number | null }): string {
  const minutes = row.actualMinutes || 0;
  if (minutes === 0) return '-';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  if (h === 0) return `${m}min`;
  return `${h}h${m}min`;
}
