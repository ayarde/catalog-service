# Reglas para Unit Tests e Integration Tests

## 📋 Resumen Ejecutivo

Este documento establece las reglas y mejores prácticas para la creación y mantenimiento de **unit tests** e **integration tests** en el proyecto `opennavent-realestate-cross`.

---

## 🏗️ Estructura de Tests

### **Given/When/Then Pattern**

Se **DEBE** mantener la estructura **givven/when/then** al codificar cualquier test:

```java
@Test
public void methodUnderTestingName_StateUnderTest() {
    //given
    // Definición y asignación de variables
    // Configuración de mocks y stubbing
    
    //when
    // Invocación del método bajo test para la instancia bajo test (NO DEBE SER UN MOCK)
    
    //then
    // Asserts y verifies
}
```

**Ejemplo práctico:**
```java
/**
En esta prueba unitaria se espera que se agregue un detalle de error a un registro existente.

Características extras:
- El registro ya existe en BigQuery
- Se proporciona una URL válida y motivo de error

Se espera que el método:
- Agregue el error al array detalles_error
- Mantenga los errores existentes
- Actualice el timestamp de última actualización
**/
@Test
public void mergeMultimediaTraceRecord_Success() {
    //given
    String correlationId = "test-correlation-id";
    MultimediaTraceRecord record = new MultimediaTraceRecord(correlationId, "123", "INICIADO");
    
    //when
    repository.mergeMultimediaTraceRecord(record);
    
    //then
    assertTrue("Debería existir el registro", repository.existsByCorrelationId(correlationId));
}
```

---

## 📝 Naming Convention

### **Formato: `methodUnderTestingName_StateUnderTest`**

#### **Componentes:**

1. **`methodUnderTestingName`**: 
   - El nombre del método que estamos testeando
   - Debe escribirse exactamente como está en la clase bajo testing
   - Ejemplo: `mergeMultimediaTraceRecord`, `addErrorDetail`, `updateEstado`

2. **`StateUnderTest`**: 
   - El caso específico que se está testeando
   - Puede ser una condición, estado de parámetros, o estado de la clase
   - Ejemplo: `Success`, `WithErrorDetails`, `NotFound`, `InvalidCredentials`

#### **Ejemplos válidos:**
```java
@Test
public void mergeMultimediaTraceRecord_Success() { }
@Test
public void addErrorDetail_WithValidUrl() { }
@Test
public void updateEstado_ToError() { }
@Test
public void findByCorrelationId_NotFound() { }
@Test
public void existsByCorrelationId_ReturnsTrue() { }
```

---

## 🌐 Idioma

### **Regla Principal:**
- **Nombres de métodos**: **DEBEN** estar escritos en **Inglés**
- **Comentarios (Javadoc)**: **DEBEN** estar escritos en **Español**
- **Variables y constantes**: Pueden estar en inglés o español según el contexto

### **Ejemplo de aplicación:**
```java
/**
En esta prueba unitaria se espera que se agregue un detalle de error a un registro existente.

Características extras:
- El registro ya existe en BigQuery
- Se proporciona una URL válida y motivo de error

Se espera que el método:
- Agregue el error al array detalles_error
- Mantenga los errores existentes
- Actualice el timestamp de última actualización
**/
@Test
public void addErrorDetail_WithValidUrl() {
    //given
    String correlationId = "test-correlation-id";
    String urlMedia = "http://example.com/image.jpg";
    String motivoError = "Timeout al descargar imagen";
    
    //when
    repository.addErrorDetail(correlationId, urlMedia, motivoError);
    
    //then
    assertTrue("Debería haberse llamado addErrorDetail", testRepository.addErrorDetailCalled);
}
```

### **Migración de métodos existentes en español:**
Si existen métodos de test en español, seguir este patrón:

```java
/**
@deprecated Este método ya no es aceptado para nuevas versiones.
Usar {@link #methodInEnglish()}.
**/
@Test
public void metodoEspañol_EstadoDePrueba() {
    return this.methodInEnglish_StateUnderTest();
}

/**
Misma explicación anterior, sin deprecated
**/
@Test
public void methodInEnglish_StateUnderTest() {
    // Lógica que estaba en el método en español
}
```

