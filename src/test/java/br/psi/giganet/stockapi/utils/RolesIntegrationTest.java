package br.psi.giganet.stockapi.utils;

public interface RolesIntegrationTest {

    void readAuthorized() throws Exception;

    void writeAuthorized() throws Exception;

    void readUnauthorized() throws Exception;

    void writeUnauthorized() throws Exception;

}
