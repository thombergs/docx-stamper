package org.wickedsource.docxstamper.context;

import java.util.ArrayList;
import java.util.List;


public class Grade {
	public Grade() {
	}

	int number;
	List<AClass> classes = new ArrayList<>();

	public int getNumber() {
		return number;
	}

	public Grade(int number) {
		this.number = number;
	}

	public List<AClass> getClasses() {
		return classes;
	}
}
