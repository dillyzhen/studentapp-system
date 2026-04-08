import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './stores/auth'
import LoginPage from './pages/Login'
import AdminLayout from './layouts/AdminLayout'
import TeacherLayout from './layouts/TeacherLayout'
import ParentLayout from './layouts/ParentLayout'

function App() {
  const { user, isAuthenticated } = useAuthStore()

  if (!isAuthenticated) {
    return <LoginPage />
  }

  // Redirect based on role
  if (window.location.pathname === '/') {
    switch (user?.role) {
      case 'ADMIN':
        return <Navigate to="/admin" replace />
      case 'TEACHER':
        return <Navigate to="/teacher" replace />
      case 'PARENT':
        return <Navigate to="/parent" replace />
      default:
        return <Navigate to="/login" replace />
    }
  }

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin/*" element={<AdminLayout />} />
      <Route path="/teacher/*" element={<TeacherLayout />} />
      <Route path="/parent/*" element={<ParentLayout />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
