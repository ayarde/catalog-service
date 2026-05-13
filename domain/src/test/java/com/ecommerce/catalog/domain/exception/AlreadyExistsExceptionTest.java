package com.ecommerce.catalog.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AlreadyExistsExceptionTest {

    /**
    En esta prueba unitaria se valida que AlreadyExistsException guarde correctamente el mensaje y los argumentos.
    **/
    @Test
    void constructor_StoresMessageAndArgs() {
        //given
        String message = "Conflict: %s";
        Object[] args = {"SKU-123"};

        //when
        AlreadyExistsException exception = new AlreadyExistsException(message, args);

        //then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getArgs()).containsExactly(args);
    }
}
