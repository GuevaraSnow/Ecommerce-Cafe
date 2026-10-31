package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.LotePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoteRepositorioJpa implements LoteRepositorio {

    private final LoteJpaDataRepository dataRepository;
    private final LotePersistenceMapper mapper;


    @Override
    public Optional<Lote> buscarPorCodigo(CodigoDeLote codigo) {
        return dataRepository.findById(codigo.valor()).map(mapper::toDomain);
    }

    @Override
    public void guardar(Lote lote) {
        dataRepository.save(mapper.toEntity(lote));
    }

    @Override
    public int obtenerNumeroMasAlto(int anio) {
        String prefijo = "LOT-"+anio+"-";
        Optional<Lote> loteEncontrado = dataRepository.findFirstByCodigoDeLoteStartingWithOrderByCodigoDeLoteDesc(prefijo)
                .map(mapper::toDomain);

        return loteEncontrado.map(lote -> lote.getCodigoDeLote().numero()).orElse(0);
    }
}
