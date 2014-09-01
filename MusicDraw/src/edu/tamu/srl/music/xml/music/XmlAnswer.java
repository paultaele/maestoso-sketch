package edu.tamu.srl.music.xml.music;

import java.util.List;
import java.util.ArrayList;

public class XmlAnswer {
	
	public XmlAnswer() {
		myStaffList = new ArrayList<XmlStaff>();
	}
	
	public XmlAnswer(List<XmlStaff> staffList) {
		
		myStaffList = staffList;
	}
	
	public List<XmlStaff> getStaffList() { return myStaffList; }

	private List<XmlStaff> myStaffList;
}
