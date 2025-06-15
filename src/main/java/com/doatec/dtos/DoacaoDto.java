package com.doatec.dtos;

import com.doatec.model.donation.PreferenciaEntrega;

public class DoacaoDto {

    private String nome;

    private String tipoDocumento;

    private String numeroDocumento;

    private String email;

    private String telefone;

    private String tipoItem;

    private String descricaoItem;

    private PreferenciaEntrega preferenciaEntrega;

    public DoacaoDto() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getDescricaoItem() {
        return descricaoItem;
    }

    public void setDescricaoItem(String descricaoItem) {
        this.descricaoItem = descricaoItem;
    }

    public PreferenciaEntrega getPreferenciaEntrega() {
        return preferenciaEntrega;
    }

    public void setPreferenciaEntrega(PreferenciaEntrega preferenciaEntrega) {
        this.preferenciaEntrega = preferenciaEntrega;
    }
}