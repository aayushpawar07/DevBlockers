import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button } from '../components/ui/Button';
import { Navbar } from '../components/layout/Navbar';
import { 
  Bug, 
  Users, 
  Zap, 
  Shield, 
  CheckCircle, 
  ArrowRight, 
  TrendingUp,
  MessageSquare,
  Bell,
  BarChart3,
  Rocket
} from 'lucide-react';

export const Landing = () => {
  const { isAuthenticated } = useAuth();

  // Redirect authenticated users to dashboard
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 relative overflow-hidden">
      {/* Decorative background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-purple-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-blue-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-pink-300 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
      </div>

      <Navbar />

      {/* Hero Section */}
      <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-32">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left Side - Text Content */}
          <div className="text-center lg:text-left">
            <h1 className="text-5xl sm:text-6xl lg:text-7xl font-bold text-gray-900 mb-6 leading-tight">
              Track and resolve blockers
              <span className="block bg-gradient-to-r from-primary-600 to-purple-600 bg-clip-text text-transparent">
                with your team
              </span>
            </h1>
            <p className="text-xl sm:text-2xl text-gray-600 mb-8 leading-relaxed">
              Connect and collaborate for free. Manage blockers, find solutions, and keep your team productive.
            </p>
            
            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start mb-12">
              <Link to="/register">
                <Button size="lg" className="w-full sm:w-auto text-lg px-8 py-4 flex items-center justify-center gap-2">
                  <Rocket className="w-5 h-5" />
                  Get started for free
                  <ArrowRight className="w-5 h-5" />
                </Button>
              </Link>
              <Link to="/register-organization">
                <Button variant="secondary" size="lg" className="w-full sm:w-auto text-lg px-8 py-4 flex items-center justify-center gap-2">
                  <Users className="w-5 h-5" />
                  Create organization
                  <ArrowRight className="w-5 h-5" />
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="outline" size="lg" className="w-full sm:w-auto text-lg px-8 py-4 flex items-center justify-center gap-2">
                  Sign in
                  <ArrowRight className="w-5 h-5" />
                </Button>
              </Link>
            </div>

            {/* Trust Indicators */}
            <div className="flex flex-wrap items-center justify-center lg:justify-start gap-6 text-sm text-gray-600">
              <div className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-success-600" />
                <span>Free forever</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-success-600" />
                <span>No credit card required</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-success-600" />
                <span>Instant setup</span>
              </div>
            </div>
          </div>

          {/* Right Side - Visual Mockup */}
          <div className="relative hidden lg:block">
            <div className="relative transform rotate-3 hover:rotate-0 transition-transform duration-500">
              {/* Mock Dashboard Card */}
              <div className="bg-white rounded-2xl shadow-2xl p-6 border border-gray-200/80 backdrop-blur-lg">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-red-500"></div>
                    <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                    <div className="w-3 h-3 rounded-full bg-green-500"></div>
                  </div>
                  <span className="text-xs text-gray-500">DevBlocker Dashboard</span>
                </div>
                
                {/* Mock Blocker Cards */}
                <div className="space-y-3">
                  <div className="bg-gradient-to-r from-red-50 to-orange-50 rounded-lg p-4 border-l-4 border-red-500">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900 text-sm mb-1">Database Connection Timeout</h3>
                        <p className="text-xs text-gray-600">High priority blocker affecting production</p>
                      </div>
                      <span className="px-2 py-1 bg-red-100 text-red-700 text-xs font-semibold rounded">OPEN</span>
                    </div>
                  </div>
                  
                  <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-4 border-l-4 border-blue-500">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900 text-sm mb-1">API Rate Limiting Issue</h3>
                        <p className="text-xs text-gray-600">3 solutions proposed</p>
                      </div>
                      <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs font-semibold rounded">IN PROGRESS</span>
                    </div>
                  </div>
                  
                  <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg p-4 border-l-4 border-green-500">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900 text-sm mb-1">Authentication Bug Fixed</h3>
                        <p className="text-xs text-gray-600">Resolved by team</p>
                      </div>
                      <span className="px-2 py-1 bg-green-100 text-green-700 text-xs font-semibold rounded">RESOLVED</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Floating decorative elements */}
            <div className="absolute -top-10 -right-10 w-20 h-20 bg-purple-200 rounded-full opacity-50 blur-xl animate-pulse"></div>
            <div className="absolute -bottom-10 -left-10 w-24 h-24 bg-blue-200 rounded-full opacity-50 blur-xl animate-pulse animation-delay-2000"></div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="relative z-10 bg-white/50 backdrop-blur-sm py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl sm:text-5xl font-bold text-gray-900 mb-4">
              Everything you need to manage blockers
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Powerful features to help your team track, discuss, and resolve blockers faster
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="bg-white/80 backdrop-blur-lg rounded-xl p-8 border border-gray-200/80 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2">
              <div className="w-12 h-12 bg-gradient-to-br from-primary-500 to-primary-600 rounded-lg flex items-center justify-center mb-4">
                <Bug className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Track Blockers</h3>
              <p className="text-gray-600">
                Create and organize blockers with tags, severity levels, and detailed descriptions
              </p>
            </div>

            {/* Feature 2 */}
            <div className="bg-white/80 backdrop-blur-lg rounded-xl p-8 border border-gray-200/80 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2">
              <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg flex items-center justify-center mb-4">
                <MessageSquare className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Collaborate</h3>
              <p className="text-gray-600">
                Discuss solutions, share ideas, and work together to resolve blockers quickly
              </p>
            </div>

            {/* Feature 3 */}
            <div className="bg-white/80 backdrop-blur-lg rounded-xl p-8 border border-gray-200/80 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2">
              <div className="w-12 h-12 bg-gradient-to-br from-green-500 to-green-600 rounded-lg flex items-center justify-center mb-4">
                <Zap className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Fast Resolution</h3>
              <p className="text-gray-600">
                Upvote best solutions and mark blockers as resolved when issues are fixed
              </p>
            </div>

            {/* Feature 4 */}
            <div className="bg-white/80 backdrop-blur-lg rounded-xl p-8 border border-gray-200/80 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2">
              <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center mb-4">
                <Bell className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Real-time Notifications</h3>
              <p className="text-gray-600">
                Get notified when blockers are updated, solutions are added, or comments are posted
              </p>
            </div>

            {/* Feature 5 */}
            <div className="bg-white/80 backdrop-blur-lg rounded-xl p-8 border border-gray-200/80 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2">
              <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg flex items-center justify-center mb-4">
                <BarChart3 className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Track Progress</h3>
              <p className="text-gray-600">
                Monitor blocker statistics and track your team's progress with detailed analytics
              </p>
            </div>

            {/* Feature 6 */}
            <div className="bg-white/80 backdrop-blur-lg rounded-xl p-8 border border-gray-200/80 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2">
              <div className="w-12 h-12 bg-gradient-to-br from-indigo-500 to-indigo-600 rounded-lg flex items-center justify-center mb-4">
                <Shield className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Secure & Private</h3>
              <p className="text-gray-600">
                Your data is secure with enterprise-grade security and privacy controls
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="relative z-10 bg-gradient-to-r from-primary-600 to-purple-600 py-20">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-4xl sm:text-5xl font-bold text-white mb-6">
            Ready to get started?
          </h2>
          <p className="text-xl text-white/90 mb-8">
            Join thousands of teams already using DevBlocker to manage their blockers
          </p>
          <Link to="/register">
            <Button size="lg" variant="secondary" className="text-lg px-8 py-4">
              Get started for free
              <ArrowRight className="w-5 h-5 ml-2" />
            </Button>
          </Link>
        </div>
      </div>

      {/* Footer */}
      <footer className="relative z-10 bg-gray-900 text-gray-400 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="flex items-center justify-center gap-2 mb-4">
              <div className="w-8 h-8 bg-gradient-to-br from-primary-600 to-primary-700 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold">DB</span>
              </div>
              <span className="text-xl font-bold text-white">DevBlocker</span>
            </div>
            <p className="text-sm">© 2025 DevBlocker. All rights reserved.</p>
          </div>
        </div>
      </footer>

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

