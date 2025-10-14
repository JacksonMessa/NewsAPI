package com.example.NewsAPI.domain.news;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.UUID;

public record NewsResponseDTO(String message,
                              UUID id,
                              String title,
                              String body,
                              @JsonFormat(
                                      shape = JsonFormat.Shape.STRING,
                                      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                                      timezone = "America/Sao_Paulo"
                              )
                              Date publishedAt,
                              String writer) {
}
