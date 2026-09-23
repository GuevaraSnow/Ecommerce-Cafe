# Mapeo Dominio -> API Compra

|Operación del dominio | Metodo HTTP | Endpoint                |
|---|---|-------------------------|
|Compra.realizar(...)|POST| /compras                |
|consultar una compra|GET| /compras{id}            |
|confirmar()|PUT| compras/(id)/confirmar  |
|solcitarReembolso/motivo)|PUT| /compras/(id)/reembolso |