package com.example.employees.service;

import com.example.employees.exception.BusinessException;
import com.example.employees.exception.OperationException;
import com.example.employees.model.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Service
public class EmployeeService {
    private static final String EMPLOYEES_PATH = "/api/employees";
    private final RestClient restClient;
    private final String downstreamBaseUrl;
    private final ConcurrentMap<Integer, Employee> fallbackEmployees = new ConcurrentHashMap<>();

    public EmployeeService(
            RestClient.Builder restClientBuilder,
            @Value("${employee.downstream.base-url}") String downstreamBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(downstreamBaseUrl).build();
        this.downstreamBaseUrl = downstreamBaseUrl;
    }

    public ServiceResult<List<Employee>> getEmployees(String department) {
        return invoke(
                () -> {
                    List<Employee> employees = restClient.get()
                            .uri(uriBuilder -> {
                                var builder = uriBuilder.path(EMPLOYEES_PATH);
                                if (department != null && !department.isBlank()) {
                                    builder.queryParam("department", department);
                                }
                                return builder.build();
                            })
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {
                            });
                    if (employees == null) {
                        throw new OperationException(
                                "Downstream returned an empty employee response", HttpStatus.BAD_GATEWAY, null);
                    }
                    employees.forEach(employee -> fallbackEmployees.put(employee.id(), employee));
                    return employees;
                },
                () -> fallbackEmployees.values().stream()
                        .filter(employee -> department == null || department.isBlank()
                                || employee.department().toLowerCase(Locale.ROOT)
                                        .equals(department.toLowerCase(Locale.ROOT)))
                        .sorted((first, second) -> Integer.compare(first.id(), second.id()))
                        .toList());
    }

    public ServiceResult<Employee> addEmployee(Employee employee) {
        return invoke(
                () -> {
                    Employee created = restClient.post()
                            .uri(EMPLOYEES_PATH)
                            .body(employee)
                            .retrieve()
                            .body(Employee.class);
                    Employee result = requireEmployee(created);
                    fallbackEmployees.put(result.id(), result);
                    return result;
                },
                () -> {
                    Employee existing = fallbackEmployees.putIfAbsent(employee.id(), employee);
                    if (existing != null) {
                        throw new BusinessException(
                                "Employee with id " + employee.id() + " already exists", HttpStatus.CONFLICT);
                    }
                    return employee;
                });
    }

    public ServiceResult<Employee> updateEmployee(int id, Employee employee) {
        if (id != employee.id()) {
            throw new BusinessException(
                    "Path employee id must match the employee id in the request body", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return invoke(
                () -> {
                    Employee updated = restClient.put()
                            .uri(EMPLOYEES_PATH + "/{id}", id)
                            .body(employee)
                            .retrieve()
                            .body(Employee.class);
                    Employee result = requireEmployee(updated);
                    fallbackEmployees.put(result.id(), result);
                    return result;
                },
                () -> {
                    if (fallbackEmployees.replace(id, employee) == null) {
                        throw new BusinessException(
                                "Employee with id " + id + " was not found", HttpStatus.NOT_FOUND);
                    }
                    return employee;
                });
    }

    private Employee requireEmployee(Employee employee) {
        if (employee == null) {
            throw new OperationException(
                    "Downstream returned an empty employee response", HttpStatus.BAD_GATEWAY, null);
        }
        return employee;
    }

    private <T> ServiceResult<T> invoke(Supplier<T> downstreamCall, Supplier<T> fallback) {
        if (isDownstreamUnconfigured()) {
            return new ServiceResult<>(fallback.get(), true);
        }
        try {
            return new ServiceResult<>(downstreamCall.get(), false);
        } catch (ResourceAccessException exception) {
            return new ServiceResult<>(fallback.get(), true);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError()) {
                return new ServiceResult<>(fallback.get(), true);
            }
            HttpStatus status = exception.getStatusCode().value() == 404
                    ? HttpStatus.NOT_FOUND
                    : exception.getStatusCode().value() == 409
                            ? HttpStatus.CONFLICT
                            : HttpStatus.UNPROCESSABLE_ENTITY;
            throw new BusinessException("Downstream rejected the employee operation", status);
        } catch (RestClientException exception) {
            throw new OperationException(
                    "Employee downstream operation failed", HttpStatus.BAD_GATEWAY, exception);
        }
    }

    private boolean isDownstreamUnconfigured() {
        return downstreamBaseUrl == null
                || downstreamBaseUrl.isBlank()
                || downstreamBaseUrl.equalsIgnoreCase("TO_BE_DEFINE");
    }
}
