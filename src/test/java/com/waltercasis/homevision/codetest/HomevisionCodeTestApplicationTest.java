package com.waltercasis.homevision.codetest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class HomevisionCodeTestApplicationTest {

    @Test
    public void context_load(){
        assertDoesNotThrow(()->HomevisionCodeTestApplication.main(new String[] {}));
    }
}