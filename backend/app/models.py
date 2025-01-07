from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.orm import relationship
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import JWTManager, create_access_token
from datetime import timedelta

db = SQLAlchemy()

# Klasa bazowa dla wspólnych pól
class BaseUser(db.Model):
    __abstract__ = True
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String, nullable=False)
    name = db.Column(db.String(100), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def set_password(self, password):
        #Hash the password when setting it
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        #Check if the provided password matches the stored hash
        return check_password_hash(self.password_hash, password)

    def generate_jwt(self):
        #Generate JWT token for the user that expires after 10 days
        return create_access_token(identity=str(self.id), expires_delta=timedelta(days=10))


# Tabela łącząca students z teachers
student_teacher = db.Table('students_teachers',
    db.Column('student_id', db.Integer, db.ForeignKey('students.id'), primary_key=True),
    db.Column('teacher_id', db.Integer, db.ForeignKey('teachers.id'), primary_key=True)
)

# 1. Model studenta
class Student(BaseUser):
    __tablename__ = 'students'
    id = db.Column(db.Integer, primary_key=True)
    # Relationships
    lessons = relationship('Lesson', back_populates='student', foreign_keys='Lesson.student_id')
    reports = relationship('LessonReport', back_populates='student', foreign_keys='LessonReport.student_id')
    teachers = relationship('Teacher', secondary=student_teacher, back_populates='students')

# 2. Model nauczyciela
class Teacher(BaseUser):
    __tablename__ = 'teachers'
    id = db.Column(db.Integer, primary_key=True)
    subjects = db.Column(db.String(255), nullable=True)  # Comma-separated subjects
    difficulty_levels = db.Column(db.String(255), nullable=True)  # Comma-separated levels (e.g., "primary,high")
    bio = db.Column(db.Text, nullable=True)

    # Relationships
    lessons = relationship('Lesson', back_populates='teacher')
    calendar = relationship('Calendar', back_populates='teacher', uselist=False)
    students = relationship('Student', secondary=student_teacher, back_populates='teachers')
    reviews = relationship('Review', back_populates='teacher')

# 3. Model lekcji
class Lesson(db.Model):
    __tablename__ = 'lessons'
    id = db.Column(db.Integer, primary_key=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id', ondelete='CASCADE'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('students.id', ondelete='CASCADE'), nullable=False)
    subject = db.Column(db.String(100), nullable=False)
    date = db.Column(db.DateTime, nullable=False)
    status = db.Column(db.String(20), default='scheduled')  # 'scheduled', 'completed', 'cancelled'
    price = db.Column(db.Float, nullable=False)

    # Relationships
    teacher = relationship('Teacher', back_populates='lessons')
    student = relationship('Student', back_populates='lessons')
    report = relationship('LessonReport', back_populates='lesson', uselist=False)

# 4. Model kalendarza
class Calendar(db.Model):
    __tablename__ = 'calendars'
    id = db.Column(db.Integer, primary_key=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id', ondelete='CASCADE'), nullable=False)
    availability = db.Column(db.Text, nullable=False)  # JSON or serialized availability

    # Relationships
    teacher = relationship('Teacher', back_populates='calendar')

# 5. Model raportu z lekcji
class LessonReport(db.Model):
    __tablename__ = 'lesson_reports'
    id = db.Column(db.Integer, primary_key=True)
    lesson_id = db.Column(db.Integer, db.ForeignKey('lessons.id', ondelete='CASCADE'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('students.id', ondelete='CASCADE'), nullable=False)
    report_text = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relationships
    lesson = relationship('Lesson', back_populates='report')
    student = relationship('Student', back_populates='reports')

# 6. Model ocen
class Review(db.Model):
    __tablename__ = 'reviews'
    id = db.Column(db.Integer, primary_key=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id', ondelete='CASCADE'), nullable=False)
    rating = db.Column(db.Integer, nullable=False)  # 1-5
    comment = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relationships
    teacher = relationship('Teacher', back_populates='reviews')
    lesson = relationship('Lesson', backref='review', uselist=False)

# 7. Model faktur
class Invoice(db.Model):
    __tablename__ = 'invoices'
    id = db.Column(db.Integer, primary_key=True)
    lesson_id = db.Column(db.Integer, db.ForeignKey('lessons.id', ondelete='CASCADE'), nullable=False)
    email_sent = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relationships
    lesson = relationship('Lesson', backref='invoice', uselist=False)
