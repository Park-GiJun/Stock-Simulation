package com.gijun.backend;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BackendApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		// 환경 변수를 Java 시스템 프로퍼티로 설정
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		// 환경 변수 로그 출력
		dotenv.entries().forEach(entry -> log.info("Checking environment variables: {} = {}", entry.getKey(), entry.getValue()));

		SpringApplication.run(BackendApplication.class, args);
	}
}