package com.example.myapplication.mappers;

import android.content.Context;

import com.example.myapplication.HttpModels.Difficulty;
import com.example.myapplication.HttpModels.Subject;
import com.example.myapplication.HttpModels.Teacher;
import com.example.myapplication.HttpQueries.TeacherHttpCommands;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DatabaseMapper {
    // Method to get the teacher's name by their ID
    public static String getTeacherNameById(Context context, int id, String authToken) throws ExecutionException, InterruptedException {
        List<Teacher> teachers = TeacherHttpCommands.getTeacherList(context, authToken, "", "").getTeacherList();
        return teachers.stream()
                .filter(s -> id == s.getId())
                .map(Teacher::getName)
                .findFirst()
                .orElse("Teacher not found");
    }

    // Method to get the name of a difficulty level by its ID
    public static String getDifficultyName(Context context, int id) {
        List<Difficulty> difficulties = getAllDifficulties(context);
        return difficulties.stream()
                .filter(s -> s.getId() == id)
                .map(Difficulty::getName)
                .findFirst()
                .orElse("Difficulty not found");
    }

    // Method to get the ID of a difficulty level by its name
    public static Integer getDifficultyId(Context context, String name) {
        List<Difficulty> difficulties = getAllDifficulties(context);
        return difficulties.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .map(Difficulty::getId)
                .orElse(0);
    }

    // Method to get the IDs of difficulty levels by their names
    public static List<Integer> getDifficultiesId(Context context, List<String> names) {
        List<Difficulty> difficulties = getAllDifficulties(context);
        if (names.isEmpty()) {
            return difficulties.stream()
                    .map(Difficulty::getId)
                    .collect(Collectors.toList());
        }
        return difficulties.stream()
                .filter(s -> names.contains(s.getName()))
                .map(Difficulty::getId)
                .collect(Collectors.toList());
    }

    // Method to get all difficulty levels
    public static List<Difficulty> getAllDifficulties(Context context) {
        List<Difficulty> difficulties;
        try {
            difficulties = TeacherHttpCommands.getAllDifficulties(context).getDifficulties();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return difficulties;
    }

    // Method to get the names of difficulty levels by their IDs
    public static List<String> getDifficultiesName(Context context, List<Integer> ids) {
        List<Difficulty> difficulties = getAllDifficulties(context);
        if (ids.isEmpty()) {
            return difficulties.stream()
                    .map(Difficulty::getName)
                    .collect(Collectors.toList());
        }
        return difficulties.stream()
                .filter(s -> ids.contains(s.getId()))
                .map(Difficulty::getName)
                .collect(Collectors.toList());
    }

    // Method to get the ID of a subject by its name
    public static Integer getSubjectId(Context context, String name) {
        List<Subject> subjects = getAllSubjects(context);
        return subjects.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .map(Subject::getId)
                .orElse(0);
    }

    // Method to get the name of a subject by its ID
    public static String getSubjectName(Context context, int id) {
        List<Subject> subjects = getAllSubjects(context);
        return subjects.stream()
                .filter(subject -> subject.getId() == id)
                .map(Subject::getName)
                .findFirst()
                .orElse("Subject not found");
    }

    // Method to get the IDs of subjects by their names
    public static List<Integer> getSubjectsId(Context context, List<String> names) {
        List<Subject> subjects = getAllSubjects(context);
        if (names.isEmpty()) {
            return subjects.stream()
                    .map(Subject::getId)
                    .collect(Collectors.toList());
        }
        return subjects.stream()
                .filter(s -> names.contains(s.getName()))
                .map(Subject::getId)
                .collect(Collectors.toList());
    }

    // Method to get the names of subjects by their IDs
    public static List<String> getSubjectsName(Context context, List<Integer> ids) {
        List<Subject> subjects = getAllSubjects(context);
        if (ids.isEmpty()) {
            return subjects.stream()
                    .map(Subject::getName)
                    .collect(Collectors.toList());
        }
        return subjects.stream()
                .filter(s -> ids.contains(s.getId()))
                .map(Subject::getName)
                .collect(Collectors.toList());
    }

    // Method to get all subjects
    public static List<Subject> getAllSubjects(Context context) {
        List<Subject> subjects;
        try {
            subjects = TeacherHttpCommands.getAllSubjects(context).getSubjects();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return subjects;
    }

}
