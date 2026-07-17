package com.bldcommons.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.bld.commons.example.CryptoExampleApplication;

@SpringBootTest(classes = CryptoExampleApplication.class)
@AutoConfigureMockMvc
class CleanExcessSpacesRequestParamTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCleanExcessSpacesOnRequestParam() throws Exception {
		mockMvc.perform(get("/clean/spaces").param("text", "   hello     world   "))
				.andExpect(status().isOk())
				.andExpect(content().string("hello world"));
	}

	@Test
	void shouldCleanExcessSpacesAndUpperCaseOnRequestParam() throws Exception {
		mockMvc.perform(get("/clean/upper").param("text", "   hello     world   "))
				.andExpect(status().isOk())
				.andExpect(content().string("HELLO WORLD"));
	}

}
