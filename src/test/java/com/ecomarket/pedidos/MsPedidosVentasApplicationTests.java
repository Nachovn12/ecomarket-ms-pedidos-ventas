package com.ecomarket.pedidos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MsPedidosVentasApplicationTests {

    @Test
    void contextLoads() {
    }

    // SE AGREGA PARA EL 100%
    @Test
    void mainEjecutaAplicacion() {
        MsPedidosVentasApplication.main(new String[] {"--spring.profiles.active=test"});
    }

}
