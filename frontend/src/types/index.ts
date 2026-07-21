/** 统一 API 响应体 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

/** 订单状态 */
export const ORDER_STATUS_MAP: Record<number, { label: string; type: string }> = {
  1: { label: '待接单', type: 'info' },
  2: { label: '进行中', type: 'primary' },
  3: { label: '待结算', type: 'warning' },
  4: { label: '已完结', type: 'success' },
  5: { label: '售后退款', type: 'danger' },
};

export function getStatusLabel(status: number): string {
  return ORDER_STATUS_MAP[status]?.label || '-';
}

export function getStatusType(status: number): string {
  return ORDER_STATUS_MAP[status]?.type || 'info';
}

/** 订单 */
export interface Order {
  id: number;
  userId: number;
  customerId: number;
  orderNo: string;
  orderSource: number;
  packageId: number;
  packageName: string;
  title: string;
  status: number;
  unitPrice: number;
  billedMinutes: number;
  actualMinutes: number;
  extraMinutes: number;
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  paymentMethod: string;
  paymentMethodId: number;
  settleRatio: number;
  isOvernight: number;
  isOffline: number;
  couponId: number;
  startTime: string;
  endTime: string;
  settleTime: string;
  appointmentTime: string;
  remark: string;
  createdAt: string;
  updatedAt: string;
}

/** 客户 */
export interface Customer {
  id: number;
  userId: number;
  nickname: string;
  contact: string;
  source: string;
  sourceId: number;
  spendLevel: number;
  gamePreference: string;
  personality: string;
  birthday: string;
  tags: string;
  isBlacklist: number;
  blacklistReason: string;
  lastOrderTime: string;
  totalSpend: number;
  orderCount: number;
  remark: string;
  createdAt: string;
  updatedAt: string;
}

/** 财务流水 */
export interface FinanceRecord {
  id: number;
  userId: number;
  orderId: number;
  recordType: number;
  category: string;
  amount: number;
  paymentMethod: string;
  platformFee: number;
  recordDate: string;
  remark: string;
  createdAt: string;
}

/** 价目套餐 */
export interface PricePackage {
  id: number;
  userId: number;
  name: string;
  packageType: number;
  price: number;
  unit: string;
  description: string;
  discountRules: string;
  sortOrder: number;
  status: number;
  createdAt: string;
  orderCount?: number;
  totalRevenue?: number;
}

/** 待办事项 */
export interface TodoItem {
  id: number;
  userId: number;
  title: string;
  status: number;
  todoType: number;
  sortOrder: number;
  createdAt: string;
}

/** 素材 */
export interface Material {
  id: number;
  userId: number;
  name: string;
  materialType: string;
  fileUrl: string;
  fileSize: number;
  watermark: number;
  createdAt: string;
}

/** 订单来源字典 */
export interface OrderSource {
  id: number;
  name: string;
  enabled: number;
  sortOrder: number;
}

/** 支付方式字典 */
export interface PaymentMethod {
  id: number;
  name: string;
  enabled: number;
  sortOrder: number;
}

/** 财务设置 */
export interface UserFinanceSetting {
  id: number;
  userId: number;
  monthlyTarget: number;
  withdrawFeeRate: number;
}

/** 系统公告 */
export interface SysAnnouncement {
  id: number;
  title: string;
  content: string;
  status: number;
  createdAt: string;
}

/** 用户接单状态映射 */
export const ORDER_STATUS_OPTIONS: Record<number, string> = {
  1: '在线接单',
  2: '休息中',
  3: '通宵接单',
  4: '仅熟客',
};
