import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import DatePicker, { registerLocale } from 'react-datepicker';
import { format, getDay, setHours, setMinutes } from 'date-fns';
import { pl } from 'date-fns/locale/pl';
import "react-datepicker/dist/react-datepicker.css";
import { Calendar, BookOpen, AlertCircle, CheckCircle, Space } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';
import { lessonsAPI, teachersAPI } from '../services/api';
import { util } from '../utils/formatters';
registerLocale('pl', pl)

const Schedule = () => {
  const { teacherId } = useParams();
  const [calendar, setCalendar] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [excludedTimes, setExcludedTimes] = useState(null);
  const [lessons, setLessons] = useState(null);
  const [subject, setSubject] = useState('');
  const [alert, setAlert] = useState(null); // Stan dla wyświetlania alertów

  useEffect(() => {
    const fetchCalendar = async () => {
      try {
        const data = await teachersAPI.getCalendar(teacherId);
        setCalendar(data);
      } catch (error) {
        console.error('Failed to fetch calendar:', error);
        setAlert({
          type: 'error',
          title: 'Error',
          description: 'Failed to fetch the calendar. Please try again later.',
          icon: <AlertCircle className="h-5 w-5 text-red-500" />
        });
      }
    };

    const fetchTeacherLessons = async () => {
      try {
        const data = await teachersAPI.getTeacherLessons(teacherId);
        setLessons(data.lesson_list);
      } catch (error) {
        console.error('Failed to fetch teacher lessons:', error);
        setAlert({
          type: 'error',
          title: 'Error',
          description: 'Failed to fetch teacher lessons. Please try again later.',
          icon: <AlertCircle className="h-5 w-5 text-red-500" />
        });
      }
    }

    fetchCalendar();
    fetchTeacherLessons();
    excludeTeacherTimes();
  }, [teacherId]);

  const handleScheduleLesson = async () => {
    if (!selectedDate || !subject) return;

    try {
      const dateTime = format(selectedDate, 'dd/MM/yyyy HH:mm');
      await lessonsAPI.scheduleLesson({
        teacher_id: teacherId,
        subject,
        date: dateTime
      });
      
      setAlert({
        type: 'success',
        title: 'Success', 
        description: 'Your lesson has been scheduled successfully!',
        icon: <CheckCircle className="h-5 w-5 text-green-500" />
      });
    } catch (error) {
      console.error('Failed to schedule the lesson:', error);
      setAlert({
        type: 'error',
        title: 'Error',
        description: 'Failed to schedule the lesson. Please try again later.',
        icon: <AlertCircle className="h-5 w-5 text-red-500" />
      });
    }
  };

  const excludeTeacherTimes = () => {
    if (!calendar) return [];
    
    const allSlots = [];
    let current = setHours(setMinutes(new Date(), 0), 0);
    const EOD = setHours(setMinutes(new Date(), 59), 23);

    // Generowanie wszystkich slotow godzinnych w ciagu doby
    while (current <= EOD) {
      allSlots.push(current);
      current.setHours(current.getHours() + 1);
    }

    // Sloty poza godzinami dostepnosci
    const unavailableSlots = [];
    const start = util.getDateFromTimeString(calendar.available_from);
    const end = util.getDateFromTimeString(calendar.available_until);
    
    allSlots.forEach(slot => {
      if (slot < start || slot >= end) {
        unavailableSlots.push(slot);
      }
    });
    
    setExcludedTimes(unavailableSlots);
  };

  const subtractOneHour = (date) => {
    return date.setHours(date.getHours() - 1);
  }

  const isAllowedDay = (date) => {
    const day = getDay(date) - 0; // Pobiera dzień tygodnia
    const allowedDays = calendar.working_days.replace(/{|}/g, '').split(',').map(Number).map(num => num % 7);
    return allowedDays.includes(day);
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
              {/* Jeśli alert istnieje, wyświetl go */}
              {alert && (
                <Alert variant="destructive" className="mb-4 flex items-start space-x-3">
                  {alert.icon}
                  <AlertTitle>{alert.title}</AlertTitle>
                  <AlertDescription>{alert.description}</AlertDescription>
                </Alert>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {calendar && 
                <div>
                  <label className="block text-sm font-medium mb-2 text-gray-700">Select Date</label>
                  <div className="relative">
                    <Space /><Space /><Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <DatePicker
                      locale={pl}
                      dateFormat="Pp"
                      selected={selectedDate}
                      onChange={e => setSelectedDate(e)}
                      filterDate={isAllowedDay}
                      showTimeSelect
                      timeIntervals={60}
                      minTime={subtractOneHour(util.getDateFromTimeString(calendar.available_from))}
                      maxTime={subtractOneHour(util.getDateFromTimeString(calendar.available_until))}
                      className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg"
                    />
                  </div>
                </div>
                }

                <div className="md:col-span-2">
                  <label className="block text-sm font-medium mb-2 text-gray-700">Subject</label>
                  <div className="relative">
                    <BookOpen className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <select
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white"
                    >
                      <option value="" disabled>
                        Select a subject...
                      </option>
                      {}
                    </select>
                  </div>
                </div>

                <div className="flex justify-end space-x-3">
                  <button
                    onClick={handleScheduleLesson}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                    disabled={!selectedDate || !subject}
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