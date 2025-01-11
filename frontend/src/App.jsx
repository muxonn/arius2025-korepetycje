/* eslint-disable react/prop-types */
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
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
    <nav className="bg-white shadow-md">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="font-bold text-xl text-blue-600">
              TutorApp
            </Link>
          </div>
          
          {localStorage.getItem('token') ? (
            <div className="flex items-center space-x-4">
              <Link 
                to="/teachers" 
                className="text-gray-600 hover:text-gray-900"
              >
                Find Teachers
              </Link>
              <Link 
                to="/history" 
                className="text-gray-600 hover:text-gray-900"
              >
                Lesson History
              </Link>
              <button
                onClick={handleLogout}
                className="text-red-600 hover:text-red-800"
              >
                Logout
              </button>
            </div>
          ) : (
            <div className="flex items-center space-x-4">
              <Link 
                to="/login" 
                className="text-gray-600 hover:text-gray-900"
              >
                Login
              </Link>
              <Link 
                to="/register" 
                className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
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
    <div className="max-w-4xl mx-auto mt-16 px-4">
      <h1 className="text-4xl font-bold text-center mb-8">
        Welcome to TutorApp
      </h1>
      <p className="text-xl text-center text-gray-600 mb-8">
        Find the perfect tutor and start learning today
      </p>
      <div className="flex justify-center space-x-4">
        <Link
          to="/register"
          className="bg-blue-500 text-white px-6 py-3 rounded-lg hover:bg-blue-600"
        >
          Get Started
        </Link>
        <Link
          to="/teachers"
          className="bg-white text-blue-500 px-6 py-3 rounded-lg border-2 border-blue-500 hover:bg-blue-50"
        >
          Browse Teachers
        </Link>
      </div>
    </div>
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

        <footer className="bg-white border-t mt-auto">
          <div className="max-w-7xl mx-auto py-6 px-4">
            <p className="text-center text-gray-600">
              © {new Date().getFullYear()} TutorApp. All rights reserved.
            </p>
          </div>
        </footer>
      </div>
    </Router>
  );
};

export default App;