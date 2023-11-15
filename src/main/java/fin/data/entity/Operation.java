package fin.data.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Operation {
	@Id
	@GeneratedValue
	private long id;
	private String description;
	private BigDecimal sum;
	private LocalDateTime date;

	@Override
	public String toString() {
		return "id=" + id + " description=" + description + " sum=" + sum + " date=" + date;
	}
}
