package com.t2ti.cardapio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.t2ti.cardapio.bd.AcessoBD;
import com.t2ti.cardapio.model.DetalheDenuncia;
import com.t2ti.cardapio.model.ItemCardapio;
import com.t2ti.cardapio.model.NaturezaOcorrencia;
import com.t2ti.cardapio.model.Pedido;
import com.t2ti.cardapio.model.VersaoCardapio;

public class CardapioOld2 {

	private final String apiKey = "SAIzaSyBhSE-i9785Xs113a90Rneoo8KR6AM9s68";
	//private String teste;

	private Gson getGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat("dd/MM/yyyy HH:mm:ss");
		return builder.create();
	}

	public String insereDetalheDenuncia(String jsonString) {
		AcessoBD acessoBD = new AcessoBD();
		DetalheDenuncia detalheDenuncia = new DetalheDenuncia();
		try {
			detalheDenuncia = getGson().fromJson(jsonString, DetalheDenuncia.class);
			String sql = "insert into denuncia (tipo_denuncia, denunciado, local_denuncia, p_referencia, descricao, data_denuncia, url_imagem, token_gcm, status_app, origem, uniqueid, mac) values "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			Connection connection = acessoBD.conectar();

			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, detalheDenuncia.getTipoDenuncia());
			ps.setString(2, detalheDenuncia.getDenunciado());
			ps.setString(3, detalheDenuncia.getLocalDenuncia());
			ps.setString(4, detalheDenuncia.getpReferencia());
			ps.setString(5, detalheDenuncia.getDescricao());
			ps.setDate(6, new Date(Calendar.getInstance().getTimeInMillis()));
			ps.setString(7, detalheDenuncia.getUrlImagem());
			ps.setString(8, detalheDenuncia.getTokenGCM());
			ps.setString(9, "Novo");
			ps.setString(10, "App");
			ps.setString(11, detalheDenuncia.getUniqueId());
			ps.setString(12, detalheDenuncia.getMac());

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				detalheDenuncia.setId(rs.getInt(1));
			}


			salvaImagem(detalheDenuncia, connection, ps);
			//enviaNotificacaoDenuncia("Denuncia: "+detalheDenuncia.getId()+"/2018 recebida com sucesso", detalheDenuncia.getTokenGCM());
			//sendToToken("Denuncia: "+detalheDenuncia.getId()+"/2018 recebida com sucesso", detalheDenuncia.getTokenGCM());
			System.out.println("Antes do metodo sendToken");
			sendToTokenFcm(detalheDenuncia.getTokenGCM(),"Denuncia: "+detalheDenuncia.getId()+"/2020 recebida com sucesso");
			//atualizaVersaoCardapio();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}
		return getGson().toJson(detalheDenuncia, DetalheDenuncia.class);
	}

	public String getDenuncia() {
		AcessoBD acessoBD = new AcessoBD();
		List<DetalheDenuncia> denuncias = new ArrayList<>();
		try {
			String sql = "select * from denuncia where denuncia.origem = 'App'";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				DetalheDenuncia detalheDenuncia = new DetalheDenuncia();
				detalheDenuncia.setId(rs.getInt("id"));
				detalheDenuncia.setDenunciado(rs.getString("denunciado"));

				denuncias.add(detalheDenuncia);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}

		String json = getGson().toJson(denuncias, new TypeToken<ArrayList<DetalheDenuncia>>() {
		}.getType());


		return json;
	}

	public String getNaturezaOcorrencia() {
		AcessoBD acessoBD = new AcessoBD();
		List<NaturezaOcorrencia> listOcorrencia = new ArrayList<>();
		try {
			String sql = "select * from natureza_ocorrencia where noapp = 'SIM'";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				NaturezaOcorrencia ocorrencia = new NaturezaOcorrencia();
				ocorrencia.setId(rs.getInt("id"));
				ocorrencia.setNome(rs.getString("nome"));

				listOcorrencia.add(ocorrencia);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}

		String json = getGson().toJson(listOcorrencia, new TypeToken<ArrayList<NaturezaOcorrencia>>() {
		}.getType());


		return json;
	}

	public String getPedidosPendentes() {
		AcessoBD acessoBD = new AcessoBD();
		List<Pedido> pedidosPendentes = new ArrayList<>();
		try {
			String sql = "select * from pedido where status = ?";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, Constantes.STATUS_PENDENTE);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Pedido pedido = new Pedido();
				pedido.setId(rs.getInt("id"));
				pedido.setNumeroMesa(rs.getInt("numero_mesa"));

				pedidosPendentes.add(pedido);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}

		String json = getGson().toJson(pedidosPendentes, new TypeToken<ArrayList<Pedido>>() {
		}.getType());

		return json;
	}


	public String alteraItemCardapio(String jsonString) {
		AcessoBD acessoBD = new AcessoBD();
		ItemCardapio itemCardapio = new ItemCardapio();
		try {
			itemCardapio = getGson().fromJson(jsonString, ItemCardapio.class);
			String sql = "update item_cardapio set nome = ?, descricao = ?, valor = ?, url_imagem = ?, categoria = ?"
					+ " where id = ?";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, itemCardapio.getDenunciado());
			ps.setString(2, itemCardapio.getLocalDenuncia());
			ps.setDouble(3, itemCardapio.getValor());
			ps.setString(4, itemCardapio.getUrlImagem());
			ps.setInt(5, itemCardapio.getTipoDenuncia());
			ps.setInt(6, itemCardapio.getId());

			ps.executeUpdate();

			//salvaImagem(itemCardapio, connection, ps);
			atualizaVersaoCardapio();
		} catch (Exception e) {
			e.printStackTrace();
			itemCardapio.setId(null);
		} finally {
			acessoBD.desconectar();
		}
		return getGson().toJson(itemCardapio, ItemCardapio.class);
	}

	public String alteraPedido(String jsonString) {
		AcessoBD acessoBD = new AcessoBD();
		Pedido pedido = new Pedido();
		try {
			pedido = getGson().fromJson(jsonString, Pedido.class);

			String sql = "select * from pedido where id = ?";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, pedido.getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				pedido.setTokenGCM(rs.getString("token_gcm"));
			}

			sql = "update pedido set status = ? where id = ?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, pedido.getStatus());
			ps.setInt(2, pedido.getId());

			ps.executeUpdate();

			enviaNotificacao("Pedido Alterado - " + pedido.getStatus(), pedido, pedido.getTokenGCM());
		} catch (Exception e) {
			e.printStackTrace();
			pedido.setId(null);
		} finally {
			acessoBD.desconectar();
		}
		return getGson().toJson(pedido, Pedido.class);
	}


	public String excluiItemCardapio(String jsonString) {
		AcessoBD acessoBD = new AcessoBD();
		ItemCardapio itemCardapio = new ItemCardapio();
		try {
			itemCardapio = getGson().fromJson(jsonString, ItemCardapio.class);
			String sql = " delete from item_cardapio where id = ?";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, itemCardapio.getId());

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			itemCardapio.setId(null);
		} finally {
			acessoBD.desconectar();
		}
		return getGson().toJson(itemCardapio, ItemCardapio.class);
	}

	private void salvaImagem(DetalheDenuncia dDenuncia, Connection con, PreparedStatement ps) throws Exception {
		if (dDenuncia.getImagemBase64() != null) {
			String nomeArquivo = dDenuncia.getId() + ".jpg";
			MessageContext mc = MessageContext.getCurrentMessageContext();
			ServletContext sc = (ServletContext) mc.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT);
			String caminhoArquivo = sc.getRealPath("/imagens") + "//"; // se for servidor windows a barra entre parenteses � invertida --> (\\) se for linux a barra � --> (//)

			//String caminhoArquivo = "C:\\Projetos\\eclipse_mars\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\CardapioWebService\\imagens\\";

			FileOutputStream out = new FileOutputStream(caminhoArquivo + nomeArquivo);
			out.write(Base64.decodeBase64(dDenuncia.getImagemBase64()));
			out.close();

			//item.setUrlImagem("http://192.168.0.13:8080/CardapioWebService/imagens/" + nomeArquivo);
			//dDenuncia.setUrlImagem("http://contx.ddns.net:8080/CardapioWebService//imagens/" + nomeArquivo);
			dDenuncia.setUrlImagem("http://192.168.13.200:8080/CardapioWebService//imagens/" + nomeArquivo);

			//String sql = "update denuncia_app set url_imagem = ? where id = ?";
			//String sql = "update denuncia set url_imagem = ? where id = ?";
			String sql = "update denuncia set url_imagem = ? where id = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, dDenuncia.getUrlImagem());
			ps.setInt(2, dDenuncia.getId());
			ps.executeUpdate();
		}
	}

	public String getVersaoCardapio() {
		AcessoBD acessoBD = new AcessoBD();
		VersaoCardapio versaoCardapio = new VersaoCardapio();
		try {
			String sql = "select * from versao_cardapio";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				versaoCardapio.setId(rs.getInt("id"));
				versaoCardapio.setVersao(rs.getInt("versao"));
				versaoCardapio.setDataVersao(rs.getDate("data_versao"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}

		String json = getGson().toJson(versaoCardapio, VersaoCardapio.class);

		return json;
	}

	private void atualizaVersaoCardapio() {
		AcessoBD acessoBD = new AcessoBD();
		try {

			int versao = 1;
			String sql = "select * from versao_cardapio";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				versao += 1;
				sql = "update versao_cardapio set versao = ?, data_versao = ?";
			} else {
				sql = "insert into versao_cardapio (versao, data_versao) values (?, ?)";
			}

			ps = connection.prepareStatement(sql);
			ps.setInt(1, versao);
			ps.setDate(2, new Date(Calendar.getInstance().getTimeInMillis()));

			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}
	}

	public String geraPedido(String jsonString) {
		AcessoBD acessoBD = new AcessoBD();
		Pedido pedido = new Pedido();
		try {
			pedido = getGson().fromJson(jsonString, Pedido.class);
			String sql = "insert into pedido (item_cardapio_id, quantidade, numero_mesa, token_gcm, status) values "
					+ "(?, ?, ?, ?, ?)";
			Connection connection = acessoBD.conectar();
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, pedido.getItemCardapioId());
			ps.setInt(2, pedido.getQuantidade());
			ps.setInt(3, pedido.getNumeroMesa());
			ps.setString(4, pedido.getTokenGCM());
			ps.setInt(5, Constantes.STATUS_PENDENTE);

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				pedido.setId(rs.getInt(1));
			}

			enviaNotificacao("Pedido recebido com sucesso", pedido, pedido.getTokenGCM());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			acessoBD.desconectar();
		}
		return getGson().toJson(pedido, Pedido.class);
	}

	public void enviaNotificacao(String mensagem, Pedido pedido, String token) {
		Sender sender = new Sender(apiKey);
		Message message = new Message.Builder()
				.addData("message", mensagem)
				.addData("pedido", getGson().toJson(pedido, Pedido.class))
				.build();
		try {
			Result result = sender.send(message, token, 3);

			System.out.println(result.getCanonicalRegistrationId());
			System.out.println(result.getErrorCodeName());
			System.out.println(result.getMessageId());
			System.out.println(pedido.getTokenGCM().substring(0, 10));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void enviaNotificacaoDenuncia(String mensagem, String token) {
		Sender sender = new Sender(apiKey);
		Message message = new Message.Builder()
				.addData("message", mensagem)
				//.addData("denuncia", getGson().toJson(denuncia, DetalheDenuncia.class))
				.build();
		try {
			Result result = sender.send(message, token, 3);

			System.out.println(result.getCanonicalRegistrationId());
			System.out.println(result.getErrorCodeName());
			System.out.println(result.getMessageId());
			//System.out.println(denuncia.getTokenGCM().substring(0, 10));


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendToTokenFcm(String tkn, String corpo)  {
		try {

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","key=SAIzaSyBhSE-i9785Xs113a90Rneoo8KR6AM9s68");
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject json = new JSONObject();

            json.put("to", tkn);


            JSONObject info = new JSONObject();
            info.put("title", "Secretaria do Meio Ambiente");   // Notification title
            info.put("body", corpo); // Notification body

            json.put("notification", info);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();

        }
        catch (Exception e)
        {
            //Log.d("Error",""+e);
            e.printStackTrace();
        }


       // return null;
	  }


}
