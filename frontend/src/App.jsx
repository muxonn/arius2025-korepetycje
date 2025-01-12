/* eslint-disable react/prop-types */
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { BookOpen, History, LogOut, User, Users } from 'lucide-react';
import Register from './components/auth/Register';
import Login from './components/auth/Login'
import TeacherList from './components/TeacherList';
import Schedule from './components/Schedule';
import LessonHistory from './components/LessonHistory';

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
};

const NavBar = () => {
  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/login';
  };

  return (
    <nav className="bg-gradient-to-r from-blue-600 to-blue-800 text-white shadow-lg w-full">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex justify-between items-center h-16 w-full">
          <div className="flex-shrink-0">
            <Link to="/" className="flex items-center space-x-2">
              <BookOpen size={24} />
              <span className="font-bold text-xl">TutorApp</span>
            </Link>
          </div>
          
          {localStorage.getItem('token') ? (
            <div className="flex items-center space-x-6 flex-nowrap">
              <Link 
                to="/teachers" 
                className="flex items-center space-x-1 hover:text-blue-200 transition-colors whitespace-nowrap"
              >
                <Users size={20} />
                <span>Find Teachers</span>
              </Link>
              <Link 
                to="/history" 
                className="flex items-center space-x-1 hover:text-blue-200 transition-colors whitespace-nowrap"
              >
                <History size={20} />
                <span>Lesson History</span>
              </Link>
              <button
                onClick={handleLogout}
                className="flex items-center space-x-1 text-red-300 hover:text-red-100 transition-colors whitespace-nowrap"
              >
                <LogOut size={20} />
                <span>Logout</span>
              </button>
            </div>
          ) : (
            <div className="flex items-center space-x-4">
              <Link 
                to="/login" 
                className="flex items-center space-x-1 hover:text-blue-200 transition-colors"
              >
                <User size={20} />
                <span>Login</span>
              </Link>
              <Link 
                to="/register" 
                className="bg-white text-blue-600 px-4 py-2 rounded-full hover:bg-blue-50 transition-colors"
              >
                Register
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

const Home = () => {
  return (
    <div className="relative">
      <div className="absolute inset-0 bg-gradient-to-br from-blue-100 to-blue-50 -z-10" />
      <div className="max-w-4xl mx-auto mt-16 px-4 pb-16">
        <div className="text-center space-y-6">
          <h1 className="text-5xl font-bold text-blue-900">
            Welcome to TutorApp
          </h1>
          <p className="text-xl text-blue-700 max-w-2xl mx-auto">
            Connect with expert tutors and transform your learning journey. 
            Schedule personalized lessons that fit your needs and goals.
          </p>
          <div className="flex justify-center space-x-4 mt-8">
            <Link
              to="/register"
              className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-8 py-4 rounded-full text-lg font-medium hover:from-blue-700 hover:to-blue-800 transition-colors shadow-lg"
            >
              Get Started Today
            </Link>
            <Link
              to="/teachers"
              className="bg-white text-blue-600 px-8 py-4 rounded-full text-lg font-medium border-2 border-blue-600 hover:bg-blue-50 transition-colors shadow-md"
            >
              Browse Teachers
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

const Footer = () => {
  return (
    <footer className="bg-gradient-to-r from-gray-800 to-gray-900 text-white">
      <div className="max-w-7xl mx-auto py-12 px-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="text-lg font-semibold mb-4">About TutorApp</h3>
            <p className="text-gray-400">
              Connecting students with qualified tutors for personalized learning experiences.
            </p>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2 text-gray-400">
              <li><Link to="/teachers" className="hover:text-white transition-colors">Find Teachers</Link></li>
              <li><Link to="/register" className="hover:text-white transition-colors">Become a Tutor</Link></li>
              <li><Link to="/help" className="hover:text-white transition-colors">Help Center</Link></li>
            </ul>
          </div>
          <div>
            <h3 className="text-lg font-semibold mb-4">Contact</h3>
            <p className="text-gray-400">
              Email: support@tutorapp.com<br />
              Phone: (555) 123-4567
            </p>
          </div>
        </div>
        <div className="mt-8 pt-8 border-t border-gray-700 text-center text-gray-400">
          <p>© {new Date().getFullYear()} TutorApp. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

const App = () => {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <NavBar />
        
        <main className="py-8">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/teachers"
              element={
                <PrivateRoute>
                  <TeacherList />
                </PrivateRoute>
              }
            />
            <Route
              path="/schedule/:teacherId"
              element={
                <PrivateRoute>
                  <Schedule />
                </PrivateRoute>
              }
            />
            <Route
              path="/history"
              element={
                <PrivateRoute>
                  <LessonHistory />
                </PrivateRoute>
              }
            />
          </Routes>
        </main>
        
        <Footer />
      </div>
    </Router>
  );
};

export default App;