---

## 💬 Comentarios (Javadoc)

### **Regla de idioma:**
- **TODOS** los comentarios **DEBEN** estar escritos en **Español**
- Esto incluye Javadoc, comentarios inline y mensajes de assert

### **Estructura de comentarios:**

```java
/**
En esta prueba unitaria se espera que [descripción del escenario principal].

Características extras:
- [Característica específica 1]
- [Característica específica 2]

Se espera que el método:
- [Comportamiento esperado 1]
- [Comportamiento esperado 2]
- [Comportamiento esperado 3]
**/
@Test
public void methodUnderTestingName_StateUnderTest() {
    //given
    // Configuración inicial
    
    //when
    // Ejecución del método
    
    //then
    // Validaciones
}
```

### **Ejemplo práctico:**
```java
/**
En esta prueba unitaria se espera que se agregue un detalle de error a un registro existente.

Características extras:
- El registro ya existe en BigQuery
- Se proporciona una URL válida y motivo de error

Se espera que el método:
- Agregue el error al array detalles_error
- Mantenga los errores existentes
- Actualice el timestamp de última actualización
**/
@Test
public void addErrorDetail_WithValidUrl() {
    //given
    String correlationId = "test-correlation-id";
    String urlMedia = "http://example.com/image.jpg";
    String motivoError = "Timeout al descargar imagen";
    
    //when
    repository.addErrorDetail(correlationId, urlMedia, motivoError);
    
    //then
    assertTrue("Debería haberse llamado addErrorDetail", testRepository.addErrorDetailCalled);
}
```

---

## ⚠️ Manejo de Excepciones

### **Regla 1: Tests que esperan excepciones específicas**
Usar `@Test(expected = ExceptionClass.class)`:

```java
/**
En esta prueba se espera que se lance una excepción cuando se proporciona un projectId inválido.

Características extras:
- Se proporciona un projectId que no existe
- El dataset tampoco existe

Se espera que el método:
- Lance MultimediaTraceException
- No cree ningún registro en BigQuery
**/
@Test(expected = MultimediaTraceException.class)
public void mergeMultimediaTraceRecord_InvalidProjectId() {
    //given
    MultimediaTraceRecord record = new MultimediaTraceRecord("test", "123", "INICIADO");
    
    //when
    repository.mergeMultimediaTraceRecord(record); // Debe lanzar MultimediaTraceException
}
```

### **Regla 2: Evaluar comportamiento con excepciones**
Usar `try-catch` para validar excepciones y efectos secundarios:

```java
/**
En esta prueba se valida el comportamiento cuando hay una actualización concurrente.

Características extras:
- Se simula una condición de carrera
- El sistema debe detectar la concurrencia

Se espera que el método:
- Lance MultimediaTraceException con tipo CONCURRENCY_ERROR
- Incluya mensaje de retry en la excepción
**/
@Test
public void addErrorDetail_ConcurrentUpdate_RetriesSuccessfully() {
    //given
    String correlationId = "test-correlation-id";
    
    //when
    try {
        repository.addErrorDetail(correlationId, "http://example.com/image.jpg", "Error");
        fail("Debería haber lanzado una excepción");
    } catch (MultimediaTraceException e) {
        //then
        assertTrue("Debería contener mensaje de retry", e.getMessage().contains("retry"));
        assertTrue("Debería ser tipo CONCURRENCY_ERROR", e.getErrorType() == ErrorType.CONCURRENCY_ERROR);
    }
}
```

---

## 🏗️ Organización de Métodos

### **Orden de métodos:**
1. **Métodos públicos** (tests) - **DEBEN ir primero**
2. **Métodos privados** (agrupados con editor-fold) - **DEBEN ir al final, después de todos los métodos públicos**

### **Regla importante:**
Los métodos privados usados en unit tests e integration tests **DEBEN** agregarse al final de todos los métodos públicos que son unit tests o integration tests.

