import apiClient from './client';
import type { ApiResponse, RawRecord, Report } from '../types';

export const teacherApi = {
  getPendingSubmissions: async (): Promise<RawRecord[]> => {
    const response = await apiClient.get<ApiResponse<RawRecord[]>>('/teacher/dashboard/pending-submissions');
    return response.data.data;
  },

  startInterpretation: async (rawRecordId: string): Promise<any> => {
    const response = await apiClient.post<ApiResponse<any>>(`/teacher/interpretations/start/${rawRecordId}`);
    return response.data.data;
  },

  generateInterpretation: async (rawRecordId: string): Promise<any> => {
    const response = await apiClient.post<ApiResponse<any>>(`/teacher/interpretations/generate/${rawRecordId}`);
    return response.data.data;
  },

  approveReport: async (reportId: string, comment: string): Promise<void> => {
    await apiClient.post(`/teacher/reports/${reportId}/approve`, { comment });
  },

  distributeReport: async (reportId: string): Promise<void> => {
    await apiClient.post(`/teacher/reports/${reportId}/distribute`);
  },

  getMyReports: async (status?: string): Promise<Report[]> => {
    const params = status ? { status } : {};
    const response = await apiClient.get<ApiResponse<Report[]>>('/teacher/dashboard/reports', { params });
    return response.data.data;
  },
};
