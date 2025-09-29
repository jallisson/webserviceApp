package com.t2ti.cardapio;



//Imports padrão Java I/O
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.File;


//Imports para HTTP
import java.net.HttpURLConnection;
import java.net.URL;

//Imports para arquivos
import java.nio.file.Files;
import java.nio.file.Paths;

//Imports para segurança e criptografia
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

//Imports para SQL
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

//Imports Java util
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

//Imports Servlet
import javax.servlet.ServletContext;

//Imports Axis2
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;

//Imports JSON Simple (para FCM)
import org.json.simple.JSONObject;

//Imports Google GCM (legacy - pode manter para compatibilidade)
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

//Imports Gson (JSON parsing)
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

//Imports das suas classes de modelo
import com.t2ti.cardapio.bd.AcessoBD;
import com.t2ti.cardapio.model.DetalheDenuncia;
import com.t2ti.cardapio.model.ItemCardapio;
import com.t2ti.cardapio.model.NaturezaOcorrencia;
import com.t2ti.cardapio.model.Pedido;
import com.t2ti.cardapio.model.VersaoCardapio;



public class Cardapio {

	private final String apiKey = "AIzaSyBhSE-i9785Xs113a90Rneoo8KR6AM9s68";
	// ADICIONE ESTAS VARIÁVEIS COMO CAMPOS DA CLASSE
	private String cachedAccessToken = null;
	private long tokenExpirationTime = 0;
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

