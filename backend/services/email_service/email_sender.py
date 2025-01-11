# Email sender that sends emails based on the given parameters

import os
from email.message import EmailMessage
import ssl
import smtplib
from config import EMAIL_SENDER, EMAIL_PASSWORD, SMTP_PORT, SMTP_SERVER
import mimetypes
import requests

def fetch_pdf_from_url(pdf_url: str) -> bytes:
    response = requests.get(pdf_url)
    if response.status_code == 200:
        return response.content
    else:
        raise ValueError(f"Failed to fetch PDF from {pdf_url}, status code {response.status_code}")


class EmailSender:
    def __init__(self):
        pass

    def send_email(self, email_receiver: str, subject: str, body: str, pdf_path: str) -> None:
        em = EmailMessage()
        em['From'] = EMAIL_SENDER
        em['To'] = email_receiver
        em['Subject'] = subject
        em.set_content(body)

        if pdf_path:
            try:
                if pdf_path.startswith("http://") or pdf_path.startswith("https://"):
                    print(f"Fetching PDF from URL: {pdf_path}")
                    pdf_data = fetch_pdf_from_url(pdf_path)
                    mime_type = "application/pdf"
                    main_type, sub_type = mime_type.split('/')
                    em.add_attachment(pdf_data, maintype=main_type, subtype=sub_type, filename=os.path.basename(pdf_path))
            except FileNotFoundError:
                print(f"File {pdf_path} not found. The message will be sent without the attachement.") 

        context = ssl.create_default_context()

        with smtplib.SMTP_SSL(SMTP_SERVER, SMTP_PORT, context=context) as smtp:
            smtp.login(EMAIL_SENDER, EMAIL_PASSWORD)
            smtp.sendmail(EMAIL_SENDER, email_receiver, em.as_string())


