package com.example.NewsAPI.domain.news;
import com.example.NewsAPI.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Table(name = "news")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class News {
    @GeneratedValue
    @Id
    private UUID id;
    private String title;
    private String body;
    private Date publishedAt;
    @ManyToOne
    @JoinColumn(name = "writer_id")
    private User writer;
}

