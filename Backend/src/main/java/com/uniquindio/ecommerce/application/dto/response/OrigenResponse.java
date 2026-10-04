package com.uniquindio.ecommerce.application.dto.response;

// Exactamente uno de los dos viene informado (el otro es null).
public record OrigenResponse(String loteId, String transformacionId) {}
