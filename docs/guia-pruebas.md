# Guia de Pruebas y Verificacion del Sistema

Esta guia detalla los comandos y peticiones HTTP para ejecutar y verificar cada checkpoint del Sistema de Solicitudes de Reembolso de Gastos Corporativos.

---

## 1. Ejecucion de la Suite de Pruebas Automatizadas

### Pruebas Unitarias y de Integracion
```powershell
# Windows PowerShell
mvn test
```
```bash
# Bash / Linux / macOS
mvn test
```
* **Resultado esperado:** 27 pruebas ejecutadas, 0 fallos, 0 errores (`BUILD SUCCESS`).

### Reglas de Arquitectura con ArchUnit
```powershell
# Windows PowerShell
mvn test -Dtest=ReglasArquitectura
```
```bash
# Bash / Linux / macOS
mvn test -Dtest=ReglasArquitectura
```
* **Resultado esperado:** 4 pruebas ejecutadas (`procesamientoSinPersistenciaNiWeb`, `controladorSoloUsaServicio`, `calculadorasImplementanInterfaz`, `notificadoresImplementanInterfaz`), 0 fallos (`BUILD SUCCESS`).

---

## 2. Verificacion de Endpoints REST (Aplicacion en Ejecucion)

Iniciar la aplicacion previamente:
```powershell
mvn spring-boot:run
# O bien: java -jar target/reembolsos-integrado-0.0.1-SNAPSHOT.jar
```

### Caso 1: Creacion de Solicitud de Viaticos (VIAJE)
* **Comando PowerShell:**
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/solicitudes -ContentType 'application/json' -Body '{
  "empleadoId": "EMP-VIAJE-01",
  "categoria": "VIAJE",
  "montoSolicitado": 120000,
  "fechaGasto": "2026-10-12",
  "diasViaje": 2,
  "documentosSoporte": true
}'
```
* **Comando cURL (Bash):**
```bash
curl -X POST http://localhost:8080/api/solicitudes \
  -H "Content-Type: application/json" \
  -d '{"empleadoId":"EMP-VIAJE-01","categoria":"VIAJE","montoSolicitado":120000,"fechaGasto":"2026-10-12","diasViaje":2,"documentosSoporte":true}'
```
* **Respuesta esperada (HTTP 200 OK):**
```json
{
  "id": 1,
  "empleadoId": "EMP-VIAJE-01",
  "categoria": "VIAJE",
  "montoSolicitado": 120000,
  "montoAprobado": 120000,
  "fechaGasto": "2026-10-12",
  "diasViaje": 2,
  "documentosSoporte": true,
  "estado": "PENDIENTE"
}
```

### Caso 2: Creacion de Solicitud de Gastos de Representacion
* **Comando PowerShell:**
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/solicitudes -ContentType 'application/json' -Body '{
  "empleadoId": "EMP-REP-02",
  "categoria": "REPRESENTACION",
  "montoSolicitado": 2500000,
  "fechaGasto": "2026-10-15",
  "diasViaje": 0,
  "documentosSoporte": true
}'
```
* **Respuesta esperada (HTTP 200 OK):**
```json
{
  "id": 2,
  "empleadoId": "EMP-REP-02",
  "categoria": "REPRESENTACION",
  "montoSolicitado": 2500000,
  "montoAprobado": 2000000,
  "fechaGasto": "2026-10-15",
  "diasViaje": 0,
  "documentosSoporte": true,
  "estado": "PENDIENTE"
}
```
*(Nota: Monto aprobado ajustado al tope mensual de $2,000,000).*

### Caso 3: Creacion de Solicitud de Capacitacion
* **Comando PowerShell:**
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/solicitudes -ContentType 'application/json' -Body '{
  "empleadoId": "EMP-CAP-03",
  "categoria": "CAPACITACION",
  "montoSolicitado": 2500000,
  "fechaGasto": "2026-10-20",
  "diasViaje": 0,
  "documentosSoporte": true
}'
```
* **Respuesta esperada (HTTP 200 OK):**
```json
{
  "id": 3,
  "empleadoId": "EMP-CAP-03",
  "categoria": "CAPACITACION",
  "montoSolicitado": 2500000,
  "montoAprobado": 2000000.0,
  "fechaGasto": "2026-10-20",
  "diasViaje": 0,
  "documentosSoporte": true,
  "estado": "PENDIENTE"
}
```
*(Nota: Reembolso del 80% de $2,500,000 = $2,000,000, respetando el tope anual de $3,000,000).*

### Caso 4: Rechazo por Falta de Documentos Soporte
* **Comando PowerShell:**
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/solicitudes -ContentType 'application/json' -Body '{
  "empleadoId": "EMP-SINDOC-04",
  "categoria": "VIAJE",
  "montoSolicitado": 100000,
  "fechaGasto": "2026-10-18",
  "diasViaje": 1,
  "documentosSoporte": false
}'
```
* **Respuesta esperada (HTTP 400 Bad Request):**
```json
{
  "mensaje": "Faltan documentos soporte"
}
```

### Caso 5: Rechazo por Solicitud Duplicada en el Periodo
* **Comando PowerShell:**
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/solicitudes -ContentType 'application/json' -Body '{
  "empleadoId": "EMP-VIAJE-01",
  "categoria": "VIAJE",
  "montoSolicitado": 90000,
  "fechaGasto": "2026-10-28",
  "diasViaje": 1,
  "documentosSoporte": true
}'
```
* **Respuesta esperada (HTTP 400 Bad Request):**
```json
{
  "mensaje": "Ya existe una solicitud pendiente de la misma categoria en el periodo"
}
```

### Caso 6: Transicion de Estado a APROBADA (Disparo de Observadores)
* **Comando PowerShell:**
```powershell
Invoke-RestMethod -Method Put -Uri http://localhost:8080/api/solicitudes/1/estado -ContentType 'application/json' -Body '"APROBADA"'
```
* **Respuesta esperada (HTTP 200 OK):**
```json
{
  "id": 1,
  "empleadoId": "EMP-VIAJE-01",
  "categoria": "VIAJE",
  "montoSolicitado": 120000,
  "montoAprobado": 120000,
  "fechaGasto": "2026-10-12",
  "diasViaje": 2,
  "documentosSoporte": true,
  "estado": "APROBADA"
}
```
* **Salida simultanea en consola de la aplicacion:**
```text
Auditoria: solicitud 1 cambio de PENDIENTE a APROBADA
Contabilidad: programar pago de 120000.00 para EMP-VIAJE-01
Notificando a EMP-VIAJE-01: su solicitud paso de PENDIENTE a APROBADA
```