	        // ✅ MANTER setString e usar getTipoDenunciaTexto()
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
	        System.out.println("Antes do metodo sendToken");
	        sendToTokenFcm(detalheDenuncia.getTokenGCM(),"Denuncia: "+detalheDenuncia.getId()+"/2025 recebida com sucesso");
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
/*
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
			dDenuncia.setUrlImagem("http://177.185.136.26:8080/CardapioWebService//imagens/" + nomeArquivo);

			//String sql = "update denuncia_app set url_imagem = ? where id = ?";
			//String sql = "update denuncia set url_imagem = ? where id = ?";
			String sql = "update denuncia set url_imagem = ? where id = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, dDenuncia.getUrlImagem());
			ps.setInt(2, dDenuncia.getId());
			ps.executeUpdate();
		}
	}*/
	
	
	private void salvaImagem(DetalheDenuncia dDenuncia, Connection con, PreparedStatement ps) throws Exception {
	    if (dDenuncia.getImagemBase64() != null) {
	        String nomeArquivo = dDenuncia.getId() + ".jpg";
	        MessageContext mc = MessageContext.getCurrentMessageContext();
	        ServletContext sc = (ServletContext) mc.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT);
	        String caminhoArquivo = sc.getRealPath("/imagens") + "//";

	        FileOutputStream out = new FileOutputStream(caminhoArquivo + nomeArquivo);
	        // CORREÇÃO AQUI ↓
	        out.write(Base64.getDecoder().decode(dDenuncia.getImagemBase64()));
	        out.close();

	        dDenuncia.setUrlImagem("http://177.185.136.26:8080/CardapioWebService//imagens/" + nomeArquivo);

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
/*
	public void sendToTokenFcm(String token, String corpo) {
	    try {
	        URL url = new URL("https://fcm.googleapis.com/fcm/send");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Authorization", "key=" + apiKey);
	        conn.setRequestProperty("Content-Type", "application/json");

	        JSONObject json = new JSONObject();
	        json.put("to", token);

	        // Dados customizados (para o seu DenunciaFcmListenerService)
	        JSONObject data = new JSONObject();
	        data.put("message", corpo);
	        json.put("data", data);

	        // Notificação visual (para quando app está em background)
	        JSONObject notification = new JSONObject();
	        notification.put("title", "Secretaria do Meio Ambiente");
	        notification.put("body", corpo);
	        json.put("notification", notification);

	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(json.toString());
	        wr.flush();

	        // Verificar resposta
	        int responseCode = conn.getResponseCode();
	        System.out.println("FCM Response Code: " + responseCode);

	        conn.getInputStream();

	    } catch (Exception e) {
	        System.err.println("Erro ao enviar FCM: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	public void sendToTokenFcm(String token, String corpo) {
	    try {
	        String projectId = "com-t2ti-cardapiobalcao";
	        URL url = new URL("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");

	        // TEMPORÁRIO - usar token fake para teste (vai dar erro 401 Unauthorized)
	        conn.setRequestProperty("Authorization", "Bearer FAKE_TOKEN_FOR_TEST");
	        conn.setRequestProperty("Content-Type", "application/json");

	        // Nova estrutura JSON FCM HTTP v1
	        JSONObject message = new JSONObject();
	        message.put("token", token);

	        // Dados customizados
	        JSONObject data = new JSONObject();
	        data.put("message", corpo);
	        message.put("data", data);

	        // Notificação visual
	        JSONObject notification = new JSONObject();
	        notification.put("title", "Secretaria do Meio Ambiente");
	        notification.put("body", corpo);
	        message.put("notification", notification);

	        // JSON principal
	        JSONObject json = new JSONObject();
	        json.put("message", message);

	        System.out.println("Testando FCM v1 para token: " + (token != null ? token.substring(0, 20) + "..." : "NULL"));
	        System.out.println("Payload JSON v1: " + json.toString());

	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(json.toString());
	        wr.flush();

	        // Verificar resposta
	        int responseCode = conn.getResponseCode();
	        System.out.println("FCM v1 Response Code: " + responseCode);

	        if (responseCode == 200) {
	            System.out.println("FCM v1 enviado com sucesso!");
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder response = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                response.append(line);
	            }
	            System.out.println("Resposta FCM: " + response.toString());
	        } else {
	            System.err.println("Erro FCM v1: " + responseCode);
	            InputStream errorStream = conn.getErrorStream();
	            if (errorStream != null) {
	                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
	                StringBuilder errorResponse = new StringBuilder();
	                String line;
	                while ((line = errorReader.readLine()) != null) {
	                    errorResponse.append(line);
	                }
	                System.err.println("Erro detalhes: " + errorResponse.toString());
	            }
	        }

	    } catch (Exception e) {
	        System.err.println("Erro ao enviar FCM v1: " + e.getMessage());
	        e.printStackTrace();
	    }
	}*/
	

	// MÉTODO PARA LER O SERVICE ACCOUNT JSON (CORRIGIDO - USA GSON)
	private JsonObject readServiceAccountJson() throws Exception {
	    MessageContext mc = MessageContext.getCurrentMessageContext();
	    ServletContext sc = (ServletContext) mc.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT);
	    String caminhoCompleto = sc.getRealPath("/WEB-INF/com-t2ti-cardapiobalcao-firebase-adminsdk-fbsvc-345fb00323.json");
	    
	    System.out.println("=== DEBUG ARQUIVO JSON ===");
	    File file = new File(caminhoCompleto);
	    System.out.println("Caminho tentado: " + caminhoCompleto);
	    System.out.println("Arquivo existe? " + file.exists());
	    System.out.println("========================");
	    
	    String jsonContent = new String(Files.readAllBytes(Paths.get(caminhoCompleto)));
	    return new JsonParser().parse(jsonContent).getAsJsonObject();
	}
	
	
	// MÉTODO PARA CRIAR JWT (CORRIGIDO - USA GSON)
	private String createJWT() throws Exception {
	    JsonObject serviceAccount = readServiceAccountJson();
	    
	    long now = System.currentTimeMillis() / 1000;
	    long expiration = now + 3600; // 1 hora
	    
	    // Header JWT
	    String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
	    String encodedHeader = Base64.getUrlEncoder().withoutPadding()
	        .encodeToString(header.getBytes());
	    
	    // Payload JWT
	    String payload = String.format(
	        "{\"iss\":\"%s\",\"scope\":\"https://www.googleapis.com/auth/firebase.messaging\",\"aud\":\"https://oauth2.googleapis.com/token\",\"iat\":%d,\"exp\":%d}",
	        serviceAccount.get("client_email").getAsString(),
	        now,
	        expiration
	    );
	    String encodedPayload = Base64.getUrlEncoder().withoutPadding()
	        .encodeToString(payload.getBytes());
	    
	    // Assinar com a chave privada
	    String dataToSign = encodedHeader + "." + encodedPayload;
	    String signature = signWithPrivateKey(
	        dataToSign, 
	        serviceAccount.get("private_key").getAsString()
	    );
	    
	    return dataToSign + "." + signature;
	}

	// MÉTODO PARA ASSINAR COM CHAVE PRIVADA RSA (MANTÉM IGUAL)
	private String signWithPrivateKey(String data, String privateKeyPem) throws Exception {
	    String privateKeyString = privateKeyPem
	        .replace("-----BEGIN PRIVATE KEY-----", "")
	        .replace("-----END PRIVATE KEY-----", "")
	        .replaceAll("\\s", "");
	    
	    byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
	    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PrivateKey privateKey = keyFactory.generatePrivate(spec);
	    
	    Signature signature = Signature.getInstance("SHA256withRSA");
	    signature.initSign(privateKey);
	    signature.update(data.getBytes());
	    byte[] signatureBytes = signature.sign();
	    
	    return Base64.getUrlEncoder().withoutPadding()
	        .encodeToString(signatureBytes);
	}

