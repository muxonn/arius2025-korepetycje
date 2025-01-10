from flask import Blueprint, jsonify, request, redirect
from flask_jwt_extended import jwt_required, get_jwt_identity
from flasgger import swag_from
from datetime import datetime, timedelta
from sqlalchemy import desc, and_, asc, cast, text, func, Index
from sqlalchemy.dialects import postgresql
from sqlalchemy.dialects.postgresql import REGCONFIG

import os

SWAGGER_TEMPLATE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../swagger_templates'))

from models import Teacher, Student, Review, Lesson, Calendar, Invoice, LessonReport, Calendar, db

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


### Teacher list ###

@api.route('/teacher-list', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_teacher.yml'))
@jwt_required()
def get_teacher_list():
    user = get_user_by_jwt()
    subject = request.args.get('subject')
    difficulty = request.args.get('difficulty')

    if not user:
        return jsonify({'message': 'User not found'}), 400

    if subject and difficulty:
        teachers = Teacher.query.filter(Teacher.subjects.match(subject),
                                        Teacher.difficulty_levels.match(difficulty)).all()
    elif subject:
        teachers = Teacher.query.filter(Teacher.subjects.match(subject)).all()
    elif difficulty:
        teachers = Teacher.query.filter(Teacher.difficulty_levels.match(difficulty)).all()
    else:
        teachers = Teacher.query.all()

    if teachers:
        teacher_list = [teacher.to_dict() for teacher in teachers]

        return jsonify({'teacher_list': teacher_list}), 200
    else:
        return jsonify({'message': 'Teachers not found'}), 404


### End of teacher list ###

### Reviews ###

@api.route('/teacher-reviews', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_reviews.yml'))
@jwt_required()
def get_reviews():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400

    reviews = Review.query.all()
    reviews_list = [r.to_dict() for r in reviews]

    return jsonify(reviews=reviews_list), 200


@api.route('/teacher-reviews/<int:teacher_id>', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_review.yml'))
@jwt_required()
def add_review(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400

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
        return jsonify({'message': 'User not found'}), 400

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
        return jsonify({'message': 'User not found'}), 400

    if user.role != 'student':
        return jsonify({'message': 'User can not be a teacher'}), 400

    data = request.get_json()
    teacher_id = data.get('teacher_id')
    subject = data.get('subject')
    date = data.get('date')

    if not subject:
        return jsonify({'message': 'Subject must be provided'}), 400

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

    if date < datetime.utcnow():
        return jsonify({'message': 'Lesson time must be in the future'}), 400

    if date.isoweekday() not in set(map(int, calendar.working_days[1:-1].split(','))):
        return jsonify({'message': 'Teacher does not work on this weekday'}), 400

    if not (calendar.available_from < date.time() <= (calendar.available_until-timedelta(hours=1))):
        return jsonify({'message': 'Teacher does not work in this hours'}), 400

    if subject not in Teacher.query.filter_by(id=teacher_id).first().subjects:
        return jsonify({'message': 'Teacher does not teach this subject'}), 400

    if Lesson.query.filter_by(teacher_id=teacher_id, date=date).first():
        return jsonify({'message': 'Lesson with this teacher is already booked for this date'}), 400

    if Lesson.query.filter_by(student_id=user.id, date=date).first():
        return jsonify({'message': 'User has already booked lesson for this date'}), 400

    new_lesson = Lesson(teacher_id=teacher_id, student_id=user.id, date=date, subject=subject, status="scheduled",
                        price=0)

    db.session.add(new_lesson)
    db.session.commit()

    return jsonify({'message': 'Lesson created'}), 201


@api.route('/lesson', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_lesson.yml'))
@jwt_required()
def get_lesson():
    user = get_user_by_jwt()
    lessons = None

    if not user:
        return jsonify({'message': 'User not found'}), 400

    if user.role == 'student':
        lessons = Lesson.query.filter_by(student_id=user.id).all()
    elif user.role == 'teacher':
        lessons = Lesson.query.filter_by(teacher_id=user.id).all()

    if not lessons:
        return jsonify({'message': 'No lessons found'}), 400

    lesson_list = [lesson.to_dict() for lesson in lessons]

    return jsonify(lesson_list=lesson_list), 200


### End of lessons ###


### Invoices ###

@api.route('/invoice', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_invoice.yml'))
@jwt_required()
def add_invoice():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400

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

    # TODO send email

    new_invoice = Invoice(lesson_id=lesson_id)

    db.session.add(new_invoice)
    db.session.commit()

    return jsonify({'message': 'Invoice created'}), 201


### End of invoices ###


### Reports ###

@api.route('/report', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_report.yml'))
@jwt_required()
def add_report():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400

    if user.role != 'teacher':
        return jsonify({'message': 'User can not be a student'}), 400

    data = request.get_json()
    lesson_id = data.get('lesson_id')
    comment = data.get('comment')
    homework = data.get('homework')
    progress_rating = data.get('progress_rating')

    if not lesson_id:
        return jsonify({'message': 'Lesson id not provided'}), 400

    try:
        lesson_id = int(lesson_id)
    except ValueError:
        return jsonify({'message': 'Lesson id must be an integer'}), 400

    lesson = Lesson.query.filter_by(id=lesson_id).first()

    if not lesson:
        return jsonify({'message': 'Lesson not found'}), 404

    if user.id != lesson.teacher_id:
        return jsonify({'message': 'Teacher does not belong to this lesson and cannot create report'}), 400

    if lesson.date + timedelta(hours=1) > datetime.now():
        return jsonify({'message': 'Report cannot be created before the end of the lesson'}), 400

    if not progress_rating:
        return jsonify({'message': 'Rating must be provided'}), 400

    try:
        progress_rating = int(progress_rating)
    except ValueError:
        return jsonify({'message': 'Progress rating must be an integer'}), 400

    if progress_rating < 0 or progress_rating > 5:
        return jsonify({'message': 'Rating must be between values 0 and 5'}), 400

    if not comment:
        comment = ''

    if not homework:
        homework = ''

    new_report = LessonReport(
        lesson_id=lesson_id,
        comment=comment,
        progress_rating=progress_rating,
        homework=homework,
        student_id=lesson.student_id
    )

    db.session.add(new_report)
    db.session.commit()

    return jsonify({'message': 'Report created'}), 201


@api.route('/report', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_report.yml'))
@jwt_required()
def get_report():
    user = get_user_by_jwt()
    reports = None

    if not user:
        return jsonify({'message': 'User not found'}), 400

    if user.role == 'student':
        reports = LessonReport.query.filter_by(student_id=user.id).all()
    elif user.role == 'teacher':
        reports = LessonReport.query.filter_by(teacher_id=user.id).all()

    if not reports:
        return jsonify({'message': 'No reports found'}), 400

    report_list = []
    for report in reports:
        report_list.append(
            {"student_name": Student.query.filter_by(id=report.student_id).first().name,
             "teacher_name": Teacher.query.filter_by(id=report.teacher_id).first().name,
             "subject": Lesson.query.filter_by(id=report.lesson_id).first().subject,
             "date": Lesson.query.filter_by(id=report.lesson_id).first().date.strftime("%d/%m/%Y %H:%M"),
             "homework": report.homework,
             "progress_rating": report.progress_rating,
             "comment": report.comment,
             }
        )

    return jsonify({'report_list': report_list}), 200


### End of reports ###


### Calendars ###


@api.route('/calendar', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_calendar.yml'))
@jwt_required()
def add_calendar():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400

    if user.role != 'teacher':
        return jsonify({'message': 'User can not be a student'}), 400

    data = request.get_json()

    available_from = data.get('available_from')
    available_until = data.get('available_until')
    working_days = data.get('working_days')

    if not available_from or not available_until or not working_days:
        return jsonify({'message': 'Available dates or hours not provided'}), 400

    try:
        available_from = datetime.strptime(available_from, '%H:%M').time()
        available_until = datetime.strptime(available_until, '%H:%M').time()
    except ValueError:
        return jsonify({'message': 'Available hours are not in %H:%M format'}), 400

    try:
        working_days = list(map(int, working_days))
    except ValueError:
        return jsonify(
            {'message': 'Wrong type of working days, expected integers between 1 (Monday) and 7 (Sunday)'}), 400

    if not all(0 < d < 8 for d in working_days):
        return jsonify(
            {'message': 'Wrong value of working days, expected integers between 1 (Monday) and 7 (Sunday)'}), 400

    new_calendar = Calendar(
        teacher_id=user.id,
        available_from=available_from,
        available_until=available_until,
        working_days=working_days
    )

    db.session.add(new_calendar)
    db.session.commit()

    return jsonify({'message': 'Calendar created'}), 201


@api.route('/calendar/<int:teacher_id>', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_calendar.yml'))
@jwt_required()
def get_calendar(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400

    calendar = Calendar.query.filter_by(teacher_id=teacher_id).first()

    if not calendar:
        return jsonify({'message': 'Calendar not found'}), 404

    return jsonify(calendar.to_dict()), 200


### End of calendars ###
