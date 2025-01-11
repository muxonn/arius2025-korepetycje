const API_URL = 'http://localhost:5000';

// Funkcja pomocnicza do obsługi zapytań
const fetchWithAuth = async (endpoint, options = {}) => {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers
  };

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'API request failed');
  }

  return response.json();
};

// Autentykacja
export const authAPI = {
  register: (userData) => 
    fetchWithAuth('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    }),

  login: (credentials) => 
    fetchWithAuth('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    })
};

// Nauczyciele
export const teachersAPI = {
  getTeacherList: ({ subject = null, difficulty = null}) => {
    const query = new URLSearchParams();
    if (subject) query.append('subject', subject);
    if (difficulty) query.append('difficulty', difficulty);
    return fetchWithAuth(`/api/teacher-list?${query.toString()}`);
  },

  getAllTeacherReviews: () => 
    fetchWithAuth('/api/teacher-reviews'),

  getOneTeacherReview: (teacherId) =>
    fetchWithAuth(`/api/teacher-reviews/${teacherId}`),

  addTeacherReview: (teacherId, review) =>
    fetchWithAuth(`/api/teacher-reviews/${teacherId}`, {
      method: 'POST',
      body: JSON.stringify({ review })
    }),
  
  deleteTeacherReview: (teacherId) =>
    fetchWithAuth(`/api/teacher-reviews/${teacherId}`, {
      method: 'DELETE'
    }),

  getCalendar: (teacherId) => 
    fetchWithAuth(`/api/calendar/${teacherId}`),

  setTeacherCalendar: (calendarData) => 
    fetchWithAuth('/api/calendar', {
      method: 'POST',
      body: JSON.stringify(calendarData),
    }),
};

// Lekcje
export const lessonsAPI = {
  getLessons: () => 
    fetchWithAuth('/api/lesson'),

  scheduleLesson: (lessonData) => 
    fetchWithAuth('/api/lesson', {
      method: 'POST',
      body: JSON.stringify(lessonData),
    }),
};

// Raporty
export const reportsAPI = {
  getReports: () => 
    fetchWithAuth('/api/report'),

  addReport: (lessonId, reportData) => 
    fetchWithAuth('/api/report', {
      method: 'POST',
      body: JSON.stringify({ lesson_id: lessonId, ...reportData}),
    }),
};

// Faktury
export const invoicesAPI = {
  generateInvoice: (lessonId) => 
    fetchWithAuth('/api/invoice', {
      method: 'POST',
      body: JSON.stringify({ lesson_id: lessonId }),
    }),
};