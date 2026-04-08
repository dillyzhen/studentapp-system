export interface User {
  id: string;
  username: string;
  name: string;
  role: 'ADMIN' | 'TEACHER' | 'PARENT';
  roleDisplayName: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string | null;
  data: T;
  timestamp: string;
}

export interface Student {
  id: string;
  name: string;
  age?: number;
  gender?: string;
  genderDisplayName?: string;
  studentNo?: string;
  className?: string;
  avatarUrl?: string;
}

export interface RawRecord {
  id: string;
  studentId: string;
  studentName: string;
  type: 'HEALTH' | 'LEARNING' | 'BEHAVIOR' | 'OTHER';
  typeDisplayName: string;
  content: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'REJECTED';
  statusDisplayName: string;
  submittedAt: string;
}

export interface Report {
  id: string;
  title: string;
  content: string;
  studentId: string;
  studentName: string;
  studentClass?: string;
  status: 'DRAFT' | 'PENDING_AUDIT' | 'APPROVED' | 'REJECTED' | 'DISTRIBUTED';
  statusDisplayName: string;
  auditorName?: string;
  auditComment?: string;
  auditedAt?: string;
  distributedAt?: string;
  viewCount: number;
  downloadCount: number;
  createdAt: string;
}

export interface DashboardStats {
  totalUsers: number;
  totalTeachers: number;
  totalParents: number;
  totalStudents: number;
  totalSubmissions: number;
  pendingSubmissions: number;
  completedSubmissions: number;
  totalReports: number;
  draftReports: number;
  approvedReports: number;
  distributedReports: number;
  totalInterpretations: number;
  totalAiCostUsd: number;
  totalAiTokens: number;
}

export interface TimelineEvent {
  id: string;
  eventType: string;
  eventTitle: string;
  eventData?: string;
  sourceType?: string;
  sourceId?: string;
  createdAt: string;
}
