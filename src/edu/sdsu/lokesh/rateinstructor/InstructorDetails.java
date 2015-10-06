package edu.sdsu.lokesh.rateinstructor;

public class InstructorDetails {
	public Instructor InstructorObj;
	public String Office;
	public String Phone;
	public String Email;
	public String Average;
	public String TotalRatings;

	public InstructorDetails(Instructor instructor, String office,
			String phone, String email, String average, String totalratings) {
		InstructorObj = instructor;
		Office = office;
		Phone = phone;
		Email = email;
		Average = average;
		TotalRatings = totalratings;
	}
}
