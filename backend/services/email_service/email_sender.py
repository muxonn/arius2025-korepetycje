# Email sender that sends emails based on the given parameters

import os
from email.message import EmailMessage
import ssl
import smtplib
from config import EMAIL_SENDER, EMAIL_PASSWORD, SMTP_PORT, SMTP_SERVER

class EmailSender:
    def __init__(self):
        pass

    def send_email(self, email_receiver: str, subject: str, body: str, pdf_path: str) -> None:
        em = EmailMessage()
        em['From'] = EMAIL_SENDER
        em['To'] = email_receiver
        em['Subject'] = subject
        em.set_content(body)

        context = ssl.create_default_context()

        with smtplib.SMTP_SSL(SMTP_SERVER, SMTP_PORT, context=context) as smtp:
            smtp.login(EMAIL_SENDER, EMAIL_PASSWORD)
            smtp.sendmail(EMAIL_SENDER, email_receiver, em.as_string())


