# Sistema de Solicitudes de Reembolso de Gastos Corporativos

Proyecto Integrador de Cierre (R2) — Unidad 12: Integración de Patrones y Arquitecturas  
Estudiante: Nicolás Andrés Palacio Boada  
Repositorio: `palacio-post1-u12`  
Stack Tecnológico: Java 17, Spring Boot 3.3.4, Maven, Spring Data JPA, H2 Database, ArchUnit 1.x  

---

## 1. Diseño Propuesto (Análisis Previo a la Implementación)

En cumplimiento de las directrices metodológicas de diseño consciente de software, se realiza la identificación analítica de los puntos de variabilidad de comportamiento para los requisitos funcionales del sistema antes de escribir el código de los patrones.

### RF1 — Validación en varias etapas
* **Punto de variabilidad:** La secuencia de reglas de verificación independientes que determinan si una solicitud de reembolso es apta para ser aprobada (tope máximo por categoría, documentos soporte obligatorios y ausencia de solicitudes duplicadas en el período), sumado a la necesidad operativa de incorporar futuras validaciones (por ejemplo, límite anual acumulado por empleado) sin intervenir el código de las verificaciones preexistentes.
* **Patrón de diseño propuesto:** Chain of Responsibility (Cadena de Responsabilidad).
* **Justificación técnica (Test de explicabilidad):** Se utiliza Chain of Responsibility porque desacopla el emisor de la solicitud de la serie heterogénea de validadores, permitiendo encadenar, reordenar y añadir nuevas reglas de negocio de manera extensible e independiente sin modificar las existentes.

### RF2 — Cálculo distinto por categoría de gasto
* **Punto de variabilidad:** El algoritmo de liquidación del monto aprobado según el tipo de desembolso (viáticos con tope diario ponderado por días de viaje, gastos de representación con tope mensual fijo y capacitación con reintegro del 80% sujeto a tope anual), previendo la adición periódica de nuevas categorías (como bienestar corporativo) con fórmulas propias sin alterar la lógica de cálculo vigente.
* **Patrón de diseño propuesto:** Strategy (Estrategia) asistido por un Selector/Registro dinámico.
* **Justificación técnica (Test de explicabilidad):** Se utiliza Strategy porque encapsula cada fórmula de liquidación de reembolsos en un objeto polimórfico intercambiable bajo una interfaz común, permitiendo incorporar futuras categorías de gasto sin alterar las fórmulas vigentes ni introducir ramificaciones condicionales rígidas.

### RF3 — Notificación simultánea a varios interesados
* **Punto de variabilidad:** El conjunto dinámico de actores interesados en las transacciones de cambio de estado (empleado solicitante, departamento de contabilidad y módulo de auditoría interna) y la diversidad de sus reacciones operativas (confirmación al usuario, programación de pago condicionada o bitácora de trazabilidad), con previsión de anexar nuevos receptores como tableros de métricas.
* **Patrón de diseño propuesto:** Observer (Observador).
* **Justificación técnica (Test de explicabilidad):** Se utiliza Observer porque establece un canal de suscripción desacoplado que notifica a múltiples receptores independientes ante una transición de estado, permitiendo añadir nuevos interesados sin modificar el flujo transaccional del servicio ni acoplarlo a los consumidores.

---

## 2. Decisiones de Diseño

### Decisión de Diseño 1: Orden de encadenamiento de validadores
* **Contexto:** Se requiere determinar el orden en que se configuran los eslabones `ValidadorDocumentosSoporte`, `ValidadorMontoMaximo` y `ValidadorDuplicados` dentro de `CadenaValidadores`.
* **Elección técnica:** Orden fail-fast: (1) `ValidadorDocumentosSoporte` -> (2) `ValidadorMontoMaximo` -> (3) `ValidadorDuplicados`.
* **Justificación por eficiencia:** El eslabón más costoso computacionalmente es `ValidadorDuplicados`, ya que requiere iterar y filtrar en memoria una colección de solicitudes existentes ($O(N)$). Por el contrario, verificar la presencia de documentos soporte es una simple evaluación booleana ($O(1)$) y verificar el tope por categoría es una comparación aritmética directa ($O(1)$). Ubicando los validadores más económicos primero, las solicitudes inválidas por omisión de soportes o por exceso de monto son rechazadas inmediatamente sin consumir ciclos de CPU innecesarios en la inspección de listas.
* **Qué se gana y qué se pierde:** Se gana rendimiento, reducción de latencia en peticiones fallidas y economía de recursos; se sacrifica la capacidad de advertir al usuario sobre solicitudes duplicadas si antes ha olvidado adjuntar los comprobantes, lo cual es admisible dado que sin documentos la solicitud carece de validez legal inicial.

