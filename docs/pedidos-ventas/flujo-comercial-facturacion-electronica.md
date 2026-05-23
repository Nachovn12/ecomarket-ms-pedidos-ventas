# Flujo comercial y facturación electrónica — EcoMarket SPA

## 1. Objetivo

Documentar el flujo desde carrito hasta factura, incluyendo venta presencial, cupones, devoluciones y reclamaciones.

## 2. Flujo carrito a pedido

1. Cliente agrega productos al CarritoCompra.
2. Se generan ItemCarrito.
3. Cliente confirma compra.
4. Se crea Pedido.
5. Se registran DetallePedido.
6. Se reserva o descuenta stock mediante Inventario.

## 3. Flujo de venta presencial

1. Empleado de Ventas registra productos.
2. Se crea Venta.
3. Se asocia Pago.
4. Se genera Factura.
5. Se actualiza inventario.

## 4. Cupones de descuento

- CuponDescuento se valida antes de confirmar pedido.
- Si el cupón es válido, se descuenta del total.
- Si está vencido o no existe, se rechaza.

## 5. Facturación electrónica

- Factura se asocia a Venta o Pedido.
- Debe contener monto, fecha, medio de pago y detalle.
- En el contexto académico, se documenta como entidad interna sin integración real con SII.

## 6. Devoluciones y reclamaciones

- Devolucion se asocia a Venta/Pedido.
- Reclamacion se asocia a Cliente y opcionalmente a Pedido/Venta.
- Se registra estado y seguimiento.

## 7. Endpoints relacionados

- POST /api/pedidos
- GET /api/pedidos/{id}
- PATCH /api/pedidos/{id}/estado
- POST /api/ventas
- POST /api/ventas/{idVenta}/devoluciones
- POST /api/reclamaciones
- POST /api/cupones/validar
- GET /api/facturas/{id}

## 8. Relación con HU

- HU-12 Consulta de estado del pedido.
- HU-15 Atención de devoluciones y reclamaciones.
- HU-57 Documentación del flujo comercial.
## 9. Alcance académico

Este documento es de carácter académico. La integración con el SII no está implementada. La entidad Factura representa el concepto dentro del sistema EcoMarket SPA.