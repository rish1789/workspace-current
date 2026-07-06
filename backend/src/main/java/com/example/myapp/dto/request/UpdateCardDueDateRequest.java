// UpdateCardDueDateRequest.java
package com.example.myapp.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for PATCH /api/cards/{id}/due-date
 * Null is accepted — clears the due date.
 */
public class UpdateCardDueDateRequest {
    @NotNull(message = "Due Date cannot be null")
    private LocalDate date;

    public LocalDate getDate()               { return date;        }
    public void      setDate(LocalDate date) { this.date = date;   }
}