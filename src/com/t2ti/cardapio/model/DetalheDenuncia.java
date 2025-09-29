package com.t2ti.cardapio.model;

/**
 * Created by Claudio on 19/08/2015.
 */
public class DetalheDenuncia {

    private Integer id;
    private String tipoDenuncia;
    private String denunciado;
    private String localDenuncia;
    private String pReferencia;
    private String descricao;
    private String urlImagem;
    private String imagemBase64;
    private String tokenGCM;
    private String uniqueId;
    private String mac;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoDenuncia() {
        return tipoDenuncia;
    }

    public void setTipoDenuncia(String tipo_denuncia) {
        this.tipoDenuncia = tipo_denuncia;
    }

    public String getDenunciado() {
        return denunciado;
    }

    public void setDenunciado(String denunciado) {
        this.denunciado = denunciado;
    }

    public String getLocalDenuncia() {
        return localDenuncia;
    }

    public void setLocalDenuncia(String localDenuncia) {
        this.localDenuncia = localDenuncia;
    }

    public String getpReferencia() {
        return pReferencia;
    }

    public void setpReferencia(String pReferencia) {
        this.pReferencia = pReferencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }


    public String getImagemBase64() {
        return imagemBase64;
    }

    public void setImagemBase64(String imagemBase64) {
        this.imagemBase64 = imagemBase64;
    }

    public String getTokenGCM() {
		return tokenGCM;
	}

	public void setTokenGCM(String tokenGCM) {
		this.tokenGCM = tokenGCM;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}



}
