package com.myjavablog.servicemcpai;

import com.myjavablog.servicemcpai.util.FinancialStatementPdfGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@SpringBootTest
class ServiceMcpAiApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) throws IOException {
        FinancialStatementPdfGenerator.generatePDF("financial_statement.pdf");

        File file = new File("financial_statement.pdf");
        MultipartFile multipartFile = new MockMultipartFile(
                "file",                          // form field name
                file.getName(),                  // original filename
                "application/pdf",               // content type
                Files.readAllBytes(file.toPath()) // file content as bytes
        );
        List<String> descriptions =
                FinancialStatementPdfGenerator.extractDescriptions(multipartFile);

        System.out.println("=== Descriptions Extracted ===");
        descriptions.forEach(System.out::println);
    }
}
