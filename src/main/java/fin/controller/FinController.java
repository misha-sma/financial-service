package fin.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fin.data.entity.Operation;
import fin.data.entity.ValCurs;
import fin.data.entity.Valute;
import fin.data.repository.OperationRepository;
import jakarta.annotation.PostConstruct;

@RestController
public class FinController {
	public static final Object SYNCH_OBJECT = new Object();

	private BigDecimal RUB_2_USD = new BigDecimal(100);
	private BigDecimal RUB_2_EUR = new BigDecimal(120);

	public static enum Course {
		RUB, USD, EUR
	};

	@Autowired
	private OperationRepository operationRepository;

	@Value("${courses.times}")
	private String coursesTimesStr;

	@Value("${cb.url}")
	private String cbUrl;

	private List<ScheduledExecutorService> schedulers = new ArrayList<ScheduledExecutorService>();

	@PostConstruct
	private void getCoursesShedulers() {
		String[] times = coursesTimesStr.split(",");
		for (String time : times) {
			String[] parts = time.split(":");
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			long delay = getDelay(hours, minutes);
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					loadCourses();
				}
			}, delay, 24 * 3600 * 1000, TimeUnit.MILLISECONDS);
			schedulers.add(scheduler);
		}
	}

	private long getDelay(int hours, int minutes) {
		LocalDateTime now = LocalDateTime.now();
		int dh = hours - now.getHour();
		int dm = minutes - now.getMinute();
		if (dm < 0) {
			--dh;
			dm += 60;
		}
		if (dh < 0) {
			dh += 24;
		}
		long delay = (dm + 60 * dh) * 60000;
		delay -= now.getSecond() * 1000 + now.getNano() / 1000000;
		if (delay < 0) {
			delay = 0;
		}
		return delay;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHomePage() {
		return "<html><body>Financial service works!!!</body></html>";
	}

	@RequestMapping(value = "/api/addOperation", method = RequestMethod.POST)
	public void addOperation(@RequestBody Operation operation) {
		System.out.println("op=" + operation);
		operationRepository.save(operation);
	}

	@RequestMapping(value = "/api/getOperations", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public List<Operation> getOperations(@RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime,
			@RequestParam Course course) {
		List<Operation> operations = operationRepository.findByDateBetweenOrderByDate(startTime, endTime);
		if (course == Course.RUB) {
			return operations;
		} else {
			final BigDecimal courseBigD;
			synchronized (SYNCH_OBJECT) {
				courseBigD = course == Course.USD ? RUB_2_USD : RUB_2_EUR;
			}
			operations.stream().forEach(op -> op.setSum(op.getSum().divide(courseBigD, 2, RoundingMode.HALF_UP)));
		}
		return operations;
	}

	@RequestMapping(value = "/api/loadCourses", method = RequestMethod.GET)
	public void loadCourses() {
		HttpGet httpget = new HttpGet(cbUrl);
		String xml = null;
		try (CloseableHttpClient httpclient = HttpClients.createDefault();
				CloseableHttpResponse cbResponse = httpclient.execute(httpget)) {
			HttpEntity entity = cbResponse.getEntity();
			xml = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} catch (IOException e) {
			System.out.println("Error while downloading courses from cb");
			e.printStackTrace();
		}
		parseCourses(xml);
		System.out.println("Updated courses USD=" + RUB_2_USD + " EUR=" + RUB_2_EUR + " time=" + LocalDateTime.now());
	}

	private void parseCourses(String xml) {
		int index = xml.indexOf('<');
		if (index > 0) {
			xml = xml.substring(index);
		}
		xml = xml.replace(',', '.');
		XmlMapper xmlMapper = XmlMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.build();
		ValCurs valCurs = null;
		try {
			valCurs = xmlMapper.readValue(xml, ValCurs.class);
		} catch (JsonMappingException e) {
			System.out.println("Error while parsing courses xml");
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			System.out.println("Error while parsing courses xml");
			e.printStackTrace();
		}
		Valute valuteUSD = valCurs.getValCurs().stream().filter(v -> "USD".equals(v.getCharCode())).findFirst().get();
		Valute valuteEUR = valCurs.getValCurs().stream().filter(v -> "EUR".equals(v.getCharCode())).findFirst().get();
		synchronized (SYNCH_OBJECT) {
			RUB_2_USD = valuteUSD.getVUnitRate();
			RUB_2_EUR = valuteEUR.getVUnitRate();
		}
	}

}
