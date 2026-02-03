package hrms.controller;

import com.example.auth.dto.ApiResponse;
import hrms.dto.CreateEmployeeRequestDTO;
import hrms.dto.EmployeeResponseDTO;
import hrms.dto.UpdateEmployeeRequestDTO;
import hrms.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hrms/employees")
@RequiredArgsConstructor
public class EmployeeManagementController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequestDTO request) {
        EmployeeResponseDTO employee = employeeService.createEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", employee));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getCurrentEmployee() {
        EmployeeResponseDTO employee = employeeService.getCurrentEmployee();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", employee));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateCurrentEmployee(
            @Valid @RequestBody UpdateEmployeeRequestDTO request) {
        EmployeeResponseDTO employee = employeeService.updateCurrentEmployee(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", employee));
    }
}