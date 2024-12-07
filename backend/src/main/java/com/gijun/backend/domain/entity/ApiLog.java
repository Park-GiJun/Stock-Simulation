package com.gijun.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_logs")
@Getter
@NoArgsConstructor
public class ApiLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String traceId;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String requestUri;

    @Column(nullable = false)
    private String controllerName;

    @Column(nullable = false)
    private String methodName;

    @Column(length = 4000)
    private String requestParameters;

    @Column(length = 4000)
    private String responseData;

    @Column(nullable = false)
    private String clientIp;

    @Column
    private String userAgent;

    @Column(nullable = false)
    private Long executionTime;

    @Column
    private String errorMessage;

    @Column(nullable = false)
    private boolean success;

    @Builder
    public ApiLog(String traceId, String httpMethod, String requestUri,
                  String controllerName, String methodName, String requestParameters,
                  String responseData, String clientIp, String userAgent,
                  Long executionTime, String errorMessage, boolean success) {
        this.traceId = traceId;
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.controllerName = controllerName;
        this.methodName = methodName;
        this.requestParameters = requestParameters;
        this.responseData = responseData;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.executionTime = executionTime;
        this.errorMessage = errorMessage;
        this.success = success;
    }
}