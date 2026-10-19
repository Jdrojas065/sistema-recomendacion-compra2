markdown
# Sistema de Recomendación de Compra

Sistema web para gestión de inventario, ventas y recomendaciones de compra basadas en demanda proyectada, rotación de stock y detección de anomalías.

## Stack técnico

- Java 21
- Spring Boot 4.1.1 (Spring MVC + Thymeleaf)
- Spring Security con autenticación MFA (TOTP)
- Spring Data JPA
- MySQL (producción) / H2 en memoria (desarrollo)
- Maven

## Funcionalidades

- Gestión de productos, categorías e inventario (lotes, movimientos)
- Registro de ventas
- Predicción de demanda
- Recomendaciones de compra automáticas (prioridad, riesgo de quiebre de stock)
- Detección de anomalías en ventas
- Análisis de rotación de productos
- Autenticación de dos factores (MFA) con códigos de recuperación

## Cómo correr el proyecto localmente

1. Clona el repositorio:

git clone https://github.com/Jdrojas065/sistema-recomendacion-compra2.git


2. Configura las variables de entorno necesarias (perfil `prod`, conexión a MySQL):

   | Variable | Descripción |
   |---|---|
   | `DB_URL` | URL JDBC de la base de datos MySQL |
   | `DB_USERNAME` | Usuario de la base de datos |
   | `DB_PASSWORD` | Contraseña de la base de datos |
   | `ML_SERVICE_URL` | URL del servicio de Machine Learning |

   Para desarrollo local rápido sin MySQL, se puede correr con el perfil `dev` por defecto, que usa una base H2 en memoria con datos de prueba.

3. Compila y corre:

./mvnw clean package
./mvnw spring-boot:run

   O con el perfil de producción:

java -jar target/sistema-recomendacion-compra2-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod


4. La app corre sobre HTTPS en `https://localhost:8080` (certificado autofirmado para desarrollo).

## Usuarios de prueba (perfil dev)

- `admin@demo.com` — rol ADMIN
- `empleado@demo.com` — rol EMPLOYEE

## Tests

./mvnw test


## Estructura del proyectos

src/main/java/com/example/recomendation/
├── config/ # Configuración de Spring (seguridad, app)
├── controller/ # Controladores MVC
├── exception/ # Manejo global de excepciones
├── integration/ml/ # Cliente del servicio de Machine Learning
├── model/entity/ # Entidades JPA
├── repository/ # Repositorios Spring Data
├── security/ # Autenticación MFA
├── seed/ # Datos de prueba (perfil dev)
└── service/ # Lógica de negocio