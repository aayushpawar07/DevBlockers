import { Loader2 } from 'lucide-react';

export const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  className = '',
  ...props
}) => {
  const baseClasses = 'font-semibold rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 focus:outline-none focus:ring-2 focus:ring-offset-2';
  
  const variants = {
    primary: 'bg-primary-600 hover:bg-primary-700 active:bg-primary-800 text-white shadow-sm hover:shadow-md focus:ring-primary-500 transform hover:-translate-y-0.5 active:translate-y-0',
    secondary: 'bg-white hover:bg-gray-50 active:bg-gray-100 text-gray-700 border border-gray-300 hover:border-gray-400 shadow-sm hover:shadow-md focus:ring-gray-500',
    danger: 'bg-danger-600 hover:bg-danger-700 active:bg-danger-800 text-white shadow-sm hover:shadow-md focus:ring-danger-500 transform hover:-translate-y-0.5 active:translate-y-0',
    success: 'bg-success-600 hover:bg-success-700 active:bg-success-800 text-white shadow-sm hover:shadow-md focus:ring-success-500 transform hover:-translate-y-0.5 active:translate-y-0',
    outline: 'border-2 border-primary-600 text-primary-600 hover:bg-primary-50 active:bg-primary-100 focus:ring-primary-500',
    warning: 'bg-yellow-500 hover:bg-yellow-600 active:bg-yellow-700 text-white shadow-sm hover:shadow-md focus:ring-yellow-500',
  };

  const sizes = {
    sm: 'py-2 px-4 text-sm',
    md: 'py-2.5 px-6 text-base',
    lg: 'py-3 px-8 text-lg',
  };

  return (
    <button
      className={`${baseClasses} ${variants[variant]} ${sizes[size]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading && <Loader2 className="w-4 h-4 animate-spin" />}
      {children}
    </button>
  );
};

