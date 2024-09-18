package com.mlorenzo.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

// Nota: Tipos de métricas: Timer, Counter, Gauge, DistributionSummary, LongTaskTimer, FunctionCounter, TimeGauge.

// Nota: Micrometer tiene una convención por defecto del formato de nombres de las métricas donde se utiliza minúsculas y el caracter '.' para
// separar nombres compuestos. Cada implementación de Micrometer(Por ejemplo, Prometheus) viene con un transformador para convertir esa convención
// estándar al formato de la implementación usada.

public class MetricsApplication {

	public static void main(String[] args) {
		MetricsApplication metricsApplication = new MetricsApplication();
		
		metricsApplication.executeSimpleMeterRegistryExample();
		metricsApplication.executeCompositeMeterRegistryExample01();
		metricsApplication.executeCompositeMeterRegistryExample02();
		metricsApplication.executeGlobalRegistryExample();
		metricsApplication.executeCounterMetricBuilderExample();
		metricsApplication.executeTimerMetricExample();
		metricsApplication.executeGaugeMetricBuilderExample();
	}
	
	
	private void executeSimpleMeterRegistryExample() {
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		// Crea una métrica de tipo contador, con nombre "numero.empleados" y tag "oficina="John Doe"(clave=valor), en el registro.
		Counter counter = meterRegistry.counter("numero.empleados", "oficina", "John Doe");
		
		// Incrementa el contador en 1 y después en 200
		counter.increment();
		counter.increment(200);
		
		System.out.printf("SimpleMeterRegistryExample - Número de empleados %f\n", counter.count());
	}
	
	// Nota: En general, podemos crear una métrica a partir de un registro de métricas(Por ejemplo, ver el método de
	// arriba "executeSimpleMeterRegistryExample" donde se crea una métrica de tipo Counter a partir de un registro de tipo "SimpleMeterRegistry")
	// o a partir del patrón Builder que tiene cada tipo de métrica(Por ejemplo, ver el método de abajo "executeCounterMetricBuilder" donde se
	// crea una métrica de tipo Counter a partir de este patrón).
	
	// Nota: Una métrica de tipo Counter no es más que realizar una medición de un contador.
	private void executeCounterMetricBuilderExample() {
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		// Creamos una métrica de tipo Counter usando el patrón Builder y, al final, se agrega al registro de métricas de tipo "SimpleMeterRegistry".
		Counter counter = Counter.builder("numero.estudiantes")
				.description("Número de estudiantes")
				.tag("curso", "Métricas con Micrometer")
				.register(meterRegistry);
		
		// Incrementamos el contador.
		counter.increment();
		counter.increment(200);
		
		System.out.printf("CounterMetricBuilderExample - Número de estudiantes %f\n", counter.count());
	}
	
	// Nota: Una métrica de tipo Timer nos permite calcular el tiempo de ejecución de un código o proceso.
	private void executeTimerMetricExample() {
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		// Crea una métrica de tipo Timer con nombre "tiempo.ejecucion" en el registro de tipo "SimpleMeterRegistry".
		Timer timer = meterRegistry.timer("tiempo.ejecucion");
		
		// Empieza a medir el tiempo de ejecución del código indicado en la función lambda.
		timer.record(() -> {
			for(int i = 0; i < 100; i++)
				System.out.println(i);
		});
		
		// Calcula el tiempo total en la unidad de tiempo que le indiquemos.
		System.out.println("TimerMetricExample - " + timer.totalTime(TimeUnit.MILLISECONDS) + " ms");
	}
	
	// Nota: Una métrica de tipo Gauge nos permite realizar una medición sobre una propiedad o atributo de un objeto(Por ejemplo,
	// tamaño de una lista, tamaño de una caché, cantidad de memoria, etc...).
	private void executeGaugeMetricBuilderExample() {
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		List<String> listaNombres = new ArrayList<String>(4);
		
		// Creamos una métrica de tipo Gauge usando el patrón Builder y, al final, se agrega al registro de métricas de tipo "SimpleMeterRegistry".
		// Para ello, indicamos el nombre de la métrica, el objecto sobre el cual queremos aplicar la métrica y la propiedad de ese objecto que se tiene que medir. 
		// (En este caso, queremos aplicar la métrica sobre una lista para que mida su tamaño).
		// Versión simplificada de la expresión "lista -> lista.size()"
		Gauge gauge = Gauge.builder("tam.lista", listaNombres , List::size)
				.register(meterRegistry);
		
		// Muestra 0 porque aún no hay elementos en la lista
		System.out.println("GaugeMetricBuilderExample - " + gauge.value() + " elementos");
		
		listaNombres.addAll(Arrays.asList("Juan", "Felipe", "Laura", "Karla"));
		
		System.out.println("GaugeMetricBuilderExample - " + gauge.value() + " elementos");
	}
	
