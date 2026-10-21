package com.uniquindio.ecommerce.application.dto.response;

import java.time.LocalDate;

public record DescuentoResponse(int porcentaje, LocalDate fechaInicio, LocalDate fechaFin) {}
