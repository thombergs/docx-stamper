package org.wickedsource.docxstamper.context;

public class Student {
    public Student() {
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    int number;
    String name;
    int age;

    public Student(int number, String name, int age) {
        this.number = number;
        this.name = name;
        this.age = age;
    }
}