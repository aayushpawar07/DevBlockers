import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card, CardBody, CardHeader } from '../components/ui/Card';

export const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState('register'); // 'register' or 'otp'
  const [otp, setOtp] = useState('');
  const { register, sendOtp, verifyOtp } = useAuth();
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      alert('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      await register(email, password);
      await sendOtp(email, 'REGISTRATION');
      setStep('otp');
    } catch (error) {
      // Error is handled by AuthContext
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await verifyOtp(email, otp, 'REGISTRATION');
      navigate('/login');
    } catch (error) {
      // Error is handled by AuthContext
    } finally {
      setLoading(false);
    }
  };

  if (step === 'otp') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
        <Card className="w-full max-w-md">
          <CardHeader>
            <h1 className="text-3xl font-bold text-center text-gray-900">Verify Email</h1>
            <p className="mt-2 text-center text-sm text-gray-600">
              We've sent a verification code to {email}
            </p>
          </CardHeader>
          <CardBody>
            <form onSubmit={handleVerifyOtp} className="space-y-6">
              <Input
                label="OTP Code"
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                required
                placeholder="Enter 6-digit code"
                maxLength={6}
              />
              <Button type="submit" className="w-full" loading={loading}>
                Verify
              </Button>
              <Button
                type="button"
                variant="secondary"
                className="w-full"
                onClick={async () => {
                  try {
                    await sendOtp(email, 'REGISTRATION');
                  } catch (error) {
                    // Error handled by context
                  }
                }}
              >
                Resend OTP
              </Button>
            </form>
          </CardBody>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
      <Card className="w-full max-w-md">
        <CardHeader>
          <h1 className="text-3xl font-bold text-center text-gray-900">Create Account</h1>
          <p className="mt-2 text-center text-sm text-gray-600">
            Join DevBlocker to track and resolve blockers
          </p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleRegister} className="space-y-6">
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@example.com"
            />
            <Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              minLength={8}
            />
            <Input
              label="Confirm Password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
            <Button type="submit" className="w-full" loading={loading}>
              Sign Up
            </Button>
          </form>
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                Sign in
              </Link>
            </p>
          </div>
        </CardBody>
      </Card>
    </div>
  );
};

