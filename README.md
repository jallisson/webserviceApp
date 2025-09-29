# CardapioWebService

WebService RESTful para gerenciamento de cardápio desenvolvido em Java para aplicação de balcão.

## 📋 Descrição

Este projeto é um webservice REST que fornece endpoints para gerenciar cardápios de estabelecimentos. Foi desenvolvido utilizando Java EE e integrado com Firebase para funcionalidades em nuvem.

## 🚀 Tecnologias Utilizadas

- Java EE
- JAX-RS (RESTful Web Services)
- Firebase Admin SDK
- Eclipse IDE
- Git

## 📦 Pré-requisitos

- JDK 8 ou superior
- Apache Tomcat 8.5 ou superior
- Eclipse IDE (ou outra IDE Java)
- Conta Firebase (para credenciais)

## ⚙️ Configuração

### 1. Clone o repositório

```bash
git clone git@github.com:jallisson/webserviceApp.git
cd webserviceApp
```

### 2. Configurar credenciais do Firebase

**IMPORTANTE:** Por segurança, as credenciais do Firebase não estão incluídas no repositório.

1. Acesse o [Console do Firebase](https://console.firebase.google.com/)
2. Vá em Configurações do Projeto > Contas de Serviço
3. Clique em "Gerar nova chave privada"
4. Salve o arquivo JSON em: `WebContent/WEB-INF/`
5. Renomeie para um nome apropriado (ex: `firebase-credentials.json`)
6. Atualize o código para referenciar o novo arquivo

### 3. Importar no Eclipse

1. Abra o Eclipse
2. File > Import > Existing Projects into Workspace
3. Selecione a pasta do projeto
4. Clique em Finish

### 4. Configurar servidor

1. Adicione o Apache Tomcat no Eclipse
2. Configure o projeto para rodar no Tomcat
3. Deploy e inicie o servidor

## 🔧 Endpoints da API

```
Base URL: http://localhost:8080/CardapioWebService/
```

### Exemplos de endpoints:

- `GET /api/cardapio` - Lista todos os itens do cardápio
- `POST /api/cardapio` - Adiciona novo item
- `PUT /api/cardapio/{id}` - Atualiza item existente
- `DELETE /api/cardapio/{id}` - Remove item

*Documentação completa dos endpoints em desenvolvimento*

## 📁 Estrutura do Projeto

```
webserviceApp/
├── src/                    # Código-fonte Java
├── WebContent/             # Recursos web
│   └── WEB-INF/           # Configurações e credenciais
├── build/                  # Arquivos compilados
└── .gitignore             # Arquivos ignorados pelo Git
```

## 🔒 Segurança

- **Nunca commite credenciais do Firebase**
- As credenciais devem estar sempre em `.gitignore`
- Rotacione suas chaves regularmente
- Use variáveis de ambiente em produção

## 🛠️ Desenvolvimento

### Fazer alterações

```bash
# Criar nova branch
git checkout -b feature/nova-funcionalidade

# Fazer commits
git add .
git commit -m "Descrição da alteração"

# Push para o GitHub
git push origin feature/nova-funcionalidade
```

## 📝 TODO

- [ ] Documentar todos os endpoints da API
- [ ] Implementar autenticação JWT
- [ ] Adicionar testes unitários
- [ ] Configurar CI/CD
- [ ] Melhorar tratamento de erros

## 👤 Autor

**Jallisson**
- GitHub: [@jallisson](https://github.com/jallisson)

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests.

1. Fork o projeto
2. Crie sua feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📞 Suporte

Para suporte, abra uma issue no GitHub ou entre em contato através do perfil.

---

⭐ Se este projeto foi útil para você, considere dar uma estrela no repositório!
