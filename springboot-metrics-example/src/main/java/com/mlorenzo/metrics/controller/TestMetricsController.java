package com.mlorenzo.metrics.controller;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("metrics")
public class TestMetricsController {
	private final static Logger log = LoggerFactory.getLogger(TestMetricsController.class);
	private final static Random random = new Random();
	
	@Autowired
	private MeterRegistry meterRegistry;
	
	// Nota: La métrica personalizada aparecerá en el endpoint "metrics" de Actuator en cuanto se ejecute por primera vez este método handler.
	@GetMapping("counter")
	public String getCounterString() {
		// Para ver en el log el tipo de registro de métricas que se está usando(El tipo es "PrometheusMeterRegistry" porque tenemos la implementación
		// "Prometheus" de Micrometer como dependencia del proyecto).
		log.info("MeterRegistry used: {}", meterRegistry.getClass().getName());
		
		// Creamos(si ya existe, se obtiene) esta métrica personalizada de tipo Counter, le damos el nombre de "test.counter.metric" y la agregamos
		// al registro de métricas que nos proporciona Spring. Además, incrementamos el contador en una unidad.
		meterRegistry.counter("test.counter.metric").increment();
		
		return "Test Counter Metric";
	}
	
	@GetMapping("timer")
	public String getTimerString() {
		// Creamos(si ya existe, se obtiene) esta métrica personalizada de tipo Timer, le damos el nombre de "test.timer.metric" y la agregamos
		// al registro de métricas que nos proporciona Spring.
		Timer timer = meterRegistry.timer("test.timer.metric");
		
		// Medimos el tiempo de ejecución del bloque de código que se indica en esta función lambda
		timer.record(() -> {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			log.info("Usless task");
		});
		
		return "Test Timer Metric";
	}
	
	// Esta anotación crea una métrica de tipo "Timer" y es útil para medir el tiempo de la ejecución de todo un método handler.
	// Nota: Si se necesita medir el tiempo de ejecución de un bloque de código determinado, hay que usar el método indicado en el método handler de arriba "getTimerString".
	// Nota: Si la anotación se pone a nivel de clase, entonces el tiempo de ejecución de todos los métodos handler de esa clase serán medidos.
	// Nota: Esta anotación solo funciona en la capa web, es decir, en clases que son controladores. No funciona en clases de otro tipo de capas.
	@Timed("test.timed.annotation.metric")
	@GetMapping("timed-annotation")
	public String getTimedAnnotationString() {
		int randomInt = random.nextInt(20);
		
		if(randomInt >= 0 && randomInt <= 4) {
			// Nota: Los logs también generan métricas.
			log.warn("Warning");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
		if(randomInt >= 5 && randomInt <= 9) {
			log.warn("Warning");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		if(randomInt > 9 && randomInt <= 13) {
			log.error("Error");
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("Success");
		
		return "Test Timed Annotation Metric";
	}

}
