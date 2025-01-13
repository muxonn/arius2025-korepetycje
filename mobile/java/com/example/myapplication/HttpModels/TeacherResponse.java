package com.example.myapplication.HttpModels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TeacherResponse extends HttpResponse {
    List<Teacher> teacherList;
    public TeacherResponse(int statusCode, String message, List<Teacher> teacherList){
        super(statusCode,message);
        this.teacherList = teacherList;
    }
    public TeacherResponse(){
        super();
    }

    public List<Teacher> getTeacherList() {
        return teacherList;
    }

    public void setTeacherList(List<Teacher> teacherList) {
        this.teacherList = teacherList;
    }
    public static TeacherResponse fromJson(int statusCode, JSONObject jsonObject) throws JSONException {
        HttpResponse baseResponse = HttpResponse.fromJson(statusCode, jsonObject);
        JSONArray teacherArray = jsonObject.getJSONArray("teacher_list");
        List<Teacher> teacherList = new ArrayList<>();
        for (int i = 0; i < teacherArray.length(); i++) {
            JSONObject teacherJson = teacherArray.getJSONObject(i);
            teacherList.add(Teacher.fromJson(teacherJson));
        }
        return new TeacherResponse(baseResponse.getStatusCode(), baseResponse.getMessage(), teacherList);
    }
}