### Decisión de Diseño 2: Mecanismo de selección de calculadora polimórfica
* **Contexto:** Elegir cómo resolver la estrategia adecuada según la categoría de gasto: un mapa dinámico inyectado por Spring frente a una estructura `switch` manual.
* **Elección técnica:** Mapa dinámico indexado en `SelectorCalculadora` a partir de `List<CalculadoraReembolso>` autoinyectada por el contenedor de inversión de control de Spring.
* **Justificación por OCP:** Cumple de manera rigurosa el Principio Abierto/Cerrado (OCP). Cuando Finanzas introduzca una nueva categoría (por ejemplo, "bienestar"), bastará con crear una nueva clase anotada con `@Component` que implemente `CalculadoraReembolso`. Spring la descubrirá en el escaneo de componentes y la integrará en el mapa sin requerir la modificación ni recompilación de una sola línea de código existente en `SelectorCalculadora` ni en el servicio.

### Decisión de Diseño 3: Capas simples para persistencia y desacoplamiento en notificación
* **Contexto:** Evaluar si se justifica migrar la arquitectura completa a Puertos y Adaptadores (Arquitectura Hexagonal) o mantenerse en capas simples.
* **Elección técnica:** Mantener capas simples directas en persistencia (`SolicitudReembolsoService` depende de `RepositorioSolicitudesJpa`) y preservar la interfaz `ObservadorCambioEstado` como seam para la notificación.
* **Justificación:** Detallada en el registro [ADR-001](docs/adr/ADR-001.md). No existe plan de cambiar el motor de base de datos relacional (YAGNI), mientras que el cambio confirmado para el próximo trimestre (llamada a API externa de pagos en Contabilidad) ya queda encapsulado limpiamente dentro de `NotificadorContabilidad` sin tocar el núcleo de la aplicación.

### Decisión de Diseño 4: Alcance y granularidad de la Regla 1 de ArchUnit
* **Contexto:** Determinar si la regla de aislamiento arquitectónico de `procesamiento` debe prohibir cualquier import de `org.springframework` o distinguir entre estereotipos y clases de infraestructura.
* **Elección técnica:** La Regla 1 prohíbe `org.springframework.data..`, `jakarta.persistence..`, `..infraestructura..` y `..adaptadores..`, pero tolera `@Component` de Spring.
* **Justificación:** Las anotaciones de estereotipo como `@Component` representan metadatos declarativos de ciclo de vida que no comprometen la lógica de negocio ni atan el procesamiento a bases de datos o capas web. Prohibir todo Spring forzaría la creación de fábricas manuales redundantes o clases de configuración verbosas (accidental complexity) para instanciar la cadena, las estrategias y los observadores, sin aportar ningún beneficio real de desacoplamiento tecnológico.

---

## 3. Resumen de Decisión Arquitectónica (ADR-001)

El documento formal [docs/adr/ADR-001.md](docs/adr/ADR-001.md) establece:
* **Estado:** Aceptado — 2026-10-10.
* **Decisión:** Persistencia en capas simples sin puerto intermediario por ausencia de variabilidad tecnológica en la base de datos (YAGNI). Se conserva el seam de `ObservadorCambioEstado` para absorber el cambio anunciado por Contabilidad (integración de API bancaria externa).
* **Compromiso técnico:** Se asume conscientemente la deuda técnica de introducir el puerto de persistencia de manera diferida si en el futuro imprevisto surgiera la necesidad de migrar el motor de almacenamiento.

---

## 4. Qué se descartó y por qué (Análisis de Impacto y Prevención de Sobreingeniería)

