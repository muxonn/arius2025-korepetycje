/* eslint-disable react/prop-types */
import { useState, useEffect } from 'react';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';
import { API, invoicesAPI, lessonsAPI, reportsAPI, teachersAPI } from '../services/api';
import { Star, Clock, Book, AlertCircle, CheckCircle, MessageSquare, FilePlus2 } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Label } from "@/components/ui/label";
import { cache } from '../utils/formatters';

const LessonHistory = () => {
  const [lessons, setLessons] = useState([]);
  const [reports, setReports] = useState([]);

  useEffect(() => {
    fetchLessons();
    fetchReports();
  }, []);

  const fetchLessons = async () => {
    try {
      const data = await lessonsAPI.getLessons();
      setLessons(data.lesson_list);
    } catch (error) {
      console.error('Failed to fetch lessons:', error);
    }
  }

  const fetchReports = async () => {
    try {
      const data = await reportsAPI.getReports();
      setReports(data.report_list);
    } catch (error) {
      console.error('Failed to fetch reports:', error);
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white">
      <div className="max-w-4xl mx-auto p-6">
        <h2 className="text-3xl font-bold text-blue-900 mb-8">Lesson history</h2>

        <div className="space-y-6">
          {lessons.map((lesson) => (
            <LessonCard 
              key={lesson.id} 
              lesson={lesson}
              report={reports.find(r => r.lesson_id === lesson.id)}
              onLessonUpdated={() => fetchLessons()}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

const LessonCard = ({ lesson, report, onLessonUpdated }) => {
  const [teacher, setTeacher] = useState(null);
  const [alert, setAlert] = useState(null);
  const userRole = localStorage.getItem('role');

  useEffect(() => {
    // Pobierz dane nauczyciela na podstawie lesson.teacher_id
    const teacherData = cache.get('teacherData'); // Pobieramy dane z localStorage
    if (teacherData) {
      const teacherInfo = teacherData.find(t => t.id === lesson.teacher_id);
      setTeacher(teacherInfo);
    }
  }, [lesson.teacher_id]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'completed': return 'bg-green-100 text-green-800';
      case 'scheduled': return 'bg-blue-100 text-blue-800';
      case 'cancelled': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const renderActions = () => {
    const actions = [];

    // Only students can leave reviews
    if (userRole === 'student' && lesson.status === 'completed') {
      actions.push(
        <ReviewForm 
          teacherId={lesson.teacher_id}
          onSubmit={() => onLessonUpdated()}
        />
      );
    }

    // Only teachers can add reports
    if (userRole === 'teacher' && !lesson.is_reported) {
      actions.push(
        <ReportForm 
          lessonId={lesson.id}
          onSubmit={() => onLessonUpdated()}
        />
      );
    }

    // Invoice generation available for all users
    actions.push(
      <button
        key="invoice"
        onClick={handleCreateInvoice}
        className="flex items-center space-x-2 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
      >
        <FilePlus2 size={16} />
        <span>Generate Invoice</span>
      </button>
    );

    return actions;
  };

  const handleCreateInvoice = async () => {
    try {
      await invoicesAPI.generateInvoice(lesson.id);
      setAlert({
        type: 'success',
        title: 'Success',
        description: 'Invoice has been generated and sent to your email.',
        icon: <AlertCircle className="h-5 w-5 text-green-500" />
      });
    } catch (error) {
      console.error('Failed to create invoice:', error);
    }
  };

  return (
    <Card className="mb-4">
      <CardContent className="p-6">
        {/* Jeśli alert istnieje, wyświetl go */}
        {alert && (
          <Alert className="mb-4 flex items-start space-x-3">
            {alert.icon}
            <AlertTitle>{alert.title}</AlertTitle>
            <AlertDescription>{alert.description}</AlertDescription>
          </Alert>
        )}
        <div className="flex items-center justify-between">
          {/* Left section - Subject, Teacher, and Date */}
          <div className="flex items-center space-x-8">
            <div>
              <h3 className="text-xl font-bold text-gray-900"><strong>Subject: </strong>{API.getSubjectNameById(lesson.subject)}</h3>
              <strong>Lesson id: </strong>{lesson.id}
              <div className="flex items-center mt-1">
                <Clock size={16} className="mr-2 text-gray-500" />
                <span className="text-gray-600">
                  {new Date(lesson.date).toLocaleString()}
                </span>
              </div>
              {teacher && (
                <div className="mt-1 text-gray-700">
                  <strong>Teacher:</strong> {teacher.name}
                </div>
              )}
            </div>
          </div>
          
          {/* Middle section - Status and Price */}
          <div className="flex items-center">
            <div className="text-gray-700">
              <strong>Price:</strong> ${lesson.price || teacher?.hourly_rate || 'N/A'}
            </div>
            <span className={`px-4 py-2 rounded-full text-sm font-medium ${getStatusColor(lesson.status)}`}>
              {lesson.status.charAt(0).toUpperCase() + lesson.status.slice(1)}
            </span>
          </div>

          {/* Right section - Actions */}
          <div className="flex items-center space-x-4 flex-nowrap overflow-x-auto">
            {renderActions()}
          </div>
        </div>

        {report && (
        <div className="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-100">
          <div className="flex items-center space-x-2 mb-3">
            <CheckCircle size={20} className="text-blue-600" />
            <h4 className="font-semibold text-blue-900">Lesson Report</h4>
          </div>
            <div className="grid grid-cols-3 gap-4">
              <div className="flex items-center">
                <Star size={16} className="text-yellow-500 mr-2" />
                <span><strong>Progress:</strong> {report.progress_rating}/5</span>
              </div>
              <div className="flex items-center">
                <MessageSquare size={16} className="text-gray-500 mr-2" />
                <span><strong>Feedback:</strong> {report.comment}</span>
              </div>
              {report.homework && (
                <div className="flex items-center">
                <Book size={16} className="text-gray-500 mr-2" />
                <span><strong>Homework:</strong> {report.homework}</span>
                </div>
              )}
            </div>
          </div>
        )}
        {!report && (
          <div>No report for this lesson yet.</div>
        )}
      </CardContent>
    </Card>
  );
};

const ReviewForm = ({ teacherId, onSubmit }) => {
  const [formData, setFormData] = useState({
    rating: 5,
    comment: ''
  });
  const [alert, setAlert] = useState(null); // Stan dla wyświetlania alertów

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await teachersAPI.addTeacherReview(teacherId, formData);
      console.log(formData)
      onSubmit();
      setAlert({
        type: 'success',
        title: 'Success',
        description: 'Review has been submitted.',
        icon: <AlertCircle className="h-5 w-5 text-green-500" />
      });
    } catch (error) {
      console.error('Failed to submit review:', error);
      setAlert({
        type: 'error',
        title: 'Error',
        description: error.toLocaleString(),
        icon: <AlertCircle className="h-5 w-5 text-red-500" />
      })
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Jeśli alert istnieje, wyświetl go */}
      {alert && (
        <Alert className="mb-4 flex items-start space-x-3">
          {alert.icon}
          <AlertTitle>{alert.title}</AlertTitle>
          <AlertDescription>{alert.description}</AlertDescription>
        </Alert>
      )}
      <h4 className="text-lg font-semibold text-gray-900 mb-4">Write a Review</h4>
      
      <StarRating
        value={formData.rating}
        onChange={(value) => setFormData({...formData, rating: value})}
        label="Rating"
      />

      <div>
        <label className="block text-sm font-medium mb-2 text-gray-700">Comment</label>
        <textarea
          value={formData.comment}
          onChange={(e) => setFormData({...formData, comment: e.target.value})}
          className="w-full p-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          rows="3"
          placeholder="Share your experience..."
        />
      </div>
      <div className="flex justify-end space-x-3">
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Submit Review
        </button>
      </div>
    </form>
  );
};

const StarRating = ({ value, onChange, label }) => {
  const [hoverValue, setHoverValue] = useState(0);

  return (
    <div className="space-y-2">
      {label && <Label>{label}</Label>}
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onMouseEnter={() => setHoverValue(star)}
            onMouseLeave={() => setHoverValue(0)}
            onClick={() => onChange(star)}
            className="p-1 transition-colors duration-200 rounded-md hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            <Star
              size={24}
              className={`
                ${(hoverValue || value) >= star 
                  ? 'fill-yellow-400 text-yellow-400' 
                  : 'fill-gray-200 text-gray-200'}
                transition-colors duration-200
              `}
            />
          </button>
        ))}
        <span className="ml-2 text-sm text-gray-500">
          {value ? `${value} star${value !== 1 ? 's' : ''}` : 'Rate'}
        </span>
      </div>
    </div>
  );
};

const ReportForm = ({ lessonId, onSubmit }) => {
  const [formData, setFormData] = useState({
    progress_rating: 5,
    comment: '',
    homework: ''
  });
  const [alert, setAlert] = useState(null); // Stan dla wyświetlania alertów

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await reportsAPI.addReport(lessonId, formData);
      onSubmit();
      setAlert({
        type: 'success',
        title: 'Success',
        description: 'Report has been submitted.',
        icon: <AlertCircle className="h-5 w-5 text-green-500" />
      });
    } catch (error) {
      console.error('Failed to submit report:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Jeśli alert istnieje, wyświetl go */}
      {alert && (
        <Alert className="mb-4 flex items-start space-x-3">
          {alert.icon}
          <AlertTitle>{alert.title}</AlertTitle>
          <AlertDescription>{alert.description}</AlertDescription>
        </Alert>
      )}
      <h4 className="text-lg font-semibold text-gray-900 mb-4">Create Lesson Report</h4>
      <div>
        <label className="block text-sm font-medium mb-2 text-gray-700">Progress Rating</label>
        <div className="flex items-center space-x-1">
          {[5, 4, 3, 2, 1].map((value) => (
            <button
              key={value}
              type="button"
              onClick={() => setFormData({...formData, progress_rating: value})}
              className={`p-1 ${formData.progress_rating >= value ? 'text-yellow-400' : 'text-gray-300'}`}
            >
              <Star size={24} fill="currentColor" />
            </button>
          ))}
        </div>
      </div>
      <div>
        <label className="block text-sm font-medium mb-2 text-gray-700">Comment</label>
        <textarea
          value={formData.comment}
          onChange={(e) => setFormData({...formData, comment: e.target.value})}
          className="w-full p-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          rows="3"
          placeholder="Describe the lesson progress..."
        />
      </div>
      <div>
        <label className="block text-sm font-medium mb-2 text-gray-700">Homework Assignment</label>
        <textarea
          value={formData.homework}
          onChange={(e) => setFormData({...formData, homework: e.target.value})}
          className="w-full p-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          rows="3"
          placeholder="Assign homework for the student..."
        />
      </div>
      <div className="flex justify-end space-x-3">
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
        >
          <CheckCircle size={20} />
          <span>Submit Report</span>
        </button>
      </div>
    </form>
  );
};

export default LessonHistory;