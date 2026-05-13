package com.ecommerce.catalog.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {

    /**
    En esta prueba unitaria se valida que NotFoundException guarde correctamente el mensaje y los argumentos.
    **/
    @Test
    void constructor_StoresMessageAndArgs() {
        //given
        String message = "Not found: %d";
        Object[] args = {1L};

        //when
        NotFoundException exception = new NotFoundException(message, args);

        //then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getArgs()).containsExactly(args);
    }
}
