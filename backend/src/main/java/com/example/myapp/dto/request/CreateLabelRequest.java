// CreateLabelRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/boards/{id}/labels
 * Contains the label name and hex color code.
 */
public class CreateLabelRequest {
    private static final String HEX_COLOR_REGEX = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";


    @NotBlank(message = "Label name cannot be empty")
    @Size(min=1,max=100,message= "Label name exceed character limit Max 100 character")
    private String name;

    @NotBlank(message = "Color cannot be empty")
    @Pattern(regexp = HEX_COLOR_REGEX,message = "Invalid color format — must be #RGB or #RRGGBB")
    private String color;

    public String getName()  { return name;  }
    public String getColor() { return color; }

    public void setName(String name)   { this.name  = name;  }
    public void setColor(String color) { this.color = color; }
}