Aplicación en Spring Boot v3.3.5 (Java 17) usando Maven.

La aplicación consiste en una API CRUD para franquicias, sucursales y productos usando AWS DynamoDb

Para ejecutar el proyecto se necesitan las siguientes credenciales de un usuario de IAM 
que puede hacer consultas únicamente al servicio DynamoDB de mi cuenta personal de AWS, estas se deben 
configurar en el archivo de las credenciales de AWS CLI "credentials"
ubicado en la ruta: cd ~/.aws

para modificar el archivo con VS Code, se recomienda usar el siguiente comando desde una terminal 

comando>> code ~/.aws/credentials

Los siguientes accesos se deben reemplazar en el archivo "credentials" donde se soliciten
(con este usuario de IAM solo tendrá permisos para DynamoDB)

aws.accessKeyId=AKIA27VY4YCSMBAKWXET
aws.secretKey=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

aws.region=us-east-1

Recuerde installar los paquetes de Maven antes de correr la aplicación.

Ejecutar la aplicación ya sea con la interfaz de IntelliJ o
en el VS Code instalando la extensión llamada "Spring Boot Dashboard".


Para probar los diferentes endpoints de la aplicación se listarán a continuación las URL
se recomienda probar desde Postman:

1. Servicio para agregar una nueva franquicia:
   
   URL: http://127.0.0.1:8080/api/franquicias/agregarFranquicia
   Método Http: POST
   Body Example:
   {
    "id":"1234bj", 
    "nombre":"test name",
    "sucursalIds": ["asd", "fgb", "jhn"]
   } 

2. Servicio para agregar una nueva sucursal a una franquicia:

   URL: http://127.0.0.1:8080/api/franquicias/agregarSucursal?franquiciaId=32435&idSucursal=6gt5
   Método Http: PUT


