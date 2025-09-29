package com.t2ti.cardapio.model;

import java.sql.Date;

/**
 * Created by Claudio on 21/08/2015.
 */
public class VersaoCardapio {

    private Integer id;
    private Integer versao;
    private Date dataVersao;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public Date getDataVersao() {
        return dataVersao;
    }

    public void setDataVersao(Date dataVersao) {
        this.dataVersao = dataVersao;
    }
}
