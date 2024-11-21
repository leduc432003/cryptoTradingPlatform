package com.duc.service;

import com.duc.response.ApiResponse;

public interface ChatbotService {
    ApiResponse getCoinDetails(String prompt) throws Exception;

    String simpleChat(String prompt);
}
