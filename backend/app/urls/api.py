from flask import Blueprint, jsonify, request, redirect
from flask_jwt_extended import jwt_required, get_jwt_identity
from flasgger import swag_from
from sqlalchemy import desc, and_, asc
import os

SWAGGER_TEMPLATE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../swagger_templates'))


from models import Teacher, Student, Review, Lesson, db

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

### Reviews ###

@api.route('/teacher-reviews', methods = ['GET'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'get_reviews.yml'))
@jwt_required()
def get_reviews():
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400
    
    reviews = Review.query.all()
    reviews_list = [r.to_dict() for r in reviews]

    return jsonify(reviews = reviews_list), 200

@api.route('/teacher-reviews/<int:teacher_id>', methods = ['POST'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'add_review.yml'))
@jwt_required()
def add_review(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400
    
    if isinstance(user, Student):
        return jsonify({'message': 'User can not be a teacher'}), 400
    
    lessons = Lesson.query.filter_by(teacher_id = teacher_id, student_id = user.id).first()

    if not lessons:
        return jsonify({'message': 'Student had no lessons with the teacher'}), 400
    
    data = request.get_json()

    rating = data.get('rating')
    comment = data.get('comment')
    
    if not rating:
        return jsonify({'message': 'Rating must be provided'}), 400
    
    if rating < 0 or rating > 5:
        return jsonify({'message': 'Rating must be between values 0 and 5'}), 400
    
    last_review = Review.query.filter_by(teacher_id = teacher_id, student_id = user.id)

    if last_review:
        return jsonify({'message': 'Rating for this teacher is already created'}), 400
    
    new_review = Review(teacher_id=teacher_id, student_id=user.id, rating = rating, comment = comment)

    db.session.add(new_review)
    db.session.commit()

    if not comment:
        comment = ""

    return jsonify({"rating": rating, "comment": comment}), 200


@api.route('/teacher-reviews/<int:teacher_id>', methods = ['DELETE'])
@swag_from(os.path.join(SWAGGER_TEMPLATE_DIR, 'delete_review.yml'))
@jwt_required()
def delete_review(teacher_id):
    user = get_user_by_jwt()

    if not user:
        return jsonify({'message': 'User not found'}), 400
    
    if isinstance(user, Student):
        return jsonify({'message': 'User can not be a teacher'}), 400
    
    review = Review.query.filter_by(teacher_id = teacher_id, student_id = user.id).first()

    if not review:
        return jsonify({"message": f"Review does not exist"}), 404
    
    db.session.delete(review)
    db.session.commit()

    return jsonify({"message": "Review deleted successfuly"}), 200

### The end of reviews ###