Durante el ciclo de desarrollo se evaluaron alternativas técnicas de mayor sofisticación arquitectónica que fueron explícitamente descartadas bajo criterios objetivos de necesidad demostrada (YAGNI, Rule of Three y Test de Explicabilidad):

### 1. Spring ApplicationEventPublisher en vez de una lista de observadores inyectada
* **Qué se consideró:** Publicar eventos de dominio mediante `ApplicationEventPublisher` y procesarlos de forma reflexiva y asíncrona con `@EventListener`.
* **Por qué se descartó:** Con exactamente tres observadores fijos y conocidos en tiempo de compilación, introducir un bus de eventos genérico añade indirección y eleva drásticamente el costo de depuración y navegación de código sin aportar desacoplamiento real. La lista inyectada tipada `List<ObservadorCambioEstado>` ofrece claridad, tipado estático y predictibilidad sin la sobrecarga de un bus de eventos.
* **Señal futura para reconsiderar:** Si se requiere procesamiento asíncrono no bloqueante en hilos secundarios para tareas pesadas, persistencia de eventos (Event Sourcing) o distribución hacia brokers externos como RabbitMQ o Apache Kafka.

### 2. Visitor o Abstract Factory para exportación de reportes en múltiples formatos
* **Qué se consideró:** Crear una familia de fábricas abstractas o una estructura Visitor anticipando solicitudes de Contabilidad para exportar reportes en PDF, Excel y JSON además del CSV actual.
* **Por qué se descartó:** Hoy solo existe el requisito de exportación en CSV. No se cumple la *Rule of Three* de la literatura de diseño. Construir una jerarquía anticipada de exportadores generaría clases vacías y abstracciones especulativas (*Speculative Generality*).
* **Señal futura para reconsiderar:** Cuando se formalicen contractualmente al menos dos requerimientos adicionales de formatos con esquemas de conversión dispares.

### 3. Jerarquía de excepciones tipadas por cada validador (Aporte propio del estudiante)
* **Qué se consideró:** Crear una jerarquía de excepciones especializadas (`DocumentosSoporteFaltantesException`, `MontoExcedidoException`, `SolicitudDuplicadaException`) con manejadores `@ExceptionHandler` independientes para cada error de validación.
* **Por qué se descartó:** Desde el punto de vista del protocolo HTTP y de la experiencia de usuario, toda validación fallida representa semánticamente un error de cliente HTTP 400 Bad Request acompañado de un mensaje claro de rechazo. Multiplicar las clases de excepción introduce sobreingeniería estructural y código boilerplate sin aportar comportamiento diferenciado. Una única excepción `SolicitudInvalidaException` que transporta el motivo provisto por el record `ResultadoValidacion` resuelve el problema con la máxima simplicidad (KISS y YAGNI).
* **Señal futura para reconsiderar:** Si los clientes de la API (como un frontend SPA o integradores móviles) requieren códigos de error canónicos estructurados con metadatos de máquina (por ejemplo, URLs de remediación de carga documental en caso de soportes faltantes, o enlaces a la solicitud duplicada existente para su visualización).

### 4. Patrón State para el ciclo de vida de la solicitud (Aporte propio del estudiante)
* **Qué se consideró:** Modelar una máquina de estados mediante el patrón State (`EstadoSolicitudState`, `EstadoPendiente`, `EstadoAprobada`, `EstadoRechazada`) para encapsular las transiciones de estado y comportamientos asociados.
* **Por qué se descartó:** Las transiciones de estado del sistema son lineales, directas y sin ramificaciones cíclicas complejas: de `PENDIENTE` pasa a `APROBADA`, `RECHAZADA` o `REQUIERE_CORRECCION`. El comportamiento asociado a cada cambio (notificación) ya está resuelto limpiamente por el patrón Observer. Implementar State añadiría al menos cinco clases y métodos de delegación sin que exista lógica condicional compleja en las entidades, constituyendo el antipatrón Golden Hammer.
* **Señal futura para reconsiderar:** Si la organización implementa un flujo de aprobación colegiado multinivel (por ejemplo, pre-aprobación por supervisor, validación presupuestal por gerencia, aprobación final por dirección y bloqueo temporal con reapertura bajo condiciones de auditoría).