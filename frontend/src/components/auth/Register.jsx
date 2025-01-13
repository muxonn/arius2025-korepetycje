import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, UserPlus, BookOpen, User, AlertCircle, DollarSign } from 'lucide-react';
import Select from 'react-select';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';
import { authAPI, API } from '../../services/api';
import { cache } from '../../utils/formatters';

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'student',
    subject_ids: [],
    difficulty_ids: [],
    hourly_rate: ''
  });
  
  const [alert, setAlert] = useState(null);
  const [subjectOptions, setSubjectOptions] = useState([]);
  const [difficultyOptions, setDifficultyOptions] = useState([]);

  useEffect(() => {
    const loadOptions = async () => {
      try {
        let subjects = JSON.parse(localStorage.getItem('subjects') || '[]');
        let difficulties = JSON.parse(localStorage.getItem('difficultyLevels') || '[]');

        if (subjects.length === 0 || difficulties.length === 0) {
          // Jeśli brak danych w cache, pobierz je z API
          if (subjects.length === 0) {
            subjects = await API.setSubjects();
            }
          if (difficulties.length === 0) {
            difficulties = await API.setDifficultyLevels();
          }
        }
        // Cache the options
        cache.save('subjects', subjects);
        cache.save('difficultyLevels', difficulties);

        setSubjectOptions(subjects);
        setDifficultyOptions(difficulties);
      } catch (error) {
        console.error('Failed to load options:', error);
        setAlert({
          type: 'error',
          title: 'Error',
          description: error.toLocaleString(),
          icon: <AlertCircle className="h-5 w-5 text-red-500" />
        });
      }
    };

    loadOptions();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Transform selected options back to the format expected by the API
      const transformedData = {
        ...formData,
        subject_ids: formData.subject_ids.map(s => s.value).join(','),
        difficulty_ids: formData.difficulty_ids.map(d => d.value).join(','),
        hourly_rate: formData.role === 'teacher' ? parseFloat(formData.hourly_rate) : undefined
      };
      
      await authAPI.register(transformedData);
      navigate('/login');
    } catch (error) {
      console.error('Failed to register:', error);
      setAlert({
        type: 'error',
        title: 'Error',
        description: error.toLocaleString(),
        icon: <AlertCircle className="h-5 w-5 text-red-500" />
      });
    }
  };

  const handleHourlyRateChange = (e) => {
    const value = e.target.value;
    // Pozwól na pusty string lub liczby z maksymalnie dwoma miejscami po przecinku
    if (value === '' || /^\d+\.?\d{0,2}$/.test(value)) {
      setFormData({ ...formData, hourly_rate: value });
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center bg-gradient-to-br from-blue-50 to-white px-4 py-8">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center">Create Account</CardTitle>
          <p className="text-center text-gray-500">Join our learning community today</p>
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
              <label className="block text-sm font-medium mb-2 text-gray-700">Full Name</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter your full name"
                  required
                />
              </div>
            </div>
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
            <div>
              <label className="block text-sm font-medium mb-2 text-gray-700">Role</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({...formData, role: e.target.value})}
                  className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
                >
                  <option value="student">Student</option>
                  <option value="teacher">Teacher</option>
                </select>
              </div>
            </div>

            {formData.role === 'teacher' && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-2 text-gray-700">Subjects</label>
                  <div className="relative">
                    <BookOpen className="absolute left-3 top-3 text-gray-400 z-10" size={20} />
                    <Select
                      isMulti
                      options={subjectOptions}
                      value={formData.subject_ids}
                      onChange={(selected) => setFormData({...formData, subject_ids: selected || []})}
                      className="pl-8"
                      classNamePrefix="select"
                      placeholder="Select subjects..."
                      getOptionValue={(option) => option.value}
                      getOptionLabel={(option) => option.label}
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2 text-gray-700">Difficulty Levels</label>
                  <div className="relative">
                    <BookOpen className="absolute left-3 top-3 text-gray-400 z-10" size={20} />
                    <Select
                      isMulti
                      options={difficultyOptions}
                      value={formData.difficulty_ids}
                      onChange={(selected) => setFormData({...formData, difficulty_ids: selected || []})}
                      className="pl-8"
                      classNamePrefix="select"
                      placeholder="Select difficulty levels..."
                      getOptionValue={(option) => option.value}
                      getOptionLabel={(option) => option.label}
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2 text-gray-700">Hourly Rate ($)</label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={formData.hourly_rate}
                      onChange={handleHourlyRateChange}
                      className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Enter your hourly rate"
                      required={formData.role === 'teacher'}
                    />
                  </div>
                </div>
              </>
            )}

            <button
              type="submit"
                  className="w-full bg-gradient-to-r from-blue-600 to-blue-700 text-white py-2 px-4 rounded-lg hover:from-blue-700 hover:to-blue-800 transition-colors flex items-center justify-center space-x-2"
            >
              <UserPlus size={20} />
              <span>Create Account</span>
            </button>
          </form>
          
          <div className="mt-6 text-center">
            <p className="text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-blue-600 hover:text-blue-700 font-medium">
                Sign in
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Register;