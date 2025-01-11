import random
from datetime import datetime, time

from faker import Faker
from models import Teacher, Student, Lesson, Review, Invoice, LessonReport, Calendar, db
from server import app

fake = Faker()


def populate_teachers(num):
    with app.app_context():
        for _ in range(num):
            teacher = Teacher(
                name=fake.name(),
                email=fake.email(),
                subjects=["Mathematics", "Physics"],
                difficulty_levels=["Beginner", "Intermediate"],
                role="teacher"
            )
            teacher.set_password("pass")
            db.session.add(teacher)
        db.session.commit()
        print(f"{num} nauczycieli dodano do bazy danych.")


def populate_students(num):
    with app.app_context():
        for _ in range(num):
            student = Student(
                name=fake.name(),
                email=fake.email(),
                role="student"
            )
            student.set_password("pass")
            db.session.add(student)
        db.session.commit()
        print(f"{num} studentów dodano do bazy danych.")


def populate_lessons(num):
    with app.app_context():
        teachers = Teacher.query.all()
        students = Student.query.all()

        if not teachers or not students:
            print("Brak nauczycieli lub studentów w bazie danych.")
            return

        for _ in range(num):
            teacher = random.choice(teachers)
            student = random.choice(students)
            date = fake.date_time_this_year()
            subject = random.choice(teacher.subjects.split(", "))

            lesson = Lesson(
                teacher_id=teacher.id,
                student_id=student.id,
                date=date,
                subject=subject,
                status="scheduled",
                price=random.randint(50, 150)
            )
            db.session.add(lesson)
        db.session.commit()
        print(f"{num} lekcji dodano do bazy danych.")


def populate_reviews(num):
    with app.app_context():
        lessons = Lesson.query.all()

        if not lessons:
            print("Brak lekcji w bazie danych.")
            return

        for lesson in lessons:
            if lesson.teacher_id and lesson.student_id:
                review = Review(
                    teacher_id=lesson.teacher_id,
                    student_id=lesson.student_id,
                    rating=random.randint(1, 5),
                    comment=fake.text()
                )
                db.session.add(review)
        db.session.commit()
        print(f"{num} recenzji dodano do bazy danych.")


def populate_invoices(num):
    with app.app_context():
        lessons = Lesson.query.filter_by(status="scheduled").all()

        if not lessons:
            print("Brak lekcji do wystawienia faktur.")
            return

        for _ in range(min(num, len(lessons))):
            lesson = random.choice(lessons)

            invoice = Invoice(
                lesson_id=lesson.id,
                email_sent=True
            )
            db.session.add(invoice)
        db.session.commit()
        print(f"{num} faktur dodano do bazy danych.")


def populate_reports(num):
    with app.app_context():
        lessons = Lesson.query.all()

        if not lessons:
            print("Brak lekcji w bazie danych.")
            return

        for lesson in lessons:
            report = LessonReport(
                lesson_id=lesson.id,
                teacher_id=lesson.teacher_id,
                homework=fake.word(),
                progress_rating=random.randint(1, 5),
                comment=fake.text(),
                student_id=lesson.student_id
            )
            db.session.add(report)
        db.session.commit()
        print(f"{num} raportów dodano do bazy danych.")


def populate_calendars(num):
    with app.app_context():
        teachers = Teacher.query.all()

        if not teachers:
            print("Brak nauczycieli w bazie danych.")
            return

        for teacher in teachers:
            available_from = time(random.randint(8, 10), 0, 0)  # Dostępne od 8:00-10:00
            available_until = time(random.randint(17, 20), 0, 0)  # Dostępne do 17:00-20:00
            working_days = random.sample(range(1, 8), random.randint(3, 5))  # 3-5 dni roboczych

            calendar = Calendar(
                teacher_id=teacher.id,
                available_from=available_from,
                available_until=available_until,
                working_days=working_days
            )
            db.session.add(calendar)
        db.session.commit()
        print(f"{num} kalendarzy dodano do bazy danych.")


if __name__ == "__main__":
    num_records = 10
    populate_teachers(num_records)
    populate_students(num_records)
    populate_lessons(num_records)
    populate_reviews(num_records)
    populate_invoices(num_records)
    populate_reports(num_records)
    populate_calendars(num_records)
