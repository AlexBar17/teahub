package com.tea.teahub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tea.teahub.mapper.TeaMapper;
import com.tea.teahub.repository.TeaStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class FullContext extends PostgresContainerTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TeaMapper teaMapper;

    @Autowired
    protected TeaStorage teaStorage;
}
