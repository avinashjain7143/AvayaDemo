package com.avaya.resources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avaya.model.Employee;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

@RestController
@RequestMapping({"/avi"})
public class EmployeeController {

	@GetMapping(value = "/healthcheck", produces = "application/json; charset=utf-8")
	public String getHealthCheck()
	{
		return "{ \"isWorking\" : true }";
	}
	
	private Session getSession() {
		Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").withoutJMXReporting().build();
		Session session = cluster.connect("mykeyspace");
		return session;
	}
	
	@GetMapping(value = "/employees", produces = "application/json; charset=utf-8")
	public List<Employee> getEmployees() {
		String query = "select * from employee";
		ResultSet result = getSession().execute(query);
		List<Employee> employeesList = new ArrayList<Employee>();
		for(Row row : result.all()) {
			Employee e = new Employee(row.getInt(0), row.getString(1), row.getString(2), row.getString(3));
			employeesList.add(e);
		}
		return employeesList;
	}
	
	@GetMapping(value = "/employee/{id}", produces = "application/json; charset=utf-8")
	public Employee getEmployees(@PathVariable int id) {
		String query = "select * from employee where id = " + id;
		ResultSet result = getSession().execute(query);
		for(Row row : result.all()) {
			if (row.getInt(0) == id) {
				return new Employee(row.getInt(0), row.getString(1), row.getString(2), row.getString(3));
			}
		}
		return new Employee();
	}
	
	@PutMapping(value = "/addEmployee", consumes = "application/json; charset=utf-8", produces = "application/json; charset=utf-8")
	public Employee addEmployee(@RequestBody Employee e) {
		String query = "INSERT INTO employee(id, firstName, lastName, email) "
				+ "VALUES ("+e.getId()+", '"+e.getFirstName()+"', '"+e.getLastName()+"', '"+e.getEmail()+"')";
		getSession().execute(query);
		System.out.println("Employee successfully added");
		return e;
	}
}
