package fin.data.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Valute {
	@JsonProperty("CharCode")
	private String charCode;

	@JsonProperty("VunitRate")
	private BigDecimal vUnitRate;
}
