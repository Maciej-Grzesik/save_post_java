package com.save.posts;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.save.posts.domain.service.PostService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@SpringBootApplication
public class PostsApplication implements CommandLineRunner {
	private final PostService postService;

	public static void main(String[] args) {
		SpringApplication.run(PostsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Starting Posts Application");
		try {
			postService.fetchAndSavePosts().block();
			log.info("Application completed successfully");
		} catch (Exception e) {
			log.error("Application failed with error: {}", e.getMessage(), e);
			throw e;
		}
	}

}
