NewsAPI é uma API feita em Java, uitilzando Spring, Spring Boot, Spring Security, PostgreSQL e alguns outros Frameworks.
Nela é possível se cadastrar como leitor ou escritor, além de publicar, ler, atualizar e excluir e notícias. Também, está implementado nesta API um sistema de token, que é necessário para acessar alguns endpoints, para a criação e verificação desses tokens foram utilizados JWT e Spring Security.
Para mais detalhes acesse sua documentação em: https://documenter.getpostman.com/view/35019224/2sB2qi8cwc.

<h2>Configurações</h2>
<ul>
  <li>Java 17</li>
  <li>Spring Boot 3.4.5</li>
  <li>PostgreSQL 17.4</li>
</ul>

<h2>Como utilizar</h2>
<ul>
  <li>Tenha o Java e o PostgreSQL instalado em sua máquina;</li>
  <li>Clone o projeto na sua máquina: <strong>git clone https://github.com/JacksonMessa/NewsAPI.git</strong>;</li>
  <li>Configure o <a href="https://github.com/JacksonMessa/NewsAPI/blob/master/NewsAPI/src/main/resources/application.properties">application.properties</a> conforme a seu usuário e senha do PostgreSQL;</li>
  <li>Crie o banco de dados <i>news</i> no postgreSQL: <strong>CREATE DATABASE news</strong>;</li>
  <li>Garanta que sua porta 8080 esteja livre ou altere a porta em inserindo <strong>server.port=número_da_porta</strong> no <a href="https://github.com/JacksonMessa/NewsAPI/blob/master/NewsAPI/src/main/resources/application.properties">application.properties</a>;</li>
  <li>Execute o projeto.</li>
</ul>

