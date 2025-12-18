export const Card = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`bg-white rounded-xl shadow-sm border border-gray-200/80 transition-all duration-200 hover:shadow-md ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export const CardHeader = ({ children, className = '' }) => {
  return (
    <div className={`p-6 lg:p-8 border-b border-gray-200/80 ${className}`}>
      {children}
    </div>
  );
};

export const CardBody = ({ children, className = '' }) => {
  return (
    <div className={`p-6 lg:p-8 ${className}`}>
      {children}
    </div>
  );
};

export const CardFooter = ({ children, className = '' }) => {
  return (
    <div className={`p-6 lg:p-8 border-t border-gray-200/80 bg-gray-50/50 rounded-b-xl ${className}`}>
      {children}
    </div>
  );
};

