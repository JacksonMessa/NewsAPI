package com.example.NewsAPI.domain.news;

import java.util.List;

public record NewsGetResponseListDTO(String message, long newsFound, int pagesFound, List<NewsGetResponseDTO> news) {
}
