import apiClient from './client';
import type { ApiResponse, DashboardStats, User } from '../types';

export const adminApi = {
  getDashboardStats: async (): Promise<DashboardStats> => {
    const response = await apiClient.get<ApiResponse<DashboardStats>>('/admin/dashboard/stats');
    return response.data.data;
  },

  getUsers: async (): Promise<User[]> => {
    const response = await apiClient.get<ApiResponse<User[]>>('/admin/users');
    return response.data.data;
  },

  createUser: async (data: Partial<User>): Promise<User> => {
    const response = await apiClient.post<ApiResponse<User>>('/admin/users', data);
    return response.data.data;
  },

  updateUser: async (id: string, data: Partial<User>): Promise<User> => {
    const response = await apiClient.put<ApiResponse<User>>(`/admin/users/${id}`, data);
    return response.data.data;
  },

  deleteUser: async (id: string): Promise<void> => {
    await apiClient.delete(`/admin/users/${id}`);
  },
};
