package com.myjavablog.servicemcpai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myjavablog.servicemcpai.model.AiCategoryListResponse;
import com.myjavablog.servicemcpai.model.AiCategoryResponse;
import com.myjavablog.servicemcpai.model.Category;
import com.myjavablog.servicemcpai.repository.CategoryRepository;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AnthropicChatModel anthropicChatModel;
    private final ObjectMapper objectMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           AnthropicChatModel anthropicChatModel,
                           ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.anthropicChatModel = anthropicChatModel;
        this.objectMapper = objectMapper;
    }

    public List<Category> listCategories() {
        return categoryRepository.listCategories();
    }
    public Category guessCategory(String description) {

        BeanOutputConverter<AiCategoryResponse> converter = new BeanOutputConverter<>(AiCategoryResponse.class);

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model("claude-sonnet-4-5") // using the most capable model to get the best results
                .maxTokens(500) // helps to manage cost by limiting the quantity of tokens
                .temperature(0.0) // makes the answer closer to deterministic
                .build();

        String promptContent;
        try {
            promptContent = """
                You are a financial transaction classifier.
                Analyze the input transaction and assign the most appropriate category.
        
                # Input Transaction
                %s
        
                # Instructions
                - Select the categoryId from System Categories that best matches the transaction.
                - Category is mandatory — make the most educated guess if unsure.
                - categoryId must be a Long that exists in the System Categories list.
                - observation is an optional String explaining your reasoning.
        
                # Output Rules
                - Respond with ONLY a raw JSON object. Nothing else.
                - Do NOT use markdown, code fences, backticks, or triple backticks.
                - Do NOT wrap your response in ```json or ```
                - Do NOT add explanations, summaries, or any text outside the JSON.
                - Your entire response must start with '{' and end with '}'.
                - It must be directly parseable by JSON.parse() with zero pre-processing.
        
                # System Categories
                %s
        
                # Required Output Format
                {"id": <Long>, "observation": "<your reasoning>"}
        
                # Examples
                Input: Netflix subscription
                Output: {"id": 6, "observation": "Netflix is a streaming service, matches Entertainment."}
        
                Input: Indigo flight to Mumbai
                Output: {"id": 3, "observation": "Indigo is an airline, matches Travel."}
        """.formatted(description, objectMapper.writeValueAsString(listCategories()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Prompt prompt = new Prompt(promptContent, options);

        var response = anthropicChatModel.call(prompt);
        Long categoryId = converter.convert(response.getResult().getOutput().getText()).categoryId();

        return listCategories().stream()
                .filter(category -> category.id().equals(categoryId))
                .findFirst()
                .orElseThrow();
    }

    public List<Category> guessCategories(List<String> descriptions) {

        BeanOutputConverter<AiCategoryListResponse> converter = new BeanOutputConverter<>(AiCategoryListResponse.class);

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model("claude-sonnet-4-5") // using the most capable model to get the best results
                .maxTokens(500) // helps to manage cost by limiting the quantity of tokens
                .temperature(0.0) // makes the answer closer to deterministic
                .build();

        String promptContent;
        try {
            promptContent = """
                            You are a financial transaction classifier. You will analyze the parsed transactions and define what should be the category for each one.
                            
                            # Input Transaction
                            %s
                            
                            # Instructions
                            - You must classify each transaction from the **Input Transactions** list
                          - Category is mandatory, so, make the most educated guess, however, there will be cases where an assumption should be made.
                          - Your output contains:
                            * categoryId, with is a long that must match the list of **System Candidates Categories**
                            * sourceDescription: the original transaction text exactly as provided in the input
                            * observation: an optional String that you should inform additional notes or rational behind the chosen category.

                            
                            # Output Rules
                            - Respond with ONLY a raw JSON object. Nothing else.
                            - Do NOT use markdown, code fences, backticks, or triple backticks.
                            - Do NOT wrap your response in ```json or ```
                            - Do NOT add explanations, summaries, or any text outside the JSON.
                            - Your entire response must start with '{' and end with '}'.
                            - It must be directly parseable by JSON.parse() with zero pre-processing.
                            
                            # System Categories
                            %s
                            
                            # Required Output Format
                            {"categories": [
                                {"categoryId": <Long>, "sourceDescription": "<transaction description>", "observation": "<reasoning>"},
                                {"categoryId": <Long>, "sourceDescription": "<transaction description>", "observation": "<reasoning>"}
                            ]}
                                        
                            # Example
                            Input: ["Netflix subscription", "Indigo flight to Mumbai"]
                            Output: {"categories": [
                                {"categoryId": 6, "sourceDescription": "Netflix subscription", "observation": "Streaming service, matches Entertainment."},
                                {"categoryId": 3, "sourceDescription": "Indigo flight to Mumbai", "observation": "Airline, matches Travel."}
                            ]}
                    """.formatted(
                    descriptions.stream().map(d -> " - `" + d + "`").reduce((a, b) -> a + "\n" + b).orElse(""),
                    objectMapper.writeValueAsString(listCategories())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Prompt prompt = new Prompt(promptContent, options);

        var response = anthropicChatModel.call(prompt);
        var responseObj = converter.convert(response.getResult().getOutput().getText());

        return descriptions.stream()
                .map(description -> {
                    AiCategoryResponse current = responseObj.categories().stream()
                            .filter(c -> description.equals(c.sourceDescription()))
                            .findFirst()
                            .orElseThrow();
                    return listCategories().stream()
                            .filter(category -> category.id() == current.categoryId())
                            .findFirst()
                            .orElseThrow();
                })
                .toList();
    }
}
