package com.example.employees.controller;

import com.example.employees.model.Employee;
import com.example.employees.service.EmployeeService;
import com.example.employees.service.ServiceResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getEmployees(
            @RequestParam(required = false) String department) {
        return response(employeeService.getEmployees(department), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@Valid @RequestBody Employee employee) {
        return response(employeeService.addEmployee(employee), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable int id, @Valid @RequestBody Employee employee) {
        return response(employeeService.updateEmployee(id, employee), HttpStatus.OK);
    }

    private <T> ResponseEntity<T> response(ServiceResult<T> result, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header("X-Data-Source", result.fallback() ? "local-fallback" : "downstream")
                .body(result.value());
    }
}
