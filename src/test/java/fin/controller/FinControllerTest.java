package fin.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fin.data.entity.Operation;
import fin.data.repository.OperationRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FinControllerTest {
	@Autowired
	MockMvc mvc;

	@Autowired
	private OperationRepository operationRepository;

	@BeforeEach
	public void cleanDb() {
		operationRepository.deleteAll();
	}

	@Test
	public void testAddOperation() throws Exception {
		String json = "{\"description\":\"operation description\",\"sum\":12.56,\"date\":\"2023-11-09T10:15:30\"}";
		mvc.perform(post("/api/addOperation").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
		Assert.assertEquals(1, operationRepository.count());
	}

	@Test
	public void testLoadCourses() throws Exception {
		mvc.perform(get("/api/loadCourses")).andExpect(status().isOk());
	}

	@Test
	public void testGetOperationsRub() throws Exception {
		operationRepository.save(new Operation(0, "operation description", new BigDecimal(12.56),
				LocalDateTime.of(2023, 11, 9, 10, 15, 30)));

		String json = mvc
				.perform(get("/api/getOperations?startTime=2021-11-09T10:15:30&endTime=2024-11-09T10:15:30&course=RUB"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		List<Operation> operations = mapper.readValue(json, new TypeReference<List<Operation>>() {
		});
		Assert.assertEquals(1, operations.size());
		Operation operation = operations.get(0);
		Assert.assertEquals("operation description", operation.getDescription());
		Assert.assertTrue(Math.abs(12.56 - operation.getSum().doubleValue()) < 0.001);
		Assert.assertEquals(LocalDateTime.of(2023, 11, 9, 10, 15, 30), operation.getDate());
	}

	@Test
	public void testGetOperationsUsd() throws Exception {
		operationRepository.save(new Operation(0, "operation description", new BigDecimal(12.56),
				LocalDateTime.of(2023, 11, 9, 10, 15, 30)));

		String json = mvc
				.perform(get("/api/getOperations?startTime=2021-11-09T10:15:30&endTime=2024-11-09T10:15:30&course=USD"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		List<Operation> operations = mapper.readValue(json, new TypeReference<List<Operation>>() {
		});
		Assert.assertEquals(1, operations.size());
		Operation operation = operations.get(0);
		Assert.assertEquals("operation description", operation.getDescription());
		Assert.assertEquals(LocalDateTime.of(2023, 11, 9, 10, 15, 30), operation.getDate());
	}

	@Test
	public void testGetOperationsEur() throws Exception {
		operationRepository.save(new Operation(0, "operation description", new BigDecimal(12.56),
				LocalDateTime.of(2023, 11, 9, 10, 15, 30)));

		String json = mvc
				.perform(get("/api/getOperations?startTime=2021-11-09T10:15:30&endTime=2024-11-09T10:15:30&course=EUR"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		List<Operation> operations = mapper.readValue(json, new TypeReference<List<Operation>>() {
		});
		Assert.assertEquals(1, operations.size());
		Operation operation = operations.get(0);
		Assert.assertEquals("operation description", operation.getDescription());
		Assert.assertEquals(LocalDateTime.of(2023, 11, 9, 10, 15, 30), operation.getDate());
	}

}