	private void executeCompositeMeterRegistryExample01() {
		// Se trata de un agrupador de varios tipos de registros de métricas. Es útil cuando queremos utilizar métricas en varios
		// sistemas de monitoreo(cada sistema de monitoreo se corresponde con un tipo de registro de métricas).
		CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
		
		// Crea una métrica de tipo contador, con nombre "numero.empleados" y tag "oficina="John Doe"(clave=valor), en el agrupador.
		Counter counter = compositeMeterRegistry.counter("numero.empleados", "oficina", "John Doe");
		
		// Se incrementa el contador pero en realidad no se lleva a cabo porque en este punto no hay ningún registro de méticas en el agrupador.
		counter.increment();
		counter.increment(200);
		
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		// Agrega el registro de métricas anterior al agrupador.
		compositeMeterRegistry.add(meterRegistry);
		
		// Muestra 0 porque los incrementos del contador no se han llevado a cabo.
		System.out.printf("CompositeMeterRegistryExample01 - Número de empleados %f\n", counter.count());
	}
	
	private void executeCompositeMeterRegistryExample02() {
		// Se trata de un agrupador de varios tipos de registros de métricas. Es útil cuando queremos utilizar métricas en varios
		// sistemas de monitoreo(cada sistema de monitoreo se corresponde con un tipo de registro de métricas).
		CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
		
		// Crea una métrica de tipo contador, con nombre "numero.empleados" y tag "oficina="John Doe"(clave=valor), en el agrupador.
		Counter counter = compositeMeterRegistry.counter("numero.empleados", "oficina", "John Doe");
		
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		// Agrega el registro de métricas anterior al agrupador.
		compositeMeterRegistry.add(meterRegistry);
		
		// Este contador se incrementa y, en este caso, si se produce realmente porque en este punto ya existe un registro de métricas en el agrupador.
		counter.increment();
		counter.increment(200);
		
		System.out.printf("CompositeMeterRegistryExample02 - Número de empleados %f\n", counter.count());
	}
	
	private void executeGlobalRegistryExample() {
		// Por defecto, existe un agrupador de registros de métricas global(Un Singleton). Es útil cuando se quiere tener un agrupador global de registros común para toda la aplicación en vez de crearlos.
		// Obtenemos la referencia a ese agrupador global.
		CompositeMeterRegistry globalRegistry = Metrics.globalRegistry;
		
		// Si queremos modificar el convenio del formato de los nombres de las métricas por defecto de Micromenter, tenemos que implementar el método
		// abstracto de la interfaz "NamingConvention"(también podemos hacerlo mediante una función lambda porque se trata de una Interfaz Funcional).
		/*globalRegistry.config().namingConvention(new NamingConvention() {
			
			@Override
			public String name(String name, Type type, String baseUnit) {
				// TODO Auto-generated method stub
				return null;
			}
		});*/
		
		// Crea una métrica de tipo contador, con nombre "numero.empleados" y tag "oficina="John Doe"(clave=valor), en el agrupador global.
		Counter counter = globalRegistry.counter("numero.empleados", "oficina", "John Doe");
		
		// Crea un registro de métricas de tipo SimpleMeterRegistry.
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		
		// Agrega el registro de métricas anterior al agrupador global.
		globalRegistry.add(meterRegistry);
		
		// Este contador se incrementa y, en este caso, si se produce realmente porque en este punto ya existe un registro de métricas en el agrupador.
		counter.increment();
		counter.increment(200);
		
		incrementCounterGlobalRegistry();
		
		System.out.printf("GlobalRegistryExample - Número de empleados %f\n", counter.count());
	}
	
	private void incrementCounterGlobalRegistry() {
		// Obtenemos la referencia al agrupador global.
		CompositeMeterRegistry globalRegistry = Metrics.globalRegistry;
		
		// En este caso, se obtiene la métrica del agrupador global porque ya existía previamente.
		Counter counter = globalRegistry.counter("numero.empleados", "oficina", "John Doe");
		
		// Se incrementa el contador en 150 con respecto a su valor actual.
		counter.increment(150);
	}

}
