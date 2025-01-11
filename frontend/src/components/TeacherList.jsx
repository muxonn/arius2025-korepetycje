/* eslint-disable react/prop-types */
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { teachersAPI } from '../services/api';

const TeacherList = () => {
  const [teachers, setTeachers] = useState([]);
  const [filters, setFilters] = useState({
    subject: '',
    difficulty: ''
  });

  const fetchTeachers = async () => {
    try {
      const data = await teachersAPI.getTeacherList(filters);
      setTeachers(data.teacher_list);
    } catch (error) {
      console.error('Failed to fetch teachers:', error);
    }
  };

  useEffect(() => {
    fetchTeachers();
  }, [filters]);

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="mb-6 bg-white rounded-lg shadow-md p-4">
        <h2 className="text-2xl font-bold mb-4">Search Teachers</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">Subject</label>
            <input
              type="text"
              value={filters.subject}
              onChange={(e) => setFilters({...filters, subject: e.target.value})}
              className="w-full p-2 border rounded"
              placeholder="Enter subject..."
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Difficulty Level</label>
            <select
              value={filters.difficulty}
              onChange={(e) => setFilters({...filters, difficulty: e.target.value})}
              className="w-full p-2 border rounded"
            >
              <option value="">All Levels</option>
              <option value="elementary">Elementary School</option>
              <option value="middle">Middle School</option>
              <option value="high">High School</option>
            </select>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {teachers.map((teacher) => (
          <TeacherCard key={teacher.id} teacher={teacher} />
        ))}
      </div>
    </div>
  );
};

const TeacherCard = ({ teacher }) => {
  const navigate = useNavigate();
  const [rating, setRating] = useState(0);

  useEffect(() => {
    fetchTeacherRating();
  }, []);

  const fetchTeacherRating = async () => {
    try {
      const response = await fetch(`/api/teacher-reviews/${teacher.id}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      if (response.ok) {
        const data = await response.json();
        const avgRating = data.reviews.reduce((acc, rev) => acc + rev.rating, 0) / data.reviews.length;
        setRating(avgRating || 0);
      }
    } catch (error) {
      console.error('Failed to fetch teacher rating:', error);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-4">
      <h3 className="text-xl font-bold mb-2">{teacher.name}</h3>
      <div className="mb-2">
        <span className="font-medium">Subjects:</span> {teacher.subjects.join(', ')}
      </div>
      <div className="mb-2">
        <span className="font-medium">Difficulty Levels:</span> {teacher.difficulty_levels.join(', ')}
      </div>
      <div className="mb-4">
        <span className="font-medium">Rating:</span> {rating.toFixed(1)} / 5.0
      </div>
      <button
        onClick={() => navigate(`/schedule/${teacher.id}`)}
        className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600"
      >
        Schedule Lesson
      </button>
    </div>
  );
};

export default TeacherList;