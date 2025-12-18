import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Layout } from './components/layout/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Landing } from './pages/Landing';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { RegisterOrganization } from './pages/RegisterOrganization';
import { Dashboard } from './pages/Dashboard';
import { OrganizationDashboard } from './pages/organization/OrganizationDashboard';
import { EmployeeDashboard } from './pages/organization/EmployeeDashboard';
import { BlockerList } from './pages/blockers/BlockerList';
import { BlockerDetail } from './pages/blockers/BlockerDetail';
import { CreateBlocker } from './pages/blockers/CreateBlocker';
import { Notifications } from './pages/Notifications';
import { Profile } from './pages/Profile';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/register-organization" element={<RegisterOrganization />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Layout>
                  <Dashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/blockers"
            element={
              <ProtectedRoute>
                <Layout>
                  <BlockerList />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/blockers/create"
            element={
              <ProtectedRoute>
                <Layout>
                  <CreateBlocker />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/blockers/:id"
            element={
              <ProtectedRoute>
                <Layout>
                  <BlockerDetail />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <Layout>
                  <Notifications />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Layout>
                  <Profile />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/organization/dashboard"
            element={
              <ProtectedRoute>
                <Layout>
                  <OrganizationDashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/employee/dashboard"
            element={
              <ProtectedRoute>
                <Layout>
                  <EmployeeDashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

