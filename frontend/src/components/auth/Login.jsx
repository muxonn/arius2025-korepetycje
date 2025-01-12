import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, ChevronRight, AlertCircle } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';
import { API, authAPI } from '../../services/api';
import { cache } from '../../utils/formatters';

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [alert, setAlert] = useState(null); // Stan dla wyświetlania alertów

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = await authAPI.login(formData);
      localStorage.setItem('token', data.access_token);
      localStorage.setItem('role', data.role);
      let subjects = cache.get("subjects");
      let difficulties = cache.get("difficulties");
      if (!subjects || !difficulties) {
        // Jeśli brak danych w cache, pobierz je z API
        if (!subjects) {
          subjects = await API.setSubjects();
          }
        if (!difficulties) {
          difficulties = await API.setDifficultyLevels();
        }
      }
      // Cache the options
      cache.save('subjects', subjects);
      cache.save('difficultyLevels', difficulties);
      navigate('/');
    } catch (error) {
      console.error('Failed to login:', error);
      setAlert({
        type: 'error',
        title: 'Error',
        description: 'Failed to login. Please try again later.',
        icon: <AlertCircle className="h-5 w-5 text-red-500" />
      })
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center bg-gradient-to-br from-blue-50 to-white px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center">Welcome Back</CardTitle>
          <p className="text-center text-gray-500">Enter your credentials to continue</p>
        </CardHeader>
        <CardContent>
          {/* Jeśli alert istnieje, wyświetl go */}
          {alert && (
            <Alert className="mb-4 flex items-start space-x-3">
              {alert.icon}
              <AlertTitle>{alert.title}</AlertTitle>
              <AlertDescription>{alert.description}</AlertDescription>
            </Alert>
          )}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2 text-gray-700">Email</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                  className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter your email"
                  required
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2 text-gray-700">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <input
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({...formData, password: e.target.value})}
                  className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter your password"
                  required
                />
              </div>
            </div>
            <button
              type="submit"
              className="w-full bg-gradient-to-r from-blue-600 to-blue-700 text-white py-2 px-4 rounded-lg hover:from-blue-700 hover:to-blue-800 transition-colors flex items-center justify-center space-x-2"
            >
              <span>Sign In</span>
              <ChevronRight size={20} />
            </button>
          </form>
          
          <div className="mt-6 text-center">
            <p className="text-gray-600">
              Don&apos;t have an account?{' '}
              <Link to="/register" className="text-blue-600 hover:text-blue-700 font-medium">
                Register here
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Login;