### **Agrupación de métodos privados:**
```java
/**
En esta prueba se valida que se pueda crear un registro multimedia exitosamente.

Características extras:
- Se crea un registro con datos válidos
- Se persiste en BigQuery

Se espera que el método:
- Cree el registro correctamente
- Se pueda recuperar después de la creación
**/
@Test
public void mergeMultimediaTraceRecord_Success() {
    //given
    // Configuración inicial
    
    //when
    // Ejecución del método
    
    //then
    // Validaciones
}

/**
En esta prueba se valida otro escenario.
**/
@Test
public void anotherTest_AnotherState() {
    //given
    // Configuración inicial
    
    //when
    // Ejecución del método
    
    //then
    // Validaciones
}

//<editor-fold desc="Métodos auxiliares">
private MultimediaTraceRecord createTestRecord(String correlationId) {
    return new MultimediaTraceRecord(correlationId, "123", "INICIADO");
}

private void assertRecordExists(String correlationId) {
    assertTrue("Debería existir el registro", repository.existsByCorrelationId(correlationId));
}
//</editor-fold>
```

---

## 🧪 Tipos de Tests

### **Unit Tests**
- **Ubicación**: `src/test/java/`
- **Propósito**: Probar lógica de negocio aislada
- **Dependencias**: Usar mocks/stubs para dependencias externas
- **Ejemplo**: `MultimediaTraceServiceImplTest`

### **Integration Tests**
- **Ubicación**: `src/intTest/java/`
- **Propósito**: Probar integración con sistemas externos
- **Dependencias**: Conectar con servicios reales (BigQuery, Pub/Sub)
- **Ejemplo**: `MultimediaTraceRepositoryImplIntTest`

---

## 🔧 Configuración de Tests

### **Unit Tests - Stub Pattern:**
```java
public class MultimediaTraceRepositoryImplTest {
    
    private TestMultimediaTraceRepository testRepository;
    
    @Before
    public void setUp() {
        testRepository = new TestMultimediaTraceRepository();
        service = new MultimediaTraceServiceImpl(testRepository);
    }
    
    //<editor-fold desc="Stub implementation">
    private static class TestMultimediaTraceRepository implements MultimediaTraceRepository {
        public boolean mergeCalled = false;
        
        @Override
        public void mergeMultimediaTraceRecord(MultimediaTraceRecord record) {
            mergeCalled = true;
        }
    }
    //</editor-fold>
}
```

### **Integration Tests - Credenciales reales:**
```java
public class MultimediaTraceRepositoryImplIntTest {
    
    @Before
    public void setUp() throws IOException {
        // Usar credenciales explícitas para el test
        String credentialsPath = "src/intTest/resources/keys-snd-PubSub-IntTest.json";
        ServiceAccountCredentials credentials = ServiceAccountCredentials
            .fromStream(new FileInputStream(credentialsPath));
        
        BigQuery bigQuery = BigQueryOptions.newBuilder()
            .setProjectId(PROJECT_ID)
            .setCredentials(credentials)
            .build()
            .getService();
        
        repository = new MultimediaTraceRepositoryImpl(PROJECT_ID, DATASET_ID, bigQuery);
    }
}
```

---

## 📚 Consejos del Libro: Unit Testing Principles, Practices, and Patterns (2020)

### **Autor:** Vladimir Khorikov

### **Consejos sobre Naming Conventions:**

#### **🎯 Principio Fundamental:**
> **"Don't follow a rigid naming policy. You simply can't fit a high-level description of a complex behavior into the narrow box of such a policy. Allow freedom of expression."**

#### **📝 Reglas Específicas:**

1. **Descripción Natural:**
   > **"Name the test as if you were describing the scenario to a non-programmer who is familiar with the problem domain. A domain expert or a business analyst is a good example."**

2. **Separación con Underscores:**
   > **"Separate words with underscores. Doing so helps improve readability, especially in long names."**

#### **🔄 Aplicación en Nuestro Proyecto:**

Estos consejos **COMPLEMENTAN** nuestras reglas existentes:

