package com.doatec.dtos;

public class ItemDoadoDto {
    private String tipoItem;
    private String descricao;

    public ItemDoadoDto(String tipoItem, String descricao) {
        this.tipoItem = tipoItem;
        this.descricao = descricao;
    }

    public ItemDoadoDto() {
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}