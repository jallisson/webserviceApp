package com.t2ti.cardapio.model;

/**
 * Created by Claudio on 19/08/2015.
 */
public class Pedido {

    private Integer id;
    private Integer itemCardapioId;
    private Integer quantidade;
    private Integer numeroMesa;
    private String tokenGCM;
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemCardapioId() {
        return itemCardapioId;
    }

    public void setItemCardapioId(Integer itemCardapioId) {
        this.itemCardapioId = itemCardapioId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(Integer numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

	public String getTokenGCM() {
		return tokenGCM;
	}

	public void setTokenGCM(String tokenGCM) {
		this.tokenGCM = tokenGCM;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
