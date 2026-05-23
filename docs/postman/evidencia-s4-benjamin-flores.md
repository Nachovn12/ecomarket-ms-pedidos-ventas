# Evidencia Postman — Sprint S4 — Benjamín Flores Arévalo

## Endpoint 1

- **HU:** HU-12
- **Método:** GET
- **URL:** `http://localhost:8084/api/pedidos/{idPedido}/estado`
- **Body:** N/A
- **Código esperado:** 200
- **Código obtenido:** 200
- **Resultado:** OK — retorna estado actual del pedido.

## Endpoint 2

- **HU:** HU-12
- **Método:** GET
- **URL:** `http://localhost:8084/api/pedidos/clientes/{idCliente}/historial`
- **Body:** N/A
- **Código esperado:** 200
- **Código obtenido:** 200
- **Resultado:** OK — retorna lista de pedidos del cliente.

## Endpoint 3

- **HU:** HU-12
- **Método:** GET
- **URL:** `http://localhost:8084/api/pedidos/{idPedido}/historial`
- **Body:** N/A
- **Código esperado:** 200
- **Código obtenido:** 200
- **Resultado:** OK — retorna historial de estados del pedido.

## Endpoint 4

- **HU:** HU-15
- **Método:** POST
- **URL:** `http://localhost:8084/api/ventas/{idVenta}/devoluciones`
- **Body:**

```json
{
  "idCliente": 1,
  "motivo": "Producto defectuoso"
}
```

- **Código esperado:** 201
- **Código obtenido:** 201
- **Resultado:** OK — devolución registrada correctamente.

## Endpoint 5

- **HU:** HU-15
- **Método:** PATCH
- **URL:** `http://localhost:8084/api/devoluciones/{id}/estado`
- **Body:**

```json
{
  "estado": "APROBADA"
}
```

- **Código esperado:** 200
- **Código obtenido:** 200
- **Resultado:** OK — estado de devolución actualizado.

## Endpoint 6

- **HU:** HU-15
- **Método:** POST
- **URL:** `http://localhost:8084/api/reclamaciones`
- **Body:**

```json
{
  "idCliente": 1,
  "idPedido": 1,
  "motivo": "Pedido incompleto",
  "descripcion": "Faltaron 2 productos"
}
```

- **Código esperado:** 201
- **Código obtenido:** 201
- **Resultado:** OK — reclamación registrada correctamente.

## Endpoint 7

- **HU:** HU-15
- **Método:** PATCH
- **URL:** `http://localhost:8084/api/reclamaciones/{id}/estado`
- **Body:**

```json
{
  "estado": "CERRADA"
}
```

- **Código esperado:** 200
- **Código obtenido:** 200
- **Resultado:** OK — estado de reclamación actualizado.

## Endpoint 8 — Caso de error

- **HU:** HU-15
- **Método:** PATCH
- **URL:** `http://localhost:8084/api/devoluciones/1/estado`
- **Body:**

```json
{
  "estado": "INVALIDO"
}
```

- **Código esperado:** 400
- **Código obtenido:** 400
- **Resultado:** Error 400 — estado de devolución inválido.

## Nota para defensa

Las capturas reales de Postman deben adjuntarse en Miro o en la carpeta de evidencias del equipo para respaldar la ejecución de estos endpoints durante la defensa.

## Alcance académico

Este documento es de carácter académico. La integración con el SII no está implementada. La entidad Factura representa el concepto dentro del sistema EcoMarket SPA.