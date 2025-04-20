package com.sns.project.domain.post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class PostImageInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String imgUrl;

  private Integer orderIndex;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @Builder
  public PostImageInfo(String imgUrl, Post post) {
    this.imgUrl = imgUrl;
    this.post = post;
  }

}