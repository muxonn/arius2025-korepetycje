/* eslint-disable react/prop-types */
import { useState, useEffect } from 'react';
import { Alert } from '@/components/ui/alert';
import { invoicesAPI, lessonsAPI, reportsAPI, teachersAPI } from '../services/api';

const LessonHistory = () => {
  const [lessons, setLessons] = useState([]);
  const [reports, setReports] = useState([]);
  // const [userRole, setUserRole] = useState(null);

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
    <div className="max-w-4xl mx-auto p-6">
      <h2 className="text-2xl font-bold mb-6">Lesson History</h2>
      
      <div className="space-y-6">
        {lessons.map((lesson) => (
          <LessonCard 
            key={lesson.id} 
            lesson={lesson}
            report={reports.find(r => r.lesson_id === lesson.id)}
            onLessonUpdated={fetchLessons}
          />
        ))}
      </div>
    </div>
  );
};

const LessonCard = ({ lesson, report, onLessonUpdated }) => {
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [showReportForm, setShowReportForm] = useState(false);

  const handleCreateInvoice = async () => {
    try {
      await invoicesAPI.generateInvoice(lesson.id);
      Alert({ 
        title: 'Success', 
        description: 'Invoice has been generated and sent to your email.' 
      });
    } catch (error) {
      console.error('Failed to create invoice:', error);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-xl font-bold">{lesson.subject}</h3>
          <p className="text-gray-600">
            {new Date(lesson.date).toLocaleString()}
          </p>
        </div>
        <div className="space-x-2">
          {lesson.status === 'completed' && !report && (
            <button
              onClick={() => setShowReportForm(true)}
              className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
            >
              Add Report
            </button>
          )}
          {lesson.status === 'completed' && !lesson.reviewed && (
            <button
              onClick={() => setShowReviewForm(true)}
              className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
            >
              Add Review
            </button>
          )}
          <button
            onClick={handleCreateInvoice}
            className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
          >
            Generate Invoice
          </button>
        </div>
      </div>

      {report && (
        <div className="mt-4 p-4 bg-gray-50 rounded">
          <h4 className="font-bold mb-2">Lesson Report</h4>
          <p><strong>Progress Rating:</strong> {report.progress_rating}/5</p>
          <p><strong>Comment:</strong> {report.comment}</p>
          {report.homework && (
            <p><strong>Homework:</strong> {report.homework}</p>
          )}
        </div>
      )}

      {showReviewForm && (
        <ReviewForm 
          key={lesson.id} 
          teacherId={lesson.teacher_id}
          onSubmit={() => {
            setShowReviewForm(false);
            onLessonUpdated();
          }}
          onCancel={() => setShowReviewForm(false)}
        />
      )}

      {showReportForm && (
        <ReportForm 
          lessonId={lesson.id}
          onSubmit={() => {
            setShowReportForm(false);
            onLessonUpdated();
          }}
          onCancel={() => setShowReportForm(false)}
        />
      )}
    </div>
  );
};

const ReviewForm = ({ teacherId, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    rating: 5,
    comment: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await teachersAPI.addTeacherReview(teacherId, formData);
      onSubmit();
      Alert({ 
        title: 'Success', 
        description: 'Review has been submitted.' 
      });
    } catch (error) {
      console.error('Failed to submit review:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="mt-4 p-4 bg-gray-50 rounded">
      <div className="mb-4">
        <label className="block text-sm font-medium mb-1">Rating</label>
        <select
          value={formData.rating}
          onChange={(e) => setFormData({...formData, rating: parseInt(e.target.value)})}
          className="w-full p-2 border rounded"
        >
          {[5, 4, 3, 2, 1].map((value) => (
            <option key={value} value={value}>{value} stars</option>
          ))}
        </select>
      </div>
      <div className="mb-4">
        <label className="block text-sm font-medium mb-1">Comment</label>
        <textarea
          value={formData.comment}
          onChange={(e) => setFormData({...formData, comment: e.target.value})}
          className="w-full p-2 border rounded"
          rows="3"
        />
      </div>
      <div className="flex justify-end space-x-2">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 border rounded hover:bg-gray-100"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Submit Review
        </button>
      </div>
    </form>
  );
};

const ReportForm = ({ lessonId, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    progress_rating: 5,
    comment: '',
    homework: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await reportsAPI.addReport(lessonId, formData);
      onSubmit();
      Alert({ 
        title: 'Success', 
        description: 'Report has been submitted.' 
      });
    } catch (error) {
      console.error('Failed to submit report:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="mt-4 p-4 bg-gray-50 rounded">
      <div className="mb-4">
        <label className="block text-sm font-medium mb-1">Progress Rating</label>
        <select
          value={formData.progress_rating}
          onChange={(e) => setFormData({...formData, progress_rating: parseInt(e.target.value)})}
          className="w-full p-2 border rounded"
        >
          {[5, 4, 3, 2, 1].map((value) => (
            <option key={value} value={value}>{value} stars</option>
          ))}
        </select>
      </div>
      <div className="mb-4">
        <label className="block text-sm font-medium mb-1">Comment</label>
        <textarea
          value={formData.comment}
          onChange={(e) => setFormData({...formData, comment: e.target.value})}
          className="w-full p-2 border rounded"
          rows="3"
        />
      </div>
      <div className="mb-4">
        <label className="block text-sm font-medium mb-1">Homework</label>
        <textarea
          value={formData.homework}
          onChange={(e) => setFormData({...formData, homework: e.target.value})}
          className="w-full p-2 border rounded"
          rows="3"
        />
      </div>
      <div className="flex justify-end space-x-2">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 border rounded hover:bg-gray-100"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Submit Report
        </button>
      </div>
    </form>
  );
};

export default LessonHistory;