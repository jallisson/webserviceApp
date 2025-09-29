package com.t2ti.cardapio.bd;

import java.sql.Connection;
import java.sql.DriverManager;

public class AcessoBD {

	private Connection con;

	public Connection conectar() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			//acesso banco local
			//con = DriverManager.getConnection("jdbc:mysql://192.168.13.200/sip","root","meioadmtec");
			//con = DriverManager.getConnection("jdbc:mysql://192.168.13.136/sip","root","root");
			//con = DriverManager.getConnection("jdbc:mysql://10.0.0.12/sip","root","root");
			con = DriverManager.getConnection("jdbc:mysql://177.185.136.26/sip","root","meioadmtec");

			//acesso banco web
			//con = DriverManager.getConnection("jdbc:mysql://sipjj.com/sipjj426_cardapio?user=sipjj426_root&password=rooot");
			//servidor-alexandre
			//con = DriverManager.getConnection("jdbc:mysql://localhost/denuncia","root","root");
		} catch( Exception e) {
			e.printStackTrace();
		}

		return con;
	}

	public void desconectar() {
		try {
			con.close();
		} catch( Exception e) {
			e.printStackTrace();
		}
	}
}
