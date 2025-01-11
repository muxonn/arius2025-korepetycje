import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Calendar, Clock, BookOpen, AlertCircle } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Alert } from '@/components/ui/alert';
import { lessonsAPI, teachersAPI } from '../services/api';

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

    try {
      const dateTime = `${selectedDate} ${selectedTime}`;
      await lessonsAPI.scheduleLesson({
      teacher_id: teacherId,
        subject,
      date: dateTime
      });
      
      Alert({ 
        title: 'Success', 
        description: 'Your lesson has been scheduled successfully!'
      });
    } catch (error) {
      Alert({
        icon: <AlertCircle className="h-5 w-5" />,
        title: 'Error',
        description: error
      });
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
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white">
      <div className="max-w-4xl mx-auto p-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-2xl font-bold text-blue-900">Schedule Your Lesson</CardTitle>
            <p className="text-gray-600">Choose your preferred date and time</p>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium mb-2 text-gray-700">Select Date</label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="date"
                      onChange={(e) => setSelectedDate(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      min={new Date().toISOString().split('T')[0]}
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2 text-gray-700">Select Time</label>
                  <div className="relative">
                    <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <select
                      onChange={(e) => setSelectedTime(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
                    >
                      <option value="">Select time...</option>
                      {generateTimeSlots().map((time) => (
                        <option key={time} value={time}>{time}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="md:col-span-2">
                  <label className="block text-sm font-medium mb-2 text-gray-700">Subject</label>
                  <div className="relative">
                    <BookOpen className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="text"
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Enter subject..."
                    />
                  </div>
                </div>

                <div className="flex justify-end space-x-3">
                  <button
                    onClick={handleScheduleLesson}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                    disabled={!selectedDate || !selectedTime || !subject}
                  >
                    Schedule Lesson
                  </button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Schedule;