const API_URL = 'http://localhost:5000';

// Pomocnicza funkcja do obsługi zapytań
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

export const API = {
  setSubjects: async () => {
    const response = await fetch(`${API_URL}/api/subjects`);
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API request failed');
    }
    const data = await response.json();
    const formattedSubjects = data.subjects.map(subject => ({
      value: subject.id,
      label: subject.name
    }));
    localStorage.setItem('subjects', JSON.stringify(formattedSubjects));
    return formattedSubjects;
  },

  setDifficultyLevels: async () => {
    const response = await fetch(`${API_URL}/api/difficulty-levels`);
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API request failed');
    }
    const data = await response.json();
    const formattedLevels = data.difficulty_levels.map(level => ({
      value: level.id,
      label: level.name
    }));
    localStorage.setItem('difficultyLevels', JSON.stringify(formattedLevels));
    return formattedLevels;
  },

  getSubjectNameById: (id) => {
    const subjects = JSON.parse(localStorage.getItem('subjects') || '[]');
    const subject = subjects.find(s => s.value === id);
    return subject ? subject.label : null;
  },

  getSubjectIdByName: (name) => {
    const subjects = JSON.parse(localStorage.getItem('subjects') || '[]');
    const subject = subjects.find(s => s.label === name);
    return subject ? subject.value : null;
  },

  getDifficultyNameById: (id) => {
    const levels = JSON.parse(localStorage.getItem('difficultyLevels') || '[]');
    const level = levels.find(l => l.value === id);
    return level ? level.label : null;
  },

  getDifficultyIdByName: (name) => {
    const levels = JSON.parse(localStorage.getItem('difficultyLevels') || '[]');
    const level = levels.find(l => l.label === name);
    return level ? level.value : null;
  },
}

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
  getTeacherList: ({ subject = null, difficulty = null } = {}) => {
    const query = new URLSearchParams();
    if (subject) query.append('subject', subject);
    if (difficulty) query.append('difficulty_id', difficulty);
    return fetchWithAuth(`/api/teacher-list?${query.toString()}`);
  },

  getTeacherLessons: (teacherId) =>
    fetchWithAuth(`/api/lesson/${teacherId}`),

  getAllTeacherReviews: () => 
    fetchWithAuth('/api/teacher-reviews'),

  getOneTeacherReview: (teacherId) =>
    fetchWithAuth(`/api/teacher-reviews/${teacherId}`),

  addTeacherReview: (teacherId, review) =>
    fetchWithAuth(`/api/teacher-reviews/${teacherId}`, {
      method: 'POST',
      body: JSON.stringify(review)
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

  updateTeacher: (formData) =>
    fetchWithAuth('/teacher-update', {
      method: 'PUT',
      body: JSON.stringify(formData),
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