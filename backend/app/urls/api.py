from flask import Blueprint, jsonify, request, redirect
from flask_jwt_extended import jwt_required, get_jwt_identity
from flasgger import swag_from
from datetime import datetime, timedelta
from sqlalchemy import desc, and_, asc, cast, text, func, Index
from sqlalchemy.dialects import postgresql
from sqlalchemy.dialects.postgresql import REGCONFIG
import sys
import requests


from models import Teacher, Student, Review, Lesson, Calendar, Invoice, db, Subject, DifficultyLevel
import os

SWAGGER_TEMPLATE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../swagger_templates'))

from models import Teacher, Student, Review, Lesson, Calendar, Invoice, db

api = Blueprint('api', __name__)


def get_user_by_jwt():
    user_id = get_jwt_identity()
    user = Teacher.query.get(user_id)

    if not user:
        user = Student.query.get(user_id)

    return user


@api.errorhandler(404)
def page_not_found(e):
    return jsonify({"error": "Page not found"}), 404


@api.route('/subjects', methods=['GET'])
def get_subjects():
    subjects = Subject.query.all()
    return jsonify({'subjects': [s.to_dict() for s in subjects]}), 200


@api.route('/difficulty-levels', methods=['GET'])
def get_difficulty_levels():
    difficulty_levels = DifficultyLevel.query.all()
    return jsonify({'difficulty_levels': [d.to_dict() for d in difficulty_levels]}), 200


### Teacher list ###

@api.route('/teacher-list', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_teacher.yml'))
@jwt_required()
def get_teacher_list():
    user = get_user_by_jwt()
    subject = request.args.get('subject')
    difficulty_id = request.args.get('difficulty_id')

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if subject and difficulty_id:
        teachers = Teacher.query.filter(Teacher.subjects.match(subject),
                                        Teacher.difficulty_levels.match(difficulty_id)).all()
    elif subject:
        teachers = Teacher.query.filter(Teacher.subjects.match(subject)).all()
    elif difficulty_id:
        teachers = Teacher.query.filter(Teacher.difficulty_levels.match(difficulty_id)).all()
    else:
        teachers = Teacher.query.all()

    if teachers:
        teacher_list = [teacher.to_dict() for teacher in teachers]

        return jsonify({'teacher_list': teacher_list}), 200
    else:
        return jsonify({'message': 'Teachers not found'}), 404


### End of teacher list ###

@api.route('/teacher-update', methods=['PUT'])
# @swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'update_teacher.yml'))
@jwt_required()
def update_teacher():
    user = get_user_by_jwt()

    data = request.get_json()
    subject_ids = data.get('subject_ids')
    difficulty_level_ids = data.get('difficulty_ids')
    hourly_rate = data.get('hourly_rate')

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if user.role != 'teacher':
        return jsonify({'message': 'User can not be a student'}), 400

    try:
        if subject_ids:
            if all(Subject.query.get(int(s)) for s in subject_ids):
                db.session.query(Teacher).filter_by(id=user.id).update({'subject_ids': subject_ids})
            else:
                return jsonify({'message': 'Subject not found'}), 404
    except ValueError:
        return jsonify({'message': 'Subject ids can only contain numbers'}), 400

    try:
        if difficulty_level_ids:
            if all(DifficultyLevel.query.get(int(s)) for s in difficulty_level_ids):
                db.session.query(Teacher).filter_by(id=user.id).update({'difficulty_level_ids': difficulty_level_ids})
            else:
                return jsonify({'message': 'Difficulty level not found'}), 404
    except ValueError:
        return jsonify({'message': 'Difficulty levels ids can only contain numbers'}), 400

    try:
        if hourly_rate:
            hourly_rate = int(hourly_rate)
            db.session.query(Teacher).filter_by(id=user.id).update({'hourly_rate': hourly_rate})
    except ValueError:
        return jsonify({'message': 'Hourly rate can only be an integer'}), 400

    db.session.commit()
    return jsonify({'message': 'Teacher details updated'}), 200




### Reviews ###

@api.route('/teacher-reviews', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_reviews.yml'))
@jwt_required()
def get_reviews():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    reviews = Review.query.all()
    reviews_list = [r.to_dict() for r in reviews]

    return jsonify(reviews=reviews_list), 200


@api.route('/teacher-reviews/<int:teacher_id>', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_reviews_by_id.yml'))
@jwt_required()
def get_reviews_by_id(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    try:
        teacher_id = int(teacher_id)
    except ValueError:
        return jsonify({'message': 'Teacher id must be an integer'}), 400

    teacher = Teacher.query.get(teacher_id)

    if not teacher:
        return jsonify({'message': 'Teacher not found'}), 404

    reviews = Review.query.filter_by(teacher_id=teacher_id).all()
    reviews_list = [r.to_dict() for r in reviews]

    return jsonify(reviews=reviews_list), 200


@api.route('/teacher-reviews/<int:teacher_id>', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_review.yml'))
@jwt_required()
def add_review(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if user.role != 'student':
        return jsonify({'message': 'User can not be a teacher'}), 400

    lessons = Lesson.query.filter_by(teacher_id=teacher_id, student_id=user.id).first()

    if not lessons:
        return jsonify({'message': 'Student had no lessons with the teacher'}), 400

    data = request.get_json()

    rating = data.get('rating')
    comment = data.get('comment')

    if not rating:
        return jsonify({'message': 'Rating must be provided'}), 400

    if rating < 0 or rating > 5:
        return jsonify({'message': 'Rating must be between values 0 and 5'}), 400

    last_review = Review.query.filter_by(teacher_id=teacher_id, student_id=user.id)

    if last_review:
        return jsonify({'message': 'Rating for this teacher is already created'}), 400

    new_review = Review(teacher_id=teacher_id, student_id=user.id, rating=rating, comment=comment)

    db.session.add(new_review)
    db.session.commit()

    if not comment:
        comment = ""

    return jsonify({"rating": rating, "comment": comment}), 200


@api.route('/teacher-reviews/<int:teacher_id>', methods=['DELETE'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'delete_review.yml'))
@jwt_required()
def delete_review(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if user.role != 'student':
        return jsonify({'message': 'User can not be a teacher'}), 400

    review = Review.query.filter_by(teacher_id=teacher_id, student_id=user.id).first()

    if not review:
        return jsonify({"message": f"Review does not exist"}), 404

    db.session.delete(review)
    db.session.commit()

    return jsonify({"message": "Review deleted successfuly"}), 200


### The end of reviews ###


### Lessons ###
@api.route('/lesson', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_lesson.yml'))
@jwt_required()
def add_lesson():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if user.role != 'student':
        return jsonify({'message': 'User can not be a teacher'}), 400

    data = request.get_json()
    teacher_id = data.get('teacher_id')
    subject_id = data.get('subject_id')
    difficulty_level_id = data.get('difficulty_id')
    date = data.get('date')

    if not subject_id:
        return jsonify({'message': 'Subject must be provided'}), 400

    try:
        subject_id = int(subject_id)
    except ValueError:
        return jsonify({'message': 'Subject id must be an integer'}), 400

    subject = Subject.query.get(subject_id)

    if not subject:
        return jsonify({'message': 'Subject not found'}), 404

    if not difficulty_level_id:
        return jsonify({'message': 'Difficulty level must be provided'}), 400

    try:
        difficulty_level_id = int(difficulty_level_id)
    except ValueError:
        return jsonify({'message': 'Difficulty level id must be an integer'}), 400

    difficulty_level = Subject.query.get(difficulty_level_id)

    if not difficulty_level:
        return jsonify({'message': 'Difficulty level not found'}), 404

    if not date:
        return jsonify({'message': 'Date must be provided'}), 400

    try:
        date = datetime.strptime(date, "%d/%m/%Y %H:%M")
    except ValueError:
        return jsonify({'message': 'Date must be in format %d/%m/%Y %H:%M'}), 400

    try:
        teacher_id = int(teacher_id)
    except ValueError:
        return jsonify({'message': 'Teacher id must be an integer'}), 400

    teacher = Teacher.query.filter_by(id=teacher_id).first()

    if not teacher:
        return jsonify({'message': 'Teacher not found'}), 404

    calendar = Calendar.query.filter_by(teacher_id=teacher_id).first()

    if not calendar:
        return jsonify({'message': 'Teacher does not hav a calendar set'}), 400

    if date < datetime.utcnow():
        return jsonify({'message': 'Lesson time must be in the future'}), 400

    if date.isoweekday() not in set(map(int, calendar.working_days[1:-1].split(','))):
        return jsonify({'message': 'Teacher does not work on this weekday'}), 400

    if not (calendar.available_from < date.time() and (date + timedelta(hours=1)).time() <= calendar.available_until):
        return jsonify({'message': 'Teacher does not work in this hours'}), 400

    if subject_id not in set(map(int, teacher.subject_ids[1:-1].split(','))):
        return jsonify({'message': 'Teacher does not teach this subject'}), 400

    if difficulty_level_id not in set(map(int, teacher.difficulty_level_ids[1:-1].split(','))):
        return jsonify({'message': 'Teacher does not teach on this difficulty level'}), 400

    if Lesson.query.filter_by(teacher_id=teacher_id, date=date).first():
        return jsonify({'message': 'Lesson with this teacher is already booked for this date'}), 400

    if Lesson.query.filter_by(student_id=user.id, date=date).first():
        return jsonify({'message': 'User has already booked lesson for this date'}), 400

    new_lesson = Lesson(teacher_id=teacher_id,
                        student_id=user.id,
                        date=date,
                        subject_id=subject_id,
                        difficulty_level_id=difficulty_level_id,
                        status="scheduled",
                        price=teacher.hourly_rate
                        )

    db.session.add(new_lesson)
    db.session.commit()

    return jsonify({'message': 'Lesson created'}), 201


@api.route('/lesson', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_lesson.yml'))
@jwt_required()
def get_lesson():
    user = get_user_by_jwt()
    status = request.args.get('status')
    lessons = None

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if status:
        if status in ['scheduled', 'completed']:
            if user.role == 'student':
                lessons = Lesson.query.filter_by(student_id=user.id, status=status).all()
            elif user.role == 'teacher':
                lessons = Lesson.query.filter_by(teacher_id=user.id, status=status).all()
        else:
            return jsonify({'message': 'Invalid status'}), 400
    else:
        if user.role == 'student':
            lessons = Lesson.query.filter_by(student_id=user.id).all()
        elif user.role == 'teacher':
            lessons = Lesson.query.filter_by(teacher_id=user.id).all()

    if not lessons:
        return jsonify({'message': 'No lessons found'}), 400

    lesson_list = [lesson.to_dict() for lesson in lessons]

    return jsonify(lesson_list=lesson_list), 200


@api.route('/lesson/<int:teacher_id>', methods=['GET'])
# @swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_lesson_by_id.yml'))
@jwt_required()
def get_lesson_by_id(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    try:
        teacher_id = int(teacher_id)
    except ValueError:
        return jsonify({'message': 'Teacher id must be an integer'}), 400

    teacher = Teacher.query.filter_by(id=teacher_id).first()

    if not teacher:
        return jsonify({'message': 'Teacher not found'}), 404

    lessons = Lesson.query.filter_by(teacher_id=teacher_id).all()

    if not lessons:
        return jsonify({'message': 'No lessons found'}), 400

    lesson_list = [lesson.to_dict() for lesson in lessons]

    return jsonify(lesson_list=lesson_list), 200


### End of lessons ###


### Invoices ###

BASE_DIR = os.path.join(os.path.dirname(__file__), "../static_invoices")
BASE_DIR = os.path.abspath(BASE_DIR) 

@api.route('/invoice', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_invoice.yml'))
@jwt_required()
def add_invoice():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 401

    if user.role != 'student':
        return jsonify({'message': 'User can not be a teacher'}), 400

    data = request.get_json()

    lesson_id = data.get('lesson_id')

    lesson = Lesson.query.filter_by(id=lesson_id).first()
    if not lesson:
        return jsonify({'message': 'Lesson not found'}), 400

    invoice = Invoice.query.filter_by(lesson_id=lesson_id).first()

    if invoice:
        return jsonify({'message': 'Lesson already invoiced'}), 400

    new_invoice = Invoice(lesson_id=lesson_id, price = lesson.price)

    db.session.add(new_invoice)
    db.session.commit()

    return jsonify({'message': 'Invoice created'}), 201

### End of invoices ###
