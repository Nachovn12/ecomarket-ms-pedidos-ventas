# MS Pedidos y Ventas

Microservicio responsable del flujo comercial de EcoMarket SPA: carrito, pedidos, ventas presenciales, pagos, facturas, cupones, devoluciones y reclamaciones.

## Responsable

| Campo                 | Detalle                     |
| --------------------- | --------------------------- |
| Responsable principal | Benjamín Flores             |
| Rama de trabajo       | `feature/ms-pedidos-ventas` |
| Base de datos         | `bd_ventas`                 |
| Puerto local          | `8086`                      |
| URL base local        | `http://localhost:8086`     |

## Que hace

- Administra carritos de compra y sus items.
- Aplica cupones de descuento.
- Crea pedidos desde carrito.
- Consulta estado e historial de pedidos.
- Registra ventas presenciales.
- Genera facturas asociadas a ventas.
- Gestiona devoluciones y reclamaciones.
- Expone respuestas REST con validaciones, manejo de errores.

## Tecnologias

- Java 25
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- MySQL
- Maven
- JUnit

## Estructura CSR

- `controller`: expone endpoints REST.
- `service`: concentra reglas de negocio y validaciones del dominio.
- `repository`: encapsula el acceso a datos con Spring Data JPA.
- `model`: contiene las clases persistentes JPA (`@Entity`, `@Table`, `@Id`).
- `dto`: define contratos de entrada y salida de la API.

## Configuracion

El archivo principal de configuracion esta en:

```text
src/main/resources/application.properties
```

Valores principales:

```properties
spring.application.name=ms-pedidos-ventas
server.port=8086
spring.datasource.url=${VENTAS_DB_URL:jdbc:mysql://localhost:3306/bd_ventas?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
```

Antes de ejecutar, crear o verificar la base de datos:

```sql
CREATE DATABASE IF NOT EXISTS bd_ventas
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

## Como ejecutar

Desde la raiz de este repositorio:

```powershell
.\mvnw.cmd spring-boot:run
```

## Como probar

```powershell
.\mvnw.cmd clean test
```

## Endpoints principales

| Metodo | Ruta                                               | Uso                              |
| ------ | -------------------------------------------------- | -------------------------------- |
| POST   | `/api/pedidos/carritos`                            | Crear carrito                    |
| GET    | `/api/pedidos/carritos`                            | Listar carritos                  |
| GET    | `/api/pedidos/carritos/{idCarrito}`                | Consultar carrito                |
| POST   | `/api/pedidos/carritos/{idCarrito}/items`          | Agregar item al carrito          |
| PUT    | `/api/pedidos/carritos/{idCarrito}/items/{idItem}` | Actualizar cantidad              |
| DELETE | `/api/pedidos/carritos/{idCarrito}/items/{idItem}` | Eliminar item                    |
| POST   | `/api/pedidos/carritos/{idCarrito}/cupon`          | Aplicar cupon                    |
| POST   | `/api/pedidos/desde-carrito/{idCarrito}`           | Crear pedido desde carrito       |
| GET    | `/api/pedidos`                                     | Listar pedidos                   |
| GET    | `/api/pedidos/{idPedido}`                          | Consultar pedido                 |
| GET    | `/api/pedidos/{idPedido}/estado`                   | Consultar estado                 |
| GET    | `/api/pedidos/clientes/{idCliente}/historial`      | Historial de cliente             |
| PATCH  | `/api/pedidos/{idPedido}/cancelar`                 | Cancelar pedido                  |
| POST   | `/api/ventas/presencial`                           | Registrar venta presencial       |
| GET    | `/api/ventas`                                      | Listar ventas                    |
| GET    | `/api/ventas/{idVenta}`                            | Consultar venta                  |
| POST   | `/api/ventas/{idVenta}/factura`                    | Generar factura                  |
| POST   | `/api/ventas/{idVenta}/devoluciones`               | Registrar devolucion             |
| PATCH  | `/api/devoluciones/{id}/estado`                    | Actualizar estado de devolucion  |
| POST   | `/api/reclamaciones`                               | Registrar reclamacion            |
| PATCH  | `/api/reclamaciones/{id}/estado`                   | Actualizar estado de reclamacion |
| POST   | `/api/pedidos/cupones`                             | Crear cupon                      |

## Ejemplo de uso

Crear pedido desde carrito:

```http
POST http://localhost:8086/api/pedidos/desde-carrito/1
```

Consultar historial de cliente:

```http
GET http://localhost:8086/api/pedidos/clientes/1/historial
```

## Diagramas

### Casos de uso

![Casos de uso MS Pedidos y Ventas](https://raw.githubusercontent.com/Nachovn12/ecomarket-spa-docs/main/docs/diagramas/casos-uso/diagrama-casos-uso-ms-pedidos-ventas.png)

### Diagrama de clases

![Diagrama de clases MS Pedidos y Ventas](https://raw.githubusercontent.com/Nachovn12/ecomarket-spa-docs/main/docs/diagramas/clases/diagrama-clases-ms-pedidos-ventas.png)

## Documentacion relacionada

- [Evidencia Postman](https://github.com/Nachovn12/ecomarket-spa-docs/blob/main/docs/postman/evidencia-postman.md)
- [Evidencia build y tests](https://github.com/Nachovn12/ecomarket-spa-docs/blob/main/docs/evidencias/evidencia-build-tests.md)
- [Comunicacion REST entre servicios](https://github.com/Nachovn12/ecomarket-spa-docs/blob/main/docs/integracion/comunicacion-rest-entre-servicios.md)
- [Arquitectura de microservicios](https://github.com/Nachovn12/ecomarket-spa-docs/blob/main/docs/arquitectura/arquitectura-microservicios.md)
- [Repositorio de documentacion](https://github.com/Nachovn12/ecomarket-spa-docs)
