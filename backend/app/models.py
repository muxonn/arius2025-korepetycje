from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import JWTManager, create_access_token
from datetime import timedelta

db = SQLAlchemy()

# Base class for students and teachers
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

student_teacher = db.Table('students_teachers',
    db.Column('student_id', db.Integer, db.ForeignKey('students.id'), primary_key=True),
    db.Column('teacher_id', db.Integer, db.ForeignKey('teachers.id'), primary_key=True)
)

class Student(BaseUser):
    __tablename__ = 'students'
    # Relationships
    teachers = db.relationship('Teacher', secondary=student_teacher, back_populates='students')

class Teacher(BaseUser):
    __tablename__ = 'teachers'
    subjects = db.Column(db.String(255), nullable=True)  # Comma-separated subjects
    difficulty_levels = db.Column(db.String(255), nullable=True)  # Comma-separated levels (e.g., "primary,high")
    bio = db.Column(db.Text, nullable=True)

    # Relationships
    students = db.relationship('Student', secondary=student_teacher, back_populates='teachers')

class Lesson(db.Model):
    __tablename__ = 'lessons'
    id = db.Column(db.Integer, primary_key=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id', ondelete='CASCADE'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('students.id', ondelete='CASCADE'), nullable=False)
    subject = db.Column(db.String(100), nullable=False)
    date = db.Column(db.DateTime, nullable=False)
    status = db.Column(db.String(20), default='scheduled')  # 'scheduled', 'completed', 'cancelled'
    price = db.Column(db.Float, nullable=False)

class Calendar(db.Model):
    __tablename__ = 'calendars'
    id = db.Column(db.Integer, primary_key=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id', ondelete='CASCADE'), nullable=False)
    availability = db.Column(db.Text, nullable=False)  # JSON or serialized availability
    
class LessonReport(db.Model):
    __tablename__ = 'lesson_reports'
    id = db.Column(db.Integer, primary_key=True)
    lesson_id = db.Column(db.Integer, db.ForeignKey('lessons.id', ondelete='CASCADE'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('students.id', ondelete='CASCADE'), nullable=False)
    report_text = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)


class Review(db.Model):
    __tablename__ = 'reviews'
    id = db.Column(db.Integer, primary_key=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id', ondelete='CASCADE'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('students.id', ondelete='CASCADE'), nullable=False)
    rating = db.Column(db.Integer, nullable=False)  # 1-5
    comment = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'teacher_id': self.teacher_id,
            'student_id': self.student_id,
            'rating': self.rating,
            'comment': self.comment,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }
    

class Invoice(db.Model):
    __tablename__ = 'invoices'
    id = db.Column(db.Integer, primary_key=True)
    lesson_id = db.Column(db.Integer, db.ForeignKey('lessons.id', ondelete='CASCADE'), nullable=False)
    email_sent = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)