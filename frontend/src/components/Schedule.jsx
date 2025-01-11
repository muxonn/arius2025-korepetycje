import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { lessonsAPI, teachersAPI } from '../services/api';
import { Alert } from '@/components/ui/alert';

const Schedule = () => {
  const { teacherId } = useParams();
  const [calendar, setCalendar] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);
  const [subject, setSubject] = useState('');

  useEffect(() => {
    const fetchCalendar = async () => {
      try {
        const data = await teachersAPI.getCalendar(teacherId);
        setCalendar(data);
      } catch (error) {
        console.error('Failed to fetch calendar:', error);
      }
    };

    fetchCalendar();
  }, [teacherId]);

  

  const handleScheduleLesson = async () => {
    if (!selectedDate || !selectedTime || !subject) return;

    const dateTime = `${selectedDate} ${selectedTime}`;
    const lessonData = {
      teacher_id: teacherId,
      subject: subject,
      date: dateTime
    };
    try {
      await lessonsAPI.scheduleLesson(lessonData);
      Alert({ 
        title: 'Success', 
        description: 'Lesson has been scheduled.' 
      });
    } catch (error) {
      console.error('Failed to schedule lesson:', error);
    }
  };

  const generateTimeSlots = () => {
    if (!calendar) return [];
    
    const slots = [];
    let current = new Date(`2000-01-01 ${calendar.available_from}`);
    const end = new Date(`2000-01-01 ${calendar.available_until}`);
    
    while (current < end) {
      slots.push(current.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }));
      current.setHours(current.getHours() + 1);
    }
    
    return slots;
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-6">Schedule a Lesson</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium mb-1">Select Date</label>
            <input
              type="date"
              onChange={(e) => setSelectedDate(e.target.value)}
              className="w-full p-2 border rounded"
              min={new Date().toISOString().split('T')[0]}
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Select Time</label>
            <select
              onChange={(e) => setSelectedTime(e.target.value)}
              className="w-full p-2 border rounded"
            >
              <option value="">Select time...</option>
              {generateTimeSlots().map((time) => (
                <option key={time} value={time}>{time}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Subject</label>
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              className="w-full p-2 border rounded"
              placeholder="Enter subject..."
            />
          </div>
        </div>

        <button
          onClick={handleScheduleLesson}
          className="mt-6 w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600"
          disabled={!selectedDate || !selectedTime || !subject}
        >
          Schedule Lesson
        </button>
      </div>
    </div>
  );
};

export default Schedule;