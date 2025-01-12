def test_get_subjects(test_client, login_student):
    headers = {'Authorization': f'Bearer {login_student}'}
    response = test_client.get('/api/subjects', headers=headers)
    assert response.status_code == 200
    assert 'subjects' in response.json


def test_get_teacher_list(test_client, login_student):
    headers = {'Authorization': f'Bearer {login_student}'}
    response = test_client.get('/api/teacher-list', headers=headers)
    assert response.status_code == 200
    assert 'teacher_list' in response.json