	// MÉTODO PARA OBTER ACCESS TOKEN OAuth 2.0 (CORRIGIDO - USA GSON)
	private String getAccessToken() throws Exception {
	    long now = System.currentTimeMillis();
	    if (cachedAccessToken != null && now < tokenExpirationTime) {
	        return cachedAccessToken;
	    }
	    
	    String jwt = createJWT();
	    
	    URL tokenUrl = new URL("https://oauth2.googleapis.com/token");
	    HttpURLConnection conn = (HttpURLConnection) tokenUrl.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    conn.setDoOutput(true);
	    
	    String postData = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwt;
	    
	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    wr.write(postData);
	    wr.flush();
	    
	    int responseCode = conn.getResponseCode();
	    if (responseCode == 200) {
	        BufferedReader br = new BufferedReader(
	            new InputStreamReader(conn.getInputStream())
	        );
	        StringBuilder response = new StringBuilder();
	        String line;
	        while ((line = br.readLine()) != null) {
	            response.append(line);
	        }
	        
	        // CORREÇÃO AQUI ↓
	        JsonObject tokenResponse = new JsonParser().parse(response.toString())
	            .getAsJsonObject();
	        
	        cachedAccessToken = tokenResponse.get("access_token").getAsString();
	        tokenExpirationTime = now + (3000 * 1000); // 50 minutos
	        
	        System.out.println("Access token obtido: " + 
	            cachedAccessToken.substring(0, 20) + "...");
	        return cachedAccessToken;
	        
	    } else {
	        InputStream errorStream = conn.getErrorStream();
	        if (errorStream != null) {
	            BufferedReader errorReader = new BufferedReader(
	                new InputStreamReader(errorStream)
	            );
	            StringBuilder errorResponse = new StringBuilder();
	            String line;
	            while ((line = errorReader.readLine()) != null) {
	                errorResponse.append(line);
	            }
	            System.err.println("Erro OAuth: " + errorResponse.toString());
	        }
	        throw new IOException("Erro autenticação OAuth: " + responseCode);
	    }
	}


	// MÉTODO sendToTokenFcm ATUALIZADO COM AUTENTICAÇÃO REAL
	public void sendToTokenFcm(String token, String corpo) {
	    try {
	        String projectId = "com-t2ti-cardapiobalcao";
	        URL url = new URL("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        
	        // USAR TOKEN OAUTH 2.0 REAL
	        String accessToken = getAccessToken();
	        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
	        conn.setRequestProperty("Content-Type", "application/json");

	        // Nova estrutura JSON FCM HTTP v1
	        JSONObject message = new JSONObject();
	        message.put("token", token);

	        // Dados customizados
	        JSONObject data = new JSONObject();
	        data.put("message", corpo);
	        message.put("data", data);

	        // Notificação visual
	        JSONObject notification = new JSONObject();
	        notification.put("title", "Secretaria do Meio Ambiente");
	        notification.put("body", corpo);
	        message.put("notification", notification);

	        // JSON principal
	        JSONObject json = new JSONObject();
	        json.put("message", message);

	        System.out.println("Enviando FCM v1 REAL para token: " + (token != null ? token.substring(0, 20) + "..." : "NULL"));
	        System.out.println("Payload JSON v1: " + json.toString());

	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(json.toString());
	        wr.flush();

	        // Verificar resposta
	        int responseCode = conn.getResponseCode();
	        System.out.println("FCM v1 Response Code: " + responseCode);

	        if (responseCode == 200) {
	            System.out.println("FCM v1 enviado com sucesso!");
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder response = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                response.append(line);
	            }
	            System.out.println("Resposta FCM: " + response.toString());
	        } else {
	            System.err.println("Erro FCM v1: " + responseCode);
	            InputStream errorStream = conn.getErrorStream();
	            if (errorStream != null) {
	                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
	                StringBuilder errorResponse = new StringBuilder();
	                String line;
	                while ((line = errorReader.readLine()) != null) {
	                    errorResponse.append(line);
	                }
	                System.err.println("Erro detalhes: " + errorResponse.toString());
	            }
	        }
	        
	    } catch (Exception e) {
	        System.err.println("Erro ao enviar FCM v1: " + e.getMessage());
	        e.printStackTrace();
	    }
	}


}
