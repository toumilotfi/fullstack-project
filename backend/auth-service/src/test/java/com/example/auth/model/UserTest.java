package com.example.auth.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void newUsersDefaultToInactive() {
        assertEquals(false, new User().getUserActive());
    }
}
