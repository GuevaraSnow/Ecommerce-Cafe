package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest

public class PrecioTest {

    @Test
    void dosPreciosConELMismoValorDebenSerIguales(){
        Precio p1= new Precio(50000,"cop");
        Precio p2= new Precio(50000,"cop");
        assertEquals(p1,p2);

    }

    @Test
    void noDebeCrearPrecioNegativo(){
        assertThrows(ReglaDominioException.class, ()->{
            new Precio(-50000,"cop");
        });

    }
}
