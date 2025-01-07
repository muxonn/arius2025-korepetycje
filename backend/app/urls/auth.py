from flask import Blueprint, jsonify, request
from models import db, Student, Teacher  # Importuj odpowiednie modele
from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import JWTManager, create_access_token
from flasgger import swag_from

auth = Blueprint('auth', __name__)

# Registration Endpoint
@auth.route('/register', methods=['POST'])
@swag_from('../swagger_templates/register.yml')
def register():
    data = request.get_json()

    name = data.get('name')
    email = data.get('email')
    password = data.get('password')
    role = data.get('role')  # student or teacher

    if not name or not email or not password or not role:
        return jsonify({"message": "Name, email, password, and role are required."}), 400

    if role not in ['student', 'teacher']:
        return jsonify({"message": "Role must be either 'student' or 'teacher'."}), 400

    # Check if the email already exists
    existing_user = Student.query.filter_by(email=email).first() or Teacher.query.filter_by(email=email).first()
    if existing_user:
        return jsonify({"message": "Email already in use."}), 400

    # Create a new user based on the role
    if role == 'student':
        new_user = Student(name=name, email=email)
    elif role == 'teacher':
        new_user = Teacher(name=name, email=email)

    new_user.set_password(password)

    # Save the new user to the database
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": f"{role.capitalize()} registered successfully."}), 201


# Login Endpoint
@auth.route('/login', methods=['POST'])
@swag_from('../swagger_templates/login.yml')
def login():
    data = request.get_json()

    email = data.get('email')
    password = data.get('password')
    role = data.get('role')

    if not email or not password or not role:
        return jsonify({"message": "Email, password and role are required."}), 400

    # Find the user by email in both Student and Teacher tables
    user = None
    if role == 'student':
        user = Student.query.filter_by(email=email).first()
    elif role == "teacher":
       user = Teacher.query.filter_by(email=email).first()

    if user and user.check_password(password):
        # Generate JWT token
        access_token = user.generate_jwt()
        return jsonify({
            "message": "Login successful.",
            "access_token": access_token
        }), 200
    else:
        return jsonify({"message": "Invalid email or password."}), 401
