from flask import Blueprint, jsonify, request, redirect, send_from_directory
from flask_jwt_extended import jwt_required, get_jwt_identity
from flasgger import swag_from
from sqlalchemy import desc, and_, asc, cast, text, func, Index
from sqlalchemy.dialects import postgresql
from sqlalchemy.dialects.postgresql import REGCONFIG
import sys
import requests


from models import Teacher, Student, Review, Lesson, Calendar, Invoice, db
import os

SWAGGER_TEMPLATE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../swagger_templates'))

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from pdf_generator.lesson_invoice import LessonInvoice
from pdf_generator.pdf_generator import PDFInvoiceGenerator

api = Blueprint('api', __name__)


def get_user_by_jwt():
    user_id = get_jwt_identity()
    user = Teacher.query.get(user_id)

    if not user or user.role == 'student':
        user = Student.query.get(user_id)

    return user


@api.errorhandler(404)
def page_not_found(e):
    return jsonify({"error": "Page not found"}), 404


### Teacher list ###

@api.route('/teacher-list', methods=['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_teacher_list.yml'))
@jwt_required()
def get_teacher_list():
    user = get_user_by_jwt()
    subject = request.args.get('subject')
    difficulty = request.args.get('difficulty')

    if not user:
        return jsonify({'message': 'User not found'}), 400

    if subject and difficulty:
        teachers = Teacher.query.filter(Teacher.subjects.match(subject), Teacher.difficulty_levels.match(difficulty)).all()
    elif subject:
        teachers = Teacher.query.filter(Teacher.subjects.match(subject)).all()
    elif difficulty:
        teachers = Teacher.query.filter(Teacher.difficulty_levels.match(difficulty)).all()
    else:
        teachers = Teacher.query.all()

    if teachers:
        teacher_id_list = [teacher.id for teacher in teachers]
        print(teacher_id_list)
        return jsonify({'teacher_id': teacher_id_list}), 200
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
# @swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_lesson.yml'))
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

    if subject not in Teacher.query.filter_by(id=teacher_id).first().subjects:
        return jsonify({'message': 'Teacher does not teach this subject'}), 400

    # TODO Check teacher's calender

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
# @swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_lesson.yml'))
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

BASE_DIR = os.path.join(os.path.dirname(__file__), "../static_invoices")
BASE_DIR = os.path.abspath(BASE_DIR) 

@api.route('/invoice', methods=['POST'])
# @swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_invoice.yml'))
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

    new_invoice = Invoice(lesson_id=lesson_id, price = lesson.price)

    db.session.add(new_invoice)
    db.session.commit()

    return jsonify({'message': 'Invoice created'}), 201

# Generated PDF path

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../static_invoices"))
@api.route('/generate-and-send-invoice/<int:invoice_id>', methods=['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'generate_and_send_invoice.yml'))
def generate_and_send_invoice(invoice_id):
    try:
        # Pobranie danych z bazy danych
        invoice = Invoice.query.filter_by(id=invoice_id).first()
        if not invoice:
            return jsonify({"error": "Invoice not found"}), 404
        
        lesson = Lesson.query.filter_by(id=invoice.lesson_id).first()
        if not lesson:
            return jsonify({"error": "Lesson not found"}), 404

        student = Student.query.filter_by(id=lesson.student_id).first()
        if not student:
            return jsonify({"error": "Student not found"}), 404

        teacher = Teacher.query.filter_by(id=lesson.teacher_id).first()
        if not teacher:
            return jsonify({"error": "Teacher not found"}), 404

        # Tworzenie obiektu LessonInvoice
        lesson_invoice = LessonInvoice(
            invoice_id=invoice.id,
            lesson_id=lesson.id,
            student_name=student.name,
            student_email=student.email,
            teacher_name=teacher.name,
            teacher_email=teacher.email,
            subject=lesson.subject,
            lesson_date=lesson.date,
            price=invoice.price,
            vat_rate=invoice.vat_rate,
            issue_date=invoice.created_at
        )

        # Generowanie PDF
        generator = PDFInvoiceGenerator()
        generator.create_invoice(lesson_invoice)  # Zapis do domyślnej lokalizacji

        # Ścieżka PDF generowana przez `PDFInvoiceGenerator`
        pdf_filename = f"invoice_{invoice_id}.pdf"
        pdf_url = f"http://host.docker.internal:5000/api/generated-invoice-pdf/{pdf_filename}"

        # Sprawdzamy, czy plik istnieje

        # Wysyłanie e-maila przez mikroserwis
        email_service_url = "http://127.0.0.1:5001/send-email"
        email_payload = {
            "email_receiver": "jaiwiecejmnie@gmail.com",
            "subject": f"Invoice #{invoice_id}",
            "body": (
                f"Dear {student.name},\n\n"
                "Attached is the invoice for your recent lesson.\n\n"
                "Best regards,\n"
                "Your Teaching Service Team"
            ),
            "pdf_path": pdf_url
        }

        response = requests.post(email_service_url, json=email_payload)
        
        # Obsługa odpowiedzi z mikroserwisu
        if response.status_code == 200:
            return jsonify({
                "message": f"Invoice PDF generated and sent to {student.email}.",
                "invoice_data": lesson_invoice.to_dict()
            }), 200
        else:
            return jsonify({
                "error": f"Failed to send email: {response.json().get('error', 'Unknown error')}"
            }), 500

    except Exception as e:
        return jsonify({"error": str(e)}), 500
