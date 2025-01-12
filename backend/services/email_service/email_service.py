# Microservice used to send automated emails. Runs on port 5001

from flask import Flask, request, jsonify
from email_sender import EmailSender


app = Flask(__name__)

email_sender = EmailSender()

@app.route('/send-email', methods=['POST'])
def send_email():
    data = request.json
    email_receiver = data.get('email_receiver')
    subject = data.get('subject')
    body = data.get('body')

    # Optional pdf path
    pdf_path = data.get('pdf_path')

    if not email_receiver or not subject or not body:
        return jsonify({"message": "email_receiver, subject and body are required."}), 400

    try:
        email_sender.send_email(email_receiver, subject, body, pdf_path)
        return jsonify({"message": f"Email sent successfully to {email_receiver}"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True,host='0.0.0.0', port=5001) 
