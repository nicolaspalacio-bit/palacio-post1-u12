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