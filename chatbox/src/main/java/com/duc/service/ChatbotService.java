package com.duc.service;

import com.duc.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface ChatbotService {
    ApiResponse getCoinDetails(String prompt) throws Exception;

    String simpleChat(String prompt) throws JsonProcessingException;
}
