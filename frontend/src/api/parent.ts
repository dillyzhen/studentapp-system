import apiClient from './client';
import type { ApiResponse, Student, RawRecord, Report } from '../types';

export const parentApi = {
  getMyStudents: async (): Promise<Student[]> => {
    const response = await apiClient.get<ApiResponse<Student[]>>('/parent/students');
    return response.data.data;
  },

  getMyReports: async (): Promise<Report[]> => {
    const response = await apiClient.get<ApiResponse<Report[]>>('/parent/reports');
    return response.data.data;
  },

  getReportDetail: async (reportId: string): Promise<Report> => {
    const response = await apiClient.get<ApiResponse<Report>>(`/parent/reports/${reportId}`);
    return response.data.data;
  },

  downloadReportPdf: async (reportId: string): Promise<Blob> => {
    const response = await apiClient.get(`/parent/reports/${reportId}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },

  submitRecord: async (data: { studentId: string; type: string; content: string }): Promise<RawRecord> => {
    const response = await apiClient.post<ApiResponse<RawRecord>>('/parent/submissions', data);
    return response.data.data;
  },
};
