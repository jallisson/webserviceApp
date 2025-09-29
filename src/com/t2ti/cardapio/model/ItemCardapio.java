package com.t2ti.cardapio.model;

import java.sql.Date;

/**
 * Created by Claudio on 19/08/2015.
 */
public class ItemCardapio {

    private Integer id;
    private Integer tipoDenuncia;
    private String denunciado;
    private String local_denuncia;
    private String descricao;
    private Double valor;
    private String urlImagem;
    private String pRerencia;
    private Date dataDenuncia;
    private String imagemBase64;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDenunciado() {
        return denunciado;
    }

    public void setDenunciado(String nome) {
        this.denunciado = nome;
    }

    public String getLocalDenuncia() {
        return local_denuncia;
    }

    public void setLocalDenuncia(String descricao) {
        this.local_denuncia = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public Integer getTipoDenuncia() {
        return tipoDenuncia;
    }

    public void setTipoDenuncia(Integer categoria) {
        this.tipoDenuncia = categoria;
    }

	public String getImagemBase64() {
		return imagemBase64;
	}

	public void setImagemBase64(String imagemBase64) {
		this.imagemBase64 = imagemBase64;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getpRerencia() {
		return pRerencia;
	}

	public void setpRerencia(String pRerencia) {
		this.pRerencia = pRerencia;
	}

	public Date getDataDenuncia() {
		return dataDenuncia;
	}

	public void setDataDenuncia(Date dataDenuncia) {
		this.dataDenuncia = dataDenuncia;
	}
}
