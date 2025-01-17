/* eslint-disable react/prop-types */
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { teachersAPI } from '../services/api';
import { Star, Calendar, Book, Award } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { cache, util } from '../utils/formatters';

const TeacherList = () => {
  const [teachers, setTeachers] = useState([]);
  const [filters, setFilters] = useState({
    subject: '',
    difficulty: ''
  });
  const [subjects, setSubjects] = useState([]);
  const [difficulties, setDifficulties] = useState([]);

  useEffect(() => {
    const fetchTeachers = async () => {
      try {
        let cachedData = cache.get("teacherData");
        if (cachedData) {
          console.log("Using cached data");
          setTeachers(cachedData);
        } else {
          console.log("Fetching data from API");
          const data = await teachersAPI.getTeacherList();
          setTeachers(data.teacher_list);
          cache.save("teacherData", data.teacher_list);
        }
      } catch (error) {
        console.error('Failed to fetch teachers:', error);
      }
    };
    fetchTeachers();

    const subjects = cache.get("subjects");
    const difficulties = cache.get("difficultyLevels");
    subjects.sort((a, b) => Number(a.value) - Number(b.value));
    difficulties.sort((a, b) => Number(a.value) - Number(b.value));
    setSubjects(subjects);
    setDifficulties(difficulties);
  }, []);

  useEffect(() => {
    const filterTeachers = async () => {
      try {
        const data = await teachersAPI.getTeacherList(filters);
        setTeachers(data.teacher_list);
      } catch (error) {
        console.error('Failed to fetch teachers:', error);
      }
    }

    filterTeachers();
  }, [filters]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white">
      <div className="max-w-6xl mx-auto p-6">
        <Card className="mb-8">
          <CardHeader>
            <CardTitle className="text-2xl font-bold text-blue-900">Find Your Perfect Tutor</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium mb-2 text-gray-700">Subject</label>
                <div className="relative">
                  <Book className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                  <select
                    value={filters.subject}
                    onChange={(e) => setFilters({...filters, subject: e.target.value})}
                    className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">All Subjects</option>
                    {subjects.map((subject) => (
                      <option key={subject.value} value={subject.value}>
                        {subject.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-2 text-gray-700">Difficulty Level</label>
                <div className="relative">
                  <Award className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                  <select
                    value={filters.difficulty}
                    onChange={(e) => setFilters({...filters, difficulty: e.target.value})}
                    className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
                  >
                    <option value="">All Levels</option>
                    {difficulties.map((level) => (
                      <option key={level.value} value={level.value}>
                        {level.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {teachers.map((teacher) => (
          <TeacherCard key={teacher.id} teacher={teacher} />
        ))}
      </div>
    </div>
  </div>
  );
};

const TeacherCard = ({ teacher }) => {
  const navigate = useNavigate();
  const [rating, setRating] = useState(0);
  const [teacherAvailabilty, setTeacherAvailability] = useState({
    available_from: '',
    available_until: '',
    working_days: ''
  });

  useEffect(() => {
    const fetchTeacherRating = async () => {
      try {
        const data = await teachersAPI.getOneTeacherReview(teacher.id);
        const avgRating = data.reviews.reduce((acc, rev) => acc + rev.rating, 0) / data.reviews.length;
        setRating(avgRating || 0);
      } catch (error) {
        console.error('Failed to fetch teacher rating:', error);
      }
    };
  
    const fetchTeacherCalendar = async () => {
      try {
        const data = await teachersAPI.getCalendar(teacher.id);
        setTeacherAvailability({
          available_from: data.available_from,
          available_until: data.available_until,
          working_days: data.working_days
        })
      } catch (error) {
        console.error('Failed to fetch teacher calendar:', error);
      }
    };

    fetchTeacherRating();
    fetchTeacherCalendar();
  }, [teacher.id]);


  return (
    <Card className="hover:shadow-lg transition-shadow duration-300">
      <CardContent className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h3 className="text-xl font-bold text-gray-900 mb-1">{teacher.name}</h3>
            <div className="flex items-center space-x-1 text-yellow-500">
              <Star size={16} fill="currentColor" />
              <span className="text-sm">{rating.toFixed(1)}</span>
            </div>
          </div>
          <div className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">
            {teacher.hourly_rate} $/h
          </div>
        </div>

        <div className="space-y-3 mb-6">
          <div className="flex items-center text-gray-600">
            <Book size={16} className="mr-2" />
            <span className="text-sm">{util.getSubjectNamesFromIdString(teacher.subjects).join(", ")}</span>
          </div>
          <div className="flex items-center text-gray-600">
            <Award size={16} className="mr-2" />
            <span className="text-sm">{util.getDifficultyNamesFromIdString(teacher.difficulty_levels).join(", ")}</span>
          </div>
          <div className="flex items-center text-gray-600">
            <Calendar size={16} className="mr-2" />
            <span className="text-sm">{util.getWorkingDaysFromIdString(teacherAvailabilty.working_days).join(", ")}</span>
          </div>
          <div className="flex items-center text-gray-600">
            <Calendar size={16} className="mr-2" />
            <span className="text-sm">{teacherAvailabilty.available_from} - {teacherAvailabilty.available_until}</span>
          </div>
        </div>

        <button
          onClick={() => navigate(`/schedule/${teacher.id}`)}
          className="w-full bg-gradient-to-r from-blue-600 to-blue-700 text-white py-2 px-4 rounded-lg hover:from-blue-700 hover:to-blue-800 transition-colors flex items-center justify-center space-x-2"
        >
          <Calendar size={20} />
          <span>Schedule Lesson</span>
        </button>
      </CardContent>
    </Card>
  );
};

export default TeacherList;