- **✅ Mantener**: Formato `methodUnderTestingName_StateUnderTest`
- **✅ Agregar**: Libertad para descripciones más naturales cuando sea necesario
- **✅ Mejorar**: Usar underscores para separar palabras en nombres largos

#### **📖 Ejemplo de Aplicación:**

```java
/**
En esta prueba se valida que cuando un usuario intenta procesar una imagen multimedia
que ya ha sido procesada anteriormente, el sistema debe detectar la duplicación
y actualizar el contador de intentos sin crear un nuevo registro.

Características extras:
- La imagen ya existe en el sistema
- Se intenta procesar nuevamente

Se espera que el método:
- Detecte la duplicación automáticamente
- Actualice el contador de intentos
- No cree un nuevo registro
**/
@Test
public void processMultimediaImage_AlreadyProcessedImage_UpdatesAttemptCounter() {
    //given
    // Configuración del escenario de duplicación
    
    //when
    // Intento de procesar imagen duplicada
    
    //then
    // Validación de actualización de contador
}
```

---

## ✅ Checklist de Validación

### **Antes de crear un test, verificar:**

- [ ] **Naming**: ¿Sigue el formato `methodUnderTestingName_StateUnderTest`?
- [ ] **Idioma**: ¿Nombres de métodos en inglés y comentarios en español?
- [ ] **Estructura**: ¿Sigue el patrón `//given`, `//when`, `//then`?
- [ ] **Comentarios**: ¿Tiene Javadoc en español explicando el escenario?
- [ ] **Excepciones**: ¿Usa el patrón correcto para manejo de excepciones?
- [ ] **Organización**: ¿Los métodos privados están agrupados con editor-fold y ubicados al final, después de todos los métodos públicos?
- [ ] **Tipo correcto**: ¿Es unit test (src/test) o integration test (src/intTest)?
- [ ] **Descripción natural**: ¿El nombre describe el escenario como lo explicarías a un experto del dominio?

### **Antes de hacer commit:**

- [ ] **Todos los tests pasan**: `./gradlew test intTest -DdoIntegrationTest=true`
- [ ] **Integration tests ejecutados**: ¿Se ejecutaron los integration tests con `-DdoIntegrationTest=true`?
- [ ] **Cobertura adecuada**: Al menos 80% para unit tests
- [ ] **Sin warnings**: Resolver todos los warnings de compilación
- [ ] **Documentación**: Comentarios claros y útiles

---

## 🚀 Ejecución de Tests

### **Unit Tests:**
```bash
./gradlew :opennavent-realestate-wnats:test
```

### **Integration Tests:**
**⚠️ IMPORTANTE**: Los integration tests **SIEMPRE** deben ejecutarse con el flag `-DdoIntegrationTest=true`. Sin este flag, los integration tests no se ejecutarán.

```bash
./gradlew :opennavent-realestate-wnats:integrationTest -DdoIntegrationTest=true
```

**Regla obligatoria:**
- Los integration tests **DEBEN** ejecutarse siempre con `-DdoIntegrationTest=true`
- Este flag es **REQUERIDO** para que los integration tests se ejecuten correctamente
- Sin este flag, los integration tests serán ignorados o no se ejecutarán

### **Todos los tests:**
```bash
./gradlew :opennavent-realestate-wnats:test intTest -DdoIntegrationTest=true
```

---

## 📚 Referencias

### **Bibliografía Principal:**
- **Khorikov, V., 2020. Unit Testing Principles, Practices, and Patterns. New York: Manning Publications Co. LLC.**

### **Recursos Adicionales:**
- **JUnit 4**: Framework de testing
- **Mockito**: Para mocks (cuando esté disponible)
- **BigQuery**: Para integration tests
- **Google Cloud**: Credenciales para servicios externos
- **7 Popular Unit Test Naming Conventions and Best Practices**: https://methodpoet.com/unit-test-method-naming-convention/
- **Making Better Unit Tests**: https://freecontent.manning.com/making-better-unit-tests/

---

*Última actualización: Agosto 2025* 