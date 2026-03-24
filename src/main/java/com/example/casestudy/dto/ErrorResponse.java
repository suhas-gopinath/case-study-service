package com.example.casestudy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for standardized error responses.
 * 
 * This class represents the structure of error responses returned by the API.
 * It maintains the exact format required by the API contract:
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Some message"
 * }
 * 
 * Design Notes:
 * - Uses Lombok annotations for cleaner code following DTO package conventions
 * - Maintains exact JSON structure for backward compatibility
 * 
 * Constraints:
 * - DO NOT add new fields (no timestamp, no path, no nested objects)
 * - DO NOT rename existing fields
 * - Maintain exact JSON structure for backward compatibility
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
}