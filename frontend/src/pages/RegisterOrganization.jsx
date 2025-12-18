import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { organizationService } from '../services/organizationService';
import { authService } from '../services/authService';
import { Button } from '../components/ui/Button';
import { Building2, Mail, Lock, User, Globe, ArrowRight, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';

export const RegisterOrganization = () => {
  const [organizationName, setOrganizationName] = useState('');
  const [domain, setDomain] = useState('');
  const [adminName, setAdminName] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    
    if (adminPassword !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    if (adminPassword.length < 8) {
      toast.error('Password must be at least 8 characters');
      return;
    }

    setLoading(true);
    try {
      const response = await organizationService.register({
        organizationName,
        domain: domain || null,
        adminName,
        adminEmail,
        adminPassword
      });
      
      toast.success('Organization registered successfully!');
      
      // Auto-login the admin
      try {
        const loginResponse = await authService.login(adminEmail, adminPassword);
        if (loginResponse.accessToken) {
          navigate('/organization/dashboard');
        }
      } catch (loginError) {
        navigate('/login');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to register organization';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Decorative background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-purple-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-pink-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
      </div>

      <div className="w-full max-w-2xl relative z-10">
        {/* Logo/Brand */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-600 to-primary-700 rounded-2xl shadow-lg mb-4 transform hover:scale-105 transition-transform">
            <Building2 className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-4xl sm:text-5xl font-bold text-gray-900 mb-3">
            Create organization
          </h1>
          <p className="text-lg text-gray-600">
            Set up your organization and start managing blockers with your team
          </p>
        </div>

        {/* Registration Card */}
        <div className="bg-white/90 backdrop-blur-lg rounded-2xl shadow-2xl border border-white/20 p-8 sm:p-10">
          <form onSubmit={handleRegister} className="space-y-6">
            {/* Organization Details */}
            <div className="border-b border-gray-200 pb-6 mb-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Building2 className="w-5 h-5 text-primary-600" />
                Organization Details
              </h2>
              
              <div className="space-y-4">
                <div>
                  <label htmlFor="organizationName" className="block text-sm font-semibold text-gray-700 mb-2">
                    Organization Name *
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Building2 className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      id="organizationName"
                      type="text"
                      value={organizationName}
                      onChange={(e) => setOrganizationName(e.target.value)}
                      required
                      placeholder="Acme Corporation"
                      className="w-full pl-12 pr-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200 text-gray-900 placeholder:text-gray-400 bg-white hover:border-gray-400"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="domain" className="block text-sm font-semibold text-gray-700 mb-2">
                    Domain (Optional)
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Globe className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      id="domain"
                      type="text"
                      value={domain}
                      onChange={(e) => setDomain(e.target.value)}
                      placeholder="acme.com"
                      className="w-full pl-12 pr-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200 text-gray-900 placeholder:text-gray-400 bg-white hover:border-gray-400"
                    />
                  </div>
                  <p className="mt-1 text-xs text-gray-500">Your organization's domain (optional)</p>
                </div>
              </div>
            </div>

            {/* Admin Account */}
            <div>
              <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                <User className="w-5 h-5 text-primary-600" />
                Administrator Account
              </h2>
              
              <div className="space-y-4">
                <div>
                  <label htmlFor="adminName" className="block text-sm font-semibold text-gray-700 mb-2">
                    Admin Name *
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <User className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      id="adminName"
                      type="text"
                      value={adminName}
                      onChange={(e) => setAdminName(e.target.value)}
                      required
                      placeholder="John Doe"
                      className="w-full pl-12 pr-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200 text-gray-900 placeholder:text-gray-400 bg-white hover:border-gray-400"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="adminEmail" className="block text-sm font-semibold text-gray-700 mb-2">
                    Admin Email *
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Mail className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      id="adminEmail"
                      type="email"
                      value={adminEmail}
                      onChange={(e) => setAdminEmail(e.target.value)}
                      required
                      placeholder="admin@example.com"
                      className="w-full pl-12 pr-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200 text-gray-900 placeholder:text-gray-400 bg-white hover:border-gray-400"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="adminPassword" className="block text-sm font-semibold text-gray-700 mb-2">
                    Admin Password *
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Lock className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      id="adminPassword"
                      type="password"
                      value={adminPassword}
                      onChange={(e) => setAdminPassword(e.target.value)}
                      required
                      placeholder="••••••••"
                      minLength={8}
                      className="w-full pl-12 pr-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200 text-gray-900 placeholder:text-gray-400 bg-white hover:border-gray-400"
                    />
                  </div>
                  <p className="mt-1 text-xs text-gray-500">Must be at least 8 characters</p>
                </div>

                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-semibold text-gray-700 mb-2">
                    Confirm Password *
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Lock className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      id="confirmPassword"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                      placeholder="••••••••"
                      className="w-full pl-12 pr-4 py-3.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-all duration-200 text-gray-900 placeholder:text-gray-400 bg-white hover:border-gray-400"
                    />
                  </div>
                </div>
              </div>
            </div>

            <Button 
              type="submit" 
              className="w-full py-3.5 text-base font-semibold flex items-center justify-center gap-2" 
              loading={loading}
            >
              {!loading && <Building2 className="w-5 h-5" />}
              Create Organization
              {!loading && <ArrowRight className="w-5 h-5" />}
            </Button>
          </form>

          <div className="mt-8 pt-6 border-t border-gray-200">
            <p className="text-center text-sm text-gray-600">
              Already have an organization?{' '}
              <Link 
                to="/login" 
                className="text-primary-600 hover:text-primary-700 font-semibold transition-colors duration-200 inline-flex items-center gap-1"
              >
                Sign in
                <ArrowRight className="w-4 h-4" />
              </Link>
            </p>
            <p className="text-center text-sm text-gray-600 mt-2">
              Or{' '}
              <Link 
                to="/register" 
                className="text-primary-600 hover:text-primary-700 font-semibold transition-colors duration-200"
              >
                create a personal account
              </Link>
            </p>
          </div>
        </div>
      </div>

      <style>{`
        @keyframes blob {
          0% {
            transform: translate(0px, 0px) scale(1);
          }
          33% {
            transform: translate(30px, -50px) scale(1.1);
          }
          66% {
            transform: translate(-20px, 20px) scale(0.9);
          }
          100% {
            transform: translate(0px, 0px) scale(1);
          }
        }
        .animate-blob {
          animation: blob 7s infinite;
        }
        .animation-delay-2000 {
          animation-delay: 2s;
        }
        .animation-delay-4000 {
          animation-delay: 4s;
        }
      `}</style>
    </div>
  );
};

