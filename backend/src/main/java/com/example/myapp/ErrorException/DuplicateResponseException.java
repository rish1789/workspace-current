package com.example.myapp.ErrorException;

public class DuplicateResponseException extends RuntimeException {
      public DuplicateResponseException(String message){
        super(message);
      